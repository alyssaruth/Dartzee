package burlton.dartzee.code.screen.game

import burlton.core.code.util.Debug
import burlton.dartzee.code.utils.PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE
import burlton.dartzee.code.utils.PreferenceUtil
import burlton.desktopcore.code.util.getSqlDateNow
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

abstract class GamePanelPausable<S : DartsScorerPausable>(parent: DartsGameScreen) : DartsGamePanel<S>(parent)
{
    private val aiPauseLock = ReentrantLock()
    private val aiPauseCondition = aiPauseLock.newCondition()
    private var aiShouldPause = false

    /**
     * Abstract methods
     */
    abstract fun saveDartsToDatabase(roundId: String)
    abstract fun currentPlayerHasFinished(): Boolean

    override fun saveDartsAndProceed()
    {
        activeScorer.updatePlayerResult()

        val roundId = currentRound.rowId
        saveDartsToDatabase(roundId)

        //This player has finished. The game isn't necessarily over though...
        if (currentPlayerHasFinished())
        {
            handlePlayerFinish()
        }

        currentPlayerNumber = getNextPlayerNumber(currentPlayerNumber)

        val activePlayers = activeCount
        if (activePlayers > 1)
        {
            //We always keep going if there's more than 1 active person in play
            nextTurn()
        }
        else if (activePlayers == 1)
        {
            activeScorer = hmPlayerNumberToDartsScorer[currentPlayerNumber]

            //Finish the game and set the last player's finishing position if we haven't already
            finishGameIfNecessary()

            if (!activeScorer.paused)
            {
                nextTurn()
            }
            else
            {
                notifyAiPaused()
            }
        }
        else
        {
            notifyAiPaused()
            allPlayersFinished()
        }
    }

    override fun handlePlayerFinish(): Int
    {
        val finishPos = super.handlePlayerFinish()
        activeScorer.finalisePlayerResult(finishPos)
        return finishPos
    }

    override fun shouldAIStop(): Boolean
    {
        if (aiShouldPause)
        {
            Debug.append("Been told to pause, stopping throwing.")
            aiShouldPause = false

            notifyAiPaused()

            return true
        }

        return false
    }

    private fun finishGameIfNecessary()
    {
        if (gameEntity.isFinished())
        {
            return
        }

        val loser = hmPlayerNumberToParticipant[currentPlayerNumber]!!
        loser.finishingPosition = totalPlayers
        loser.saveToDatabase()

        gameEntity.dtFinish = getSqlDateNow()
        gameEntity.saveToDatabase()

        parentWindow.startNextGameIfNecessary()

        //Display this player's result. If they're an AI and we have the preference, then
        //automatically play on.
        activeScorer.finalisePlayerResult(totalPlayers)
        if (loser.isAi() && PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE))
        {
            activeScorer.toggleResume()
        }
    }

    fun pauseLastPlayer()
    {
        if (!activeScorer.getHuman())
        {
            aiPauseLock.withLock{
                aiShouldPause = true
                aiPauseCondition.await()
            }
        }

        //Now the player has definitely stopped, reset the round
        resetRound()
        dartboard.stopListening()
    }

    fun unpauseLastPlayer()
    {
        //If we've come through game load, we'll have disabled this.
        slider.isEnabled = true

        val pt = hmPlayerNumberToParticipant[currentPlayerNumber]!!
        if (currentRound == null || !currentRound.isForParticipant(pt))
        {
            //We need to create a new Round entity. Either we've come through game load or it's the first time we've pressed unpause.
            nextTurn()
        }
        else
        {
            //Don't do nextTurn() as that will up the round number when we don't want it to be upped
            readyForThrow()
        }
    }

    /**
     * When we click the pause button, the EDT is frozen while we wait for the AI to actually pause.
     * At this point, one of two things will happen. Both will result in us notifying to wake the EDT back up.
     *
     * - The AI will come to begin a throw and find that it should stop.
     * - The AI was throwing its last dart as pause was pressed, so won't hit the above code.
     * For this reason, we also notify when the last player totally finishes, *after* the point that the
     * pause/unpause button has been removed from the screen.
     */
    private fun notifyAiPaused()
    {
        aiPauseLock.withLock {
            aiPauseCondition.signalAll()
        }
    }
}
