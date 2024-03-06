package dartzee.e2e

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.findWindow
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import dartzee.bean.GameSetupPlayerSelector
import dartzee.clickButton
import dartzee.core.bean.ScrollTable
import dartzee.helper.AbstractTest
import dartzee.helper.preparePlayers
import dartzee.`object`.SegmentType
import dartzee.screen.DartsApp
import dartzee.screen.ScreenCache
import dartzee.screen.game.DartsGamePanel
import dartzee.screen.game.DartsGameScreen
import dartzee.waitForAssertionWithReturn
import io.kotest.matchers.nulls.shouldNotBeNull
import javax.swing.JButton
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestPartyModeE2E : AbstractTest() {
    @Test
    @Tag("e2e")
    fun `E2E - Party Mode`() {
        preparePlayers(2)

        val app = launchApp()
        app.clickButton(text = "Utilities")
        app.clickButton(text = "Enter Party Mode")
        app.clickButton(text = "New Game")

        val selector = app.getChild<GameSetupPlayerSelector>()
        selector.getChild<ScrollTable>("TableUnselected").selectRow(0)
        selector.clickButton("Select")
        selector.getChild<ScrollTable>("TableUnselected").selectRow(0)
        selector.clickButton("Select")
        app.clickButton(text = "Launch Game >")

        val gameWindow = waitForAssertionWithReturn {
            val window = findWindow<DartsGameScreen>()
            window.shouldNotBeNull()
            window.shouldBeVisible()
            window
        }

        val gamePanel = gameWindow.getChild<DartsGamePanel<*, *>>()

        // Alice - 201
        gamePanel.throwHumanDart(20, SegmentType.OUTER_SINGLE)
        gamePanel.throwHumanDart(20, SegmentType.TREBLE)
        gamePanel.throwHumanDart(20, SegmentType.OUTER_SINGLE)
        gamePanel.confirmRound()

        // Bob - 256
        gamePanel.throwHumanDart(19, SegmentType.OUTER_SINGLE)
        gamePanel.throwHumanDart(19, SegmentType.OUTER_SINGLE)
        gamePanel.throwHumanDart(7, SegmentType.INNER_SINGLE)
        gamePanel.confirmRound()

        // Alice - 106
        gamePanel.throwHumanDart(20, SegmentType.OUTER_SINGLE)
        gamePanel.throwHumanDart(20, SegmentType.TREBLE)
        gamePanel.throwHumanDart(5, SegmentType.TREBLE)
        gamePanel.confirmRound()

        // Bob - 121
        gamePanel.throwHumanDart(19, SegmentType.TREBLE)
        gamePanel.throwHumanDart(19, SegmentType.TREBLE)
        gamePanel.throwHumanDart(7, SegmentType.TREBLE)
        gamePanel.confirmRound()

        // Alice - 80
        gamePanel.throwHumanDart(20, SegmentType.OUTER_SINGLE)
        gamePanel.throwHumanDart(5, SegmentType.OUTER_SINGLE)
        gamePanel.throwHumanDart(1, SegmentType.OUTER_SINGLE)
        gamePanel.confirmRound()

        // Bob - 36
        gamePanel.throwHumanDart(19, SegmentType.TREBLE)
        gamePanel.throwHumanDart(14, SegmentType.INNER_SINGLE)
        gamePanel.throwHumanDart(14, SegmentType.OUTER_SINGLE)
        gamePanel.confirmRound()

        // Alice - 0 - 12 darts.
        gamePanel.throwHumanDart(20, SegmentType.OUTER_SINGLE)
        gamePanel.throwHumanDart(20, SegmentType.OUTER_SINGLE)
        gamePanel.throwHumanDart(20, SegmentType.DOUBLE)
        gamePanel.confirmRound()

        // Bob - resume - 12
        gamePanel.clickChild<JButton> { it.isVisible && it.toolTipText == "Resume throwing" }
        gamePanel.throwHumanDart(16, SegmentType.OUTER_SINGLE)
        gamePanel.throwHumanDart(1, SegmentType.INNER_SINGLE)
        gamePanel.throwHumanDart(7, SegmentType.OUTER_SINGLE)
        gamePanel.confirmRound()

        // Bob - 0 - 14 darts
        gamePanel.throwHumanDart(12, SegmentType.MISS)
        gamePanel.throwHumanDart(12, SegmentType.OUTER_SINGLE)
        gamePanel.confirmRound()

        // Assert some boring stuff etc

        gameWindow.dispose()

        // Check leaderboard
        app.clickChild<JButton>(text = " < Back")
        app.clickChild<JButton>(text = "Leaderboards")
    }

    private fun launchApp(): DartsApp {
        val mainScreen = ScreenCache.mainScreen
        mainScreen.isVisible = true
        mainScreen.init()
        return mainScreen
    }
}
