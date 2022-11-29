package dartzee.ai

import dartzee.core.obj.HashMapList
import dartzee.core.util.getSqlDateNow
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.listener.DartboardListener
import dartzee.`object`.Dart
import dartzee.`object`.DartsClient
import dartzee.screen.Dartboard
import dartzee.stats.GameWrapper
import dartzee.utils.convertForUiDartboard
import java.awt.Point
import java.sql.Timestamp

abstract class AbstractDartsSimulation(val dartboard: Dartboard,
                                       val player: PlayerEntity,
                                       val model: DartsAiModel) : DartboardListener
{
    //Transient things
    protected var dtStart: Timestamp? = null
    protected var dtFinish: Timestamp? = null
    protected var currentRound = -1
    protected var dartsThrown = mutableListOf<Dart>()

    protected var hmRoundNumberToDarts = HashMapList<Int, Dart>()

    abstract val gameParams: String
    abstract val gameType: GameType

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
        dartboard.clearDarts()
    }

    protected fun dartThrown(aiPt: Point)
    {
        val pt = convertForUiDartboard(aiPt, dartboard)
        dartboard.dartThrown(pt)
    }
}
