package dartzee.screen.game.scorer

import dartzee.`object`.Dart
import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.Test

class TestDartsScorerX01: AbstractScorerTest<DartsScorerX01>()
{
    override fun factoryScorerImpl() = DartsScorerX01(mockk(relaxed = true), "501")
    override fun addRound(scorer: DartsScorerX01, roundNumber: Int)
    {
        scorer.addDart(Dart(20, 1))
        scorer.addDart(Dart(20, 2))
        scorer.addDart(Dart(20, 1))
    }

    @Test
    fun `should have 4 columns`()
    {
        val scorer = factoryScorer()
        scorer.getNumberOfColumns() shouldBe 4
    }
}