package burlton.dartzee.code.screen.game

import burlton.dartzee.code.achievements.getWinAchievementRef
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.screen.game.scorer.DartsScorer

abstract class GamePanelFixedLength<S : DartsScorer, D: Dartboard>(parent: AbstractDartsGameScreen, game: GameEntity):
        DartsGamePanel<S, D>(parent, game)
{
    abstract val totalRounds: Int

    fun finishRound()
    {
        if (currentRoundNumber == totalRounds)
        {
            handlePlayerFinish()
        }

        currentPlayerNumber = getNextPlayerNumber(currentPlayerNumber)
        if (getActiveCount() > 0)
        {
            nextTurn()
        }
        else
        {
            finishGame()
        }
    }

    private fun finishGame()
    {
        //Get the participants sorted by score so we can assign finishing positions
        setFinishingPositions()

        updateScorersWithFinishingPositions()

        allPlayersFinished()

        parentWindow?.startNextGameIfNecessary()
    }

    private fun setFinishingPositions()
    {
        //If there's only one player, it's already set to -1 which is correct
        if (totalPlayers == 1)
        {
            return
        }

        val participants = hmPlayerNumberToParticipant.values.sortedBy { it.finalScore }

        var previousScore = Integer.MAX_VALUE
        var finishPos = 1
        for (i in participants.indices)
        {
            val pt = participants[i]
            val ptScore = pt.finalScore
            if (ptScore > previousScore)
            {
                finishPos++
            }

            pt.finishingPosition = finishPos
            pt.saveToDatabase()

            if (finishPos == 1)
            {
                val achievementRef = getWinAchievementRef(gameEntity.gameType)
                AchievementEntity.incrementAchievement(achievementRef, pt.playerId, getGameId())
            }

            previousScore = pt.finalScore
        }
    }

    override fun getFinishingPositionFromPlayersRemaining(): Int
    {
        //Finishing positions are determined at the end
        return -1
    }
}