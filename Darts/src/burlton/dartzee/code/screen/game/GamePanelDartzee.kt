package burlton.dartzee.code.screen.game

import burlton.core.code.obj.HashMapList
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.db.DartzeeRoundResult
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.screen.dartzee.DartboardRuleVerifier
import burlton.dartzee.code.screen.game.scorer.DartsScorerDartzee

class GamePanelDartzee(parent: AbstractDartsGameScreen, game: GameEntity) : DartsGamePanel<DartsScorerDartzee>(parent, game)
{
    val dtos = DartzeeRuleEntity().retrieveForGame(game.rowId)

    override fun factoryDartboard() = DartboardRuleVerifier()

    override fun doAiTurn(model: AbstractDartsModel)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, totalRounds: Int)
    {
        val pt = hmPlayerNumberToParticipant[playerNumber]!!

        val roundResults = DartzeeRoundResult().retrieveEntities("PlayerId = ${pt.playerId} AND ParticipantId = ${pt.rowId}")
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

}