package dartzee.e2e

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.clickYes
import com.github.alyssaburlton.swingtest.waitForAssertion
import dartzee.game.FinishType
import dartzee.game.GameType
import dartzee.game.X01Config
import dartzee.game.state.IWrappedParticipant
import dartzee.getQuestionDialog
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.`object`.SegmentType
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import javax.swing.JButton
import org.junit.jupiter.api.Test

class TestResignationE2E : AbstractE2ETest() {
    @Test
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

        gamePanel.awaitTurn(ptResignee)
        gamePanel.throwHumanDart(20, SegmentType.OUTER_SINGLE)
        gamePanel.throwHumanDart(5, SegmentType.OUTER_SINGLE)
        gamePanel.throwHumanDart(1, SegmentType.OUTER_SINGLE)
        gamePanel.clickChild<JButton> { it.toolTipText == "Confirm round" }

        gamePanel.awaitTurn(ptResignee)
        gamePanel.clickChild<JButton>(async = true) { it.toolTipText == "Resign" }
        getQuestionDialog().clickYes()

        awaitGameFinish(game)
        waitForAssertion { ptLoser.participant.isActive() shouldBe false }

        verifyFinishingPositions(ptWinner, ptLoser, ptResignee)
    }

    @Test
    fun `Resigning mid-game - Golf`() {
        val game = insertGame(gameType = GameType.GOLF, gameParams = "9")

        val (gamePanel) = setUpGamePanel(game, 3)

        val (winner, loser) = createPlayers()
        val resignee = insertPlayer(strategy = "")

        val (ptWinner, ptResignee, ptLoser) = gamePanel.startGame(listOf(winner, resignee, loser))

        gamePanel.awaitTurn(ptResignee)
        gamePanel.throwHumanDart(1, SegmentType.OUTER_SINGLE)
        gamePanel.clickChild<JButton> { it.toolTipText == "Confirm round" }

        gamePanel.awaitTurn(ptResignee)
        gamePanel.clickChild<JButton>(async = true) { it.toolTipText == "Resign" }
        getQuestionDialog().clickYes()

        awaitGameFinish(game)

        verifyFinishingPositions(ptWinner, ptLoser, ptResignee)
    }

    private fun verifyFinishingPositions(
        winner: IWrappedParticipant,
        loser: IWrappedParticipant,
        resignee: IWrappedParticipant
    ) {
        winner.participant.finishingPosition shouldBe 1
        winner.participant.resigned shouldBe false
        winner.participant.finalScore shouldBeGreaterThan -1

        loser.participant.finishingPosition shouldBe 2
        loser.participant.resigned shouldBe false
        loser.participant.finalScore shouldBeGreaterThan -1

        resignee.participant.finishingPosition shouldBe 3
        resignee.participant.resigned shouldBe true
        resignee.participant.finalScore shouldBe -1
    }
}
