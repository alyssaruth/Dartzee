package burlton.dartzee.code.ai

import burlton.dartzee.code.core.obj.HashMapList
import burlton.dartzee.code.core.util.Debug
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartsClient
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.listener.DartboardListener
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.stats.GameWrapper
import burlton.dartzee.code.core.util.getSqlDateNow
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
    protected var dartsThrown = mutableListOf<Dart>()

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

        val wrapper = GameWrapper(gameId, gameParams, dtStart!!, dtFinish!!, totalScore)
        wrapper.setHmRoundNumberToDartsThrown(hmRoundNumberToDarts)
        wrapper.setTotalRounds(totalRounds)

        if (DartsClient.devMode)
        {
            wrapper.generateRealEntities(gameType, player)
        }

        return wrapper
    }

    protected open fun resetVariables()
    {
        dartsThrown = mutableListOf()
        hmRoundNumberToDarts = HashMapList()
        currentRound = 1
    }

    protected fun resetRound()
    {
        dartsThrown = mutableListOf()
        dartboard.clearDarts()
    }
}
