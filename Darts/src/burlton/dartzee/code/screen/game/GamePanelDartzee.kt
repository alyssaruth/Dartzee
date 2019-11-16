package burlton.dartzee.code.screen.game

import burlton.core.code.obj.HashMapList
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.db.DartzeeRoundResultEntity
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.screen.dartzee.DartboardRuleVerifier
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCarousel
import burlton.dartzee.code.screen.dartzee.IDartzeeCarouselHoverListener
import burlton.dartzee.code.screen.game.scorer.DartsScorerDartzee
import java.awt.BorderLayout

class GamePanelDartzee(parent: AbstractDartsGameScreen, game: GameEntity) :
        DartsGamePanel<DartsScorerDartzee>(parent, game),
        IDartzeeCarouselHoverListener
{
    val dtos = DartzeeRuleEntity().retrieveForGame(game.rowId).map { it.toDto() }

    val carousel = DartzeeRuleCarousel(this, dtos)

    init
    {
        add(carousel, BorderLayout.NORTH)
    }

    override fun factoryDartboard() = DartboardRuleVerifier()

    override fun doAiTurn(model: AbstractDartsModel)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, totalRounds: Int)
    {
        val pt = hmPlayerNumberToParticipant[playerNumber]!!

        val roundResults = DartzeeRoundResultEntity().retrieveEntities("PlayerId = ${pt.playerId} AND ParticipantId = ${pt.rowId}")
    }

    override fun updateVariablesForNewRound() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resetRoundVariables() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateVariablesForDartThrown(dart: Dart) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun shouldStopAfterDartThrown(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun shouldAIStop(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveDartsAndProceed() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun initImpl(game: GameEntity)
    {

    }

    override fun factoryStatsPanel(): GameStatisticsPanel? = null
    override fun factoryScorer() = DartsScorerDartzee()

    override fun hoverChanged(validSegments: List<DartboardSegment>)
    {

    }
}