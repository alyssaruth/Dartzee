package dartzee.ai

import dartzee.core.obj.HashMapList
import dartzee.core.util.getSqlDateNow
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.`object`.Dart
import dartzee.`object`.DartsClient
import dartzee.stats.GameWrapper
import dartzee.utils.getDartForSegment
import java.awt.Point
import java.sql.Timestamp

abstract class AbstractDartsSimulation(val player: PlayerEntity,
                                       val model: DartsAiModel)
{
    //Transient things
    protected var dtStart: Timestamp? = null
    protected var dtFinish: Timestamp? = null
    protected var currentRound = -1
    protected var dartsThrown = mutableListOf<Dart>()

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

        dtStart = getSqlDateNow()

        while (shouldPlayCurrentRound())
        {
            startRound()
        }

        dtFinish = getSqlDateNow()

        val totalRounds = currentRound - 1
        val totalScore = getTotalScore()

        val wrapper = GameWrapper(gameId, gameParams, dtStart!!, dtFinish!!, totalScore, false)
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
    }

    protected fun dartThrown(aiPt: Point)
    {
        val segment = AI_DARTBOARD.getSegmentForPoint(aiPt)
        dartThrown(getDartForSegment(aiPt, segment))
    }
}
