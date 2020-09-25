package dartzee.screen.game.scorer

import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.Test

class TestDartsScorerX01: AbstractTest()
{
    @Test
    fun `should have 4 columns`()
    {
        val scorer = factoryScorer()
        scorer.getNumberOfColumns() shouldBe 4
    }

    private fun factoryScorer(): DartsScorerX01
    {
        val scorer = DartsScorerX01(mockk(relaxed = true), "501")
        scorer.init(null)
        return scorer
    }
}