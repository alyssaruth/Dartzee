package dartzee.screen.game

import dartzee.achievements.AbstractAchievement
import dartzee.db.GameEntity
import dartzee.game.state.IWrappedParticipant
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings
import java.awt.Dimension
import java.awt.Frame

/** DartsGameScreen Simple screen which wraps up a single game panel */
class DartsGameScreen(game: GameEntity, private val participants: List<IWrappedParticipant>) :
    AbstractDartsGameScreen() {
    private val tutorialPanel = TutorialPanel(this)
    var gamePanel: DartsGamePanel<*, *> = DartsGamePanel.factory(this, game, participants.size)
    override val windowName = gamePanel.gameTitle

    init {
        // Cache this screen in ScreenCache
        val gameId = game.rowId
        ScreenCache.addDartsGameScreen(gameId, this)

        title = gamePanel.gameTitle

        if (InjectedThings.partyMode) {
            contentPane.add(tutorialPanel)
            size = Dimension(1000, 600)
            extendedState = Frame.MAXIMIZED_BOTH
        } else {
            contentPane.add(gamePanel)
        }
    }

    fun startNewGame() {
        if (!InjectedThings.partyMode) {
            gamePanel.startNewGame(participants)
        }
    }

    fun tutorialFinished() {
        contentPane.remove(tutorialPanel)
        contentPane.add(gamePanel)
        repaint()

        gamePanel.startNewGame(participants)
    }

    override fun fireAppearancePreferencesChanged() {
        gamePanel.fireAppearancePreferencesChanged()
    }

    override fun achievementUnlocked(
        gameId: String,
        playerId: String,
        achievement: AbstractAchievement
    ) {
        gamePanel.achievementUnlocked(playerId, achievement)
    }
}
