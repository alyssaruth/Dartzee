package dartzee.screen.dartzee

import dartzee.`object`.SegmentType
import dartzee.dartzee.DartzeeRoundResult
import dartzee.helper.*
import dartzee.screen.game.dartzee.DartzeeRuleCarousel
import dartzee.screen.game.dartzee.DartzeeRuleSummaryPanel
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.DurationTimer
import dartzee.utils.getAllPossibleSegments
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.numerics.shouldBeLessThan
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestDartzeeRuleSummaryPanel: AbstractTest()
{
    @Test
    fun `Should show the high score panel by default`()
    {
        val summaryPanel = makeSummaryPanel()
        summaryPanel.components.toList().shouldContainExactly(summaryPanel.panelHighScore)
    }

    @Test
    fun `Should swap out the high score panel and update the carousel for roundnumber greater than 1`()
    {
        val carousel = mockk<DartzeeRuleCarousel>(relaxed = true)

        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val results = makeRoundResultEntities(DartzeeRoundResult(2, true, 35))
        val darts = listOf(makeDart(19, 1, SegmentType.OUTER_SINGLE), makeDart(7, 1, SegmentType.DOUBLE), makeDart(2, 1, SegmentType.INNER_SINGLE))
        summaryPanel.update(results, darts, 103, 2)

        summaryPanel.components.toList().shouldContainExactly(carousel)
        verify { carousel.update(results, darts, 103) }
    }

    @Test
    fun `Should return all valid segments if on the scoring round`()
    {
        val summaryPanel = makeSummaryPanel()

        summaryPanel.getSegmentStatus() shouldBe SegmentStatus(getAllPossibleSegments(), getAllPossibleSegments())
    }

    @Test
    fun `Should return the carousel segments once past round one`()
    {
        val carousel = mockk<DartzeeRuleCarousel>(relaxed = true)
        every { carousel.getSegmentStatus() } returns SegmentStatus(getOuterSegments(), getOuterSegments())

        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        summaryPanel.update(listOf(), listOf(), 103, 2)

        summaryPanel.getSegmentStatus() shouldBe SegmentStatus(getOuterSegments(), getOuterSegments())
    }

    @Test
    fun `Should swap in the carousel and call gameFinished`()
    {
        val carousel = mockk<DartzeeRuleCarousel>(relaxed = true)

        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        summaryPanel.gameFinished()

        summaryPanel.components.toList().shouldContainExactly(carousel)
        verify { carousel.gameFinished() }
    }

    @Test
    fun `Should call through to the carousel to select a tile`()
    {
        val carousel = mockk<DartzeeRuleCarousel>(relaxed = true)
        val model = beastDartsModel()

        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        summaryPanel.selectRule(model)

        verify { carousel.selectRule(model) }
    }

    @Test
    fun `Should wait for the carousel to be initialised`()
    {
        val carousel = mockk<DartzeeRuleCarousel>(relaxed = true)

        var repetitions = 0
        every { carousel.initialised } answers {
            repetitions++
            repetitions == 6
        }

        val panel = DartzeeRuleSummaryPanel(carousel)

        val timer = DurationTimer()
        panel.ensureReady()

        val duration = timer.getDuration()
        duration.shouldBeGreaterThan(1000)
        duration.shouldBeLessThan(2000)
    }

    private fun makeSummaryPanel() =
        DartzeeRuleSummaryPanel(mockk(relaxed = true))
}