package burlton.dartzee.code.screen.game

import burlton.core.code.obj.HashMapList
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.db.GameEntity

class GamePanelDartzee(parent: AbstractDartsGameScreen, game: GameEntity) : DartsGamePanel<DartsScorerDartzee>(parent, game)
{
    override fun doAiTurn(model: AbstractDartsModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, totalRounds: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    override fun initImpl(gameParams: String) { }

    override fun factoryStatsPanel() = null
    override fun factoryScorer() = DartsScorerDartzee()

}