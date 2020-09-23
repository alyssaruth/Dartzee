package dartzee.screen.game.scorer

import dartzee.game.state.AbstractPlayerState
import dartzee.helper.AbstractTest

abstract class AbstractScorerTest<S: DartsScorer<out AbstractPlayerState<*>>> : AbstractTest()
{
    abstract fun factoryScorerImpl(): S
    abstract fun addRound(scorer: S, roundNumber: Int)

    protected fun factoryScorer(): S
    {
        val s = factoryScorerImpl()
        s.init(null)
        return s
    }
}