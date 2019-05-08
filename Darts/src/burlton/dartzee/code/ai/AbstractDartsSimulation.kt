package burlton.dartzee.code.ai

import burlton.core.code.obj.HandyArrayList
import burlton.core.code.obj.HashMapList
import burlton.core.code.util.Debug
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.db.BulkInserter
import burlton.dartzee.code.db.DartEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.db.RoundEntity
import burlton.dartzee.code.listener.DartboardListener
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.stats.GameWrapper
import burlton.dartzee.test.helper.insertGame
import burlton.dartzee.test.helper.insertParticipant
import burlton.desktopcore.code.util.getSqlDateNow
import java.sql.Timestamp

abstract class AbstractDartsSimulation(protected var dartboard: Dartboard,
                                       protected var player: PlayerEntity,
                                       protected var model: AbstractDartsModel) : DartboardListener
{
    protected var logging = false

    //Transient things
    protected var dtStart: Timestamp? = null
    protected var dtFinish: Timestamp? = null
    protected var currentRound = -1
    protected var dartsThrown = HandyArrayList<Dart>()

    protected var hmRoundNumberToDarts = HashMapList<Int, Dart>()

    abstract val gameParams: String
    abstract val gameType: Int

    init
    {
        dartboard.addDartboardListener(this)
    }

    abstract fun shouldPlayCurrentRound(): Boolean
    abstract fun startRound()
    abstract fun getTotalScore(): Int

    fun simulateGame(gameId: Long): GameWrapper
    {
        resetVariables()

        dtStart = getSqlDateNow()

        while (shouldPlayCurrentRound())
        {
            startRound()
        }

        dtFinish = getSqlDateNow()

        val totalRounds = currentRound - 1
        val totalScore = getTotalScore()

        Debug.appendBanner("Game Over. Rounds: $totalRounds, Score: $totalScore", logging)

        saveEntities()

        val wrapper = GameWrapper(gameId, gameParams, dtStart!!, dtFinish!!, totalScore)
        wrapper.setHmRoundNumberToDartsThrown(hmRoundNumberToDarts)
        wrapper.setTotalRounds(totalRounds)

        return wrapper
    }

    private fun saveEntities()
    {
        val game = insertGame(gameType = gameType,
                gameParams = gameParams,
                dtCreation = dtStart!!,
                dtFinish = dtFinish!!)

        val pt = insertParticipant(gameId = game.rowId, playerId = player.rowId, finalScore = getTotalScore(), dtFinished = dtFinish!!, ordinal = 1)

        val roundsToSave = mutableListOf<RoundEntity>()
        val dartsToSave = mutableListOf<DartEntity>()
        for (i in 1 until currentRound)
        {
            val darts = hmRoundNumberToDarts[i]!!

            val round = RoundEntity()
            round.assignRowId()
            round.roundNumber = i
            round.participantId = pt.rowId
            roundsToSave.add(round)

            darts.forEachIndexed { ix, drt ->

                dartsToSave.add(DartEntity.factory(drt, round.rowId, ix + 1, drt.startingScore))
            }
        }

        BulkInserter.insert(roundsToSave)
        BulkInserter.insert(dartsToSave)
    }

    protected open fun resetVariables()
    {
        dartsThrown = HandyArrayList()
        hmRoundNumberToDarts = HashMapList()
        currentRound = 1
    }

    protected fun resetRound()
    {
        dartsThrown = HandyArrayList()
        dartboard.clearDarts()
    }
}
