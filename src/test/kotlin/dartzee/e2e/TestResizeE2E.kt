package dartzee.e2e

import dartzee.ai.AimDart
import dartzee.game.GameType
import dartzee.helper.AbstractRegistryTest
import dartzee.helper.DEFAULT_X01_CONFIG
import dartzee.helper.beastDartsModel
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.`object`.Dart
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import io.kotest.matchers.comparables.shouldBeLessThan
import java.awt.Dimension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestResizeE2E : AbstractRegistryTest() {
    override fun getPreferencesAffected() = listOf(PREFERENCES_INT_AI_SPEED)

    @BeforeEach
    fun beforeEach() {
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, 100)
    }

    @Test
    @Tag("e2e")
    fun `E2E - Small dartboard`() {
        val game = insertGame(gameType = GameType.X01, gameParams = DEFAULT_X01_CONFIG.toJson())

        val aiModel = beastDartsModel(hmScoreToDart = mapOf(81 to AimDart(19, 3)))
        val player = insertPlayer(model = aiModel)

        val (gamePanel, listener) = setUpGamePanel(game)
        gamePanel.setSize(300, 675)
        gamePanel.preferredSize = Dimension(300, 675)
        gamePanel.startGame(listOf(player))

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
