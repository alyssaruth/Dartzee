package e2e

import dartzee.ai.AimDart
import dartzee.game.GameType
import dartzee.helper.AbstractRegistryTest
import dartzee.helper.beastDartsModel
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.listener.DartboardListener
import dartzee.`object`.Dart
import dartzee.screen.game.DartsGameScreen
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestResizeE2E : AbstractRegistryTest()
{
    override fun getPreferencesAffected() = listOf(PREFERENCES_INT_AI_SPEED)

    @BeforeEach
    fun beforeEach()
    {
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, 100)
    }

    @Test
    @Tag("e2e")
    fun `E2E - Small dartboard`()
    {
        val game = insertGame(gameType = GameType.X01, gameParams = "501")

        val aiModel = beastDartsModel(hmScoreToDart = mapOf(81 to AimDart(19, 3)))
        val player = insertPlayer(model = aiModel)

        val parentWindow = DartsGameScreen(game, 1)
        parentWindow.setSize(300, 675)
        parentWindow.isVisible = true

        val listener = mockk<DartboardListener>(relaxed = true)
        parentWindow.gamePanel.dartboard.addDartboardListener(listener)
        parentWindow.gamePanel.startNewGame(listOf(player))
        awaitGameFinish(game)

        val expectedRounds = listOf(
            listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)),
            listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)),
            listOf(Dart(20, 3), Dart(19, 3), Dart(12, 2))
        )

        verifyState(parentWindow.gamePanel, listener, expectedRounds, scoreSuffix = " Darts", finalScore = 9)
    }
}