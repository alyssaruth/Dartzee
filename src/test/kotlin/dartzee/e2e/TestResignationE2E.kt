package dartzee.e2e

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.clickYes
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.waitForAssertion
import dartzee.game.FinishType
import dartzee.game.GameType
import dartzee.game.X01Config
import dartzee.getQuestionDialog
import dartzee.helper.AbstractRegistryTest
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.`object`.SegmentType
import dartzee.screen.game.scorer.DartsScorerX01
import dartzee.utils.PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import javax.swing.JButton
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestResignationE2E : AbstractRegistryTest() {
    override fun getPreferencesAffected() =
        listOf(PREFERENCES_INT_AI_SPEED, PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE)

    @BeforeEach
    fun beforeEach() {
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, 100)
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE, true)
    }

    @Test
    @Tag("e2e")
    fun `Resigning mid-game - X01`() {
        val game =
            insertGame(
                gameType = GameType.X01,
                gameParams = X01Config(501, FinishType.Doubles).toJson()
            )

        val (gamePanel) = setUpGamePanel(game, 3)

        val (winner, loser) = createPlayers()
        val resignee = insertPlayer(strategy = "")

        val (ptWinner, ptResignee, ptLoser) = gamePanel.startGame(listOf(winner, resignee, loser))

        val resigneeScorer = gamePanel.getChild<DartsScorerX01> { it.participant == ptResignee }

        waitForAssertion { resigneeScorer.lblAvatar.shouldBeSelected() }
        gamePanel.throwHumanDart(20, SegmentType.OUTER_SINGLE)
        gamePanel.throwHumanDart(5, SegmentType.OUTER_SINGLE)
        gamePanel.throwHumanDart(1, SegmentType.OUTER_SINGLE)
        gamePanel.clickChild<JButton> { it.toolTipText == "Confirm round" }

        waitForAssertion { resigneeScorer.lblAvatar.shouldBeSelected() }
        gamePanel.clickChild<JButton>(async = true) { it.toolTipText == "Resign" }
        getQuestionDialog().clickYes()

        awaitGameFinish(game)
    }
}
