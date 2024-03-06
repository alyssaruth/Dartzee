package dartzee.e2e

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.findWindow
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.waitForAssertion
import dartzee.bean.GameSetupPlayerSelector
import dartzee.core.bean.ScrollTable
import dartzee.helper.AbstractTest
import dartzee.helper.preparePlayers
import dartzee.`object`.SegmentType
import dartzee.screen.DartsApp
import dartzee.screen.ScreenCache
import dartzee.screen.game.DartsGamePanel
import dartzee.screen.game.DartsGameScreen
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
        app.clickChild<JButton>(text = "Utilities")
        app.clickChild<JButton>(text = "Enter Party Mode")
        app.clickChild<JButton>(text = "New Game")

        val selector = app.getChild<GameSetupPlayerSelector>()
        selector.getChild<ScrollTable>("TableUnselected").selectRow(0)
        selector.clickChild<JButton>("Select")
        selector.getChild<ScrollTable>("TableUnselected").selectRow(0)
        selector.clickChild<JButton>("Select")

        app.clickChild<JButton>(text = "Launch Game >")

        waitForAssertion {
            val window = findWindow<DartsGameScreen>()
            window.shouldNotBeNull()
            window.shouldBeVisible()
        }

        val gamePanel = findWindow<DartsGameScreen>()!!.getChild<DartsGamePanel<*, *>>()

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

        // Bob -
        gamePanel.throwHumanDart(19, SegmentType.TREBLE)
    }

    private fun launchApp(): DartsApp {
        val mainScreen = ScreenCache.mainScreen
        mainScreen.isVisible = true
        mainScreen.init()
        return mainScreen
    }
}
