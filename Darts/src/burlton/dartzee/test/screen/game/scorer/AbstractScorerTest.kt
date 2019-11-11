package burlton.dartzee.test.screen.game.scorer

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.screen.game.scorer.DartsScorer
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.shouldBe
import org.junit.Test

abstract class AbstractScorerTest<S: DartsScorer> : AbstractDartsTest()
{
    abstract fun getValidGameParams(): String
    abstract fun factoryScorerImpl(): S
    abstract fun addRound(scorer: S, roundNumber: Int)

    @Test
    fun `Should clear down the current round correctly`()
    {
        val scorer = factoryScorer()

        addRound(scorer, 1)

        scorer.addDart(Dart(2, 0))
        scorer.getRowCount() shouldBe 2

        scorer.clearRound(2)
        scorer.getRowCount() shouldBe 1
    }

    @Test
    fun `Should ignore round numbers that are higher than how many there are`()
    {
        val scorer = factoryScorer()

        scorer.addDart(Dart(2, 0))
        scorer.getRowCount() shouldBe 1

        scorer.clearRound(2)
        scorer.getRowCount() shouldBe 1
    }

    protected fun factoryScorer(): S
    {
        val s = factoryScorerImpl()
        s.init(null, getValidGameParams())
        return s
    }
}