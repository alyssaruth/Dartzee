package dartzee.screen.game.scorer

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import dartzee.core.helper.verifyNotCalled
import dartzee.core.util.DateStatics
import dartzee.core.util.getSqlDateNow
import dartzee.game.state.TestPlayerState
import dartzee.helper.AbstractTest
import dartzee.helper.insertParticipant
import dartzee.screen.game.GamePanelPausable
import dartzee.screen.game.makeSingleParticipant
import dartzee.shouldHaveColours
import dartzee.utils.DartsColour
import dartzee.utils.ResourceCache.ICON_PAUSE
import dartzee.utils.ResourceCache.ICON_RESUME
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
import javax.swing.JButton
import org.junit.jupiter.api.Test

class TestAbstractDartsScorerPausable : AbstractTest() {
    @Test
    fun `Should not be paused by default, when button is not visible`() {
        val scorer = factoryScorer()
        scorer.getPaused() shouldBe false
        scorer.getChild<JButton> { it.icon == ICON_RESUME }.shouldNotBeVisible()
    }

    @Test
    fun `Should show the right result for an unfinished player`() {
        val scorer = factoryScorer()
        val defaultLabelColours = Pair(scorer.lblResult.background, scorer.lblResult.foreground)
        val state = TestPlayerState(insertParticipant(), scoreSoFar = 25)
        scorer.stateChanged(state)

        scorer.lblResult.text shouldBe "25 Darts"
        scorer.lblResult.shouldHaveColours(defaultLabelColours)
        scorer.getChild<JButton> { it.icon == ICON_RESUME }.shouldNotBeVisible()
    }

    @Test
    fun `Should show the correct state for a completely finished player`() {
        val scorer = factoryScorer()
        val state =
            TestPlayerState(
                insertParticipant(finishingPosition = 2, dtFinished = getSqlDateNow()),
                scoreSoFar = 30
            )
        scorer.stateChanged(state)

        scorer.lblResult.text shouldBe "30 Darts"
        scorer.lblResult.shouldHaveColours(DartsColour.SECOND_COLOURS)
        scorer.getChild<JButton> { it.icon == ICON_RESUME }.shouldNotBeVisible()
    }

    @Test
    fun `Should show the correct state for a resigned player`() {
        val scorer = factoryScorer()
        val state =
            TestPlayerState(
                insertParticipant(
                    finishingPosition = 2,
                    resigned = true,
                    dtFinished = getSqlDateNow()
                ),
                scoreSoFar = 30
            )
        scorer.stateChanged(state)

        scorer.lblResult.text shouldBe "RESIGNED"
        scorer.lblResult.shouldHaveColours(DartsColour.SECOND_COLOURS)
        scorer.getChild<JButton> { it.icon == ICON_RESUME }.shouldNotBeVisible()
    }

    @Test
    fun `Should show the correct paused state for a player who came last but has not finished`() {
        val scorer = factoryScorer()
        val state =
            TestPlayerState(
                insertParticipant(finishingPosition = 2, dtFinished = DateStatics.END_OF_TIME),
                scoreSoFar = 30
            )
        scorer.stateChanged(state)

        scorer.lblResult.text shouldBe "Unfinished"
        scorer.lblResult.shouldHaveColours(DartsColour.SECOND_COLOURS)
        scorer.getChild<JButton> { it.icon == ICON_RESUME }.shouldBeVisible()
        scorer.getPaused() shouldBe true
    }

    @Test
    fun `Unpausing and pausing should update the state`() {
        val scorer = factoryScorer()
        val defaultLabelColours = Pair(scorer.lblResult.background, scorer.lblResult.foreground)
        val state =
            TestPlayerState(
                insertParticipant(finishingPosition = 2, dtFinished = DateStatics.END_OF_TIME),
                scoreSoFar = 30
            )
        scorer.stateChanged(state)

        scorer.clickChild<JButton> { it.icon == ICON_RESUME }
        scorer.lblResult.text shouldBe "30 Darts"
        scorer.lblResult.shouldHaveColours(defaultLabelColours)
        scorer.getPaused() shouldBe false

        scorer.clickChild<JButton> { it.icon == ICON_PAUSE }
        scorer.lblResult.text shouldBe "Unfinished"
        scorer.lblResult.shouldHaveColours(DartsColour.SECOND_COLOURS)
        scorer.getPaused() shouldBe true
    }

    @Test
    fun `Should notify parent of pause and unpause`() {
        val parent = mockk<GamePanelPausable<*, *>>(relaxed = true)
        val scorer = factoryScorer(parent)
        val state =
            TestPlayerState(
                insertParticipant(finishingPosition = 2, dtFinished = DateStatics.END_OF_TIME),
                scoreSoFar = 30
            )
        scorer.stateChanged(state)

        scorer.clickChild<JButton> { it.icon == ICON_RESUME }
        verify { parent.unpauseLastPlayer() }
        verifyNotCalled { parent.pauseLastPlayer() }

        clearMocks(parent)
        scorer.clickChild<JButton> { it.icon == ICON_PAUSE }
        verifyNotCalled { parent.unpauseLastPlayer() }
        verify { parent.pauseLastPlayer() }
    }

    private fun factoryScorer(parent: GamePanelPausable<*, *> = mockk(relaxed = true)) =
        FakeDartsScorerPausable(parent).also { it.init() }

    private class FakeDartsScorerPausable(parent: GamePanelPausable<*, *>) :
        AbstractDartsScorerPausable<TestPlayerState>(parent, makeSingleParticipant()) {
        override fun getNumberOfColumns() = 4

        override fun initImpl() {}

        override fun stateChangedImpl(state: TestPlayerState) {
            finalisePlayerResult(state)
        }
    }
}
