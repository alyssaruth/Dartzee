package dartzee.e2e

import dartzee.game.GameType
import dartzee.game.prepareParticipants
import dartzee.helper.DEFAULT_X01_CONFIG
import dartzee.helper.beastDartsModel
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.`object`.Dart
import io.kotest.matchers.comparables.shouldBeLessThan
import java.awt.Dimension
import org.junit.jupiter.api.Test

class TestResizeE2E : AbstractE2ETest() {
    @Test
    fun `E2E - Small dartboard`() {
        val game = insertGame(gameType = GameType.X01, gameParams = DEFAULT_X01_CONFIG.toJson())

        val aiModel = beastDartsModel()
        val player = insertPlayer(model = aiModel)

        val participants = prepareParticipants(game.rowId, listOf(player), false)
        val (gamePanel, listener) = setUpGamePanel(game, participants)
        gamePanel.setSize(300, 675)
        gamePanel.preferredSize = Dimension(300, 675)
        gamePanel.startNewGame(participants)

        awaitGameFinish(game)

        gamePanel.dartboard.width shouldBeLessThan 100

        val expectedRounds =
            listOf(
                listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)),
                listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)),
                listOf(Dart(20, 3), Dart(19, 3), Dart(12, 2))
            )

        verifyState(gamePanel, listener, expectedRounds, scoreSuffix = " Darts", finalScore = 9)
    }
}
