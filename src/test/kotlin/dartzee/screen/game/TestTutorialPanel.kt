package dartzee.screen.game

import com.github.alyssaburlton.swingtest.findAll
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeDisabled
import com.github.alyssaburlton.swingtest.shouldBeEnabled
import dartzee.*
import dartzee.bean.DartLabel
import dartzee.e2e.throwHumanDart
import dartzee.e2e.throwHumanRound
import dartzee.helper.AbstractTest
import dartzee.`object`.SegmentType
import dartzee.screen.GameplayDartboard
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JLabel

class TestTutorialPanel: AbstractTest() {
    @Test
    fun `Should launch the game when ready button is pressed`() {
        val parentWindow = mockk<DartsGameScreen>(relaxed = true)
        val panel = TutorialPanel(parentWindow)
        panel.clickButton(text = "I'm ready - let's play!")

        verify { parentWindow.tutorialFinished() }
    }

    @Test
    fun `Should enable round controls appropriately`() {
        val panel = TutorialPanel(mockk(relaxed = true))

        val confirmButton = panel.getChild<JButton> { it.toolTipText == "Confirm round" }
        val resetButton = panel.getChild<JButton> { it.toolTipText == "Reset round" }
        confirmButton.shouldBeDisabled()
        resetButton.shouldBeDisabled()

        panel.throwHumanDart(20, SegmentType.TREBLE)
        confirmButton.shouldBeEnabled()
        resetButton.shouldBeEnabled()

        resetButton.doClick()
        confirmButton.shouldBeDisabled()
        resetButton.shouldBeDisabled()

        panel.throwHumanDart(20, SegmentType.TREBLE)
        confirmButton.doClick()
        confirmButton.shouldBeDisabled()
        resetButton.shouldBeDisabled()
    }

    @Test
    fun `Should reset darts thrown`() {
        val panel = TutorialPanel(mockk(relaxed = true))
        val dartboard = panel.getChild<GameplayDartboard>()
        dartboard.size = Dimension(500, 500)

        panel.throwHumanDart(20, SegmentType.TREBLE)
        panel.throwHumanDart(20, SegmentType.OUTER_SINGLE)
        dartboard.findAll<DartLabel>().shouldNotBeEmpty()

        panel.clickButton { it.toolTipText == "Reset round" }
        dartboard.findAll<DartLabel>().shouldBeEmpty()

        panel.getChild<JLabel>("ScoredLabel").text shouldBe "0"
    }

    @Test
    fun `Should accurately track the score of the darts thrown`() {
        val panel = TutorialPanel(mockk(relaxed = true))
        val scoredLabel = panel.getChild<JLabel>("ScoredLabel")

        panel.throwHumanDart(20, SegmentType.TREBLE)
        scoredLabel.text shouldBe "60"

        panel.throwHumanDart(20, SegmentType.OUTER_SINGLE)
        scoredLabel.text shouldBe "T20 + 20 = 80"

        panel.clickButton { it.toolTipText == "Confirm round" }
        scoredLabel.text shouldBe "0"
    }

    @Test
    fun `Should accurately record busts in the demo`() {
        val panel = TutorialPanel(mockk(relaxed = true))

        panel.throwHumanRound(drtTrebleTwenty(), drtDoubleTwenty(), drtOuterTwenty()) // 181
        panel.throwHumanRound(drtTrebleTwenty(), drtDoubleTwenty(), drtDoubleTwenty()) //  41

        val label = panel.getChild<JLabel>("RemainingLabel")
        label.text shouldBe "41"

        panel.throwHumanRound(drtOuterTwenty(), drtOuterTwenty(), drtOuterTwo())
        label.text shouldBe "41"
    }

    @Test
    fun `Should accurately record finishes in the demo`() {
        val panel = TutorialPanel(mockk(relaxed = true))

        panel.throwHumanRound(drtTrebleTwenty(), drtDoubleTwenty(), drtOuterTwenty()) // 181
        panel.throwHumanRound(drtTrebleTwenty(), drtDoubleTwenty(), drtDoubleTwenty()) //  41

        val label = panel.getChild<JLabel>("RemainingLabel")
        label.text shouldBe "41"

        panel.throwHumanRound(drtOuterTwenty(), drtOuterTwenty(), drtOuterOne())
        label.text shouldBe "You win!"
    }
}