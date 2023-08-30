package dartzee.ai

import dartzee.core.obj.HashMapList
import dartzee.core.util.getSqlDateNow
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.`object`.ComputedPoint
import dartzee.`object`.Dart
import dartzee.`object`.DartsClient
import dartzee.stats.GameWrapper
import dartzee.utils.getDartForSegment

abstract class AbstractDartsSimulation(val player: PlayerEntity,
                                       val model: DartsAiModel)
{
    //Transient things
    protected var currentRound = -1
    protected val dartsThrown = mutableListOf<Dart>()

    protected var hmRoundNumberToDarts = HashMapList<Int, Dart>()

    abstract val gameParams: String
    abstract val gameType: GameType

    abstract fun shouldPlayCurrentRound(): Boolean
    abstract fun startRound()
    abstract fun getTotalScore(): Int
    abstract fun dartThrown(dart: Dart)

    fun simulateGame(gameId: Long): GameWrapper
    {
        resetVariables()

        val dtStart = getSqlDateNow()

        while (shouldPlayCurrentRound())
        {
            startRound()
        }

        val dtFinish = getSqlDateNow()

        val totalRounds = currentRound - 1
        val totalScore = getTotalScore()

        val wrapper = GameWrapper(gameId, gameParams, dtStart, dtFinish, totalScore, false, totalRounds, hmRoundNumberToDarts)

        if (DartsClient.devMode)
        {
            wrapper.generateRealEntities(gameType, player)
        }

        return wrapper
    }

    protected open fun resetVariables()
    {
        dartsThrown.clear()
        hmRoundNumberToDarts = HashMapList()
        currentRound = 1
    }

    protected fun resetRound()
    {
        dartsThrown.clear()
    }

    protected fun confirmRound()
    {
        hmRoundNumberToDarts[currentRound] = dartsThrown.toMutableList()
    }

    protected fun dartThrown(aiPt: ComputedPoint)
    {
        dartThrown(getDartForSegment(aiPt.segment))
    }
}
