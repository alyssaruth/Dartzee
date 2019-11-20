package burlton.dartzee.code.screen.game

import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.screen.game.scorer.DartsScorer

/**
 * DartsGameScreen
 * Simple screen which wraps up either a single game panel, or multiple tabs for a match.
 */
class DartsGameScreen(game: GameEntity, totalPlayers: Int) : AbstractDartsGameScreen(totalPlayers)
{
    var gamePanel: DartsGamePanel<out DartsScorer, out Dartboard> = DartsGamePanel.factory(this, game)

    init
    {
        //Cache this screen in ScreenCache
        val gameId = game.rowId
        ScreenCache.addDartsGameScreen(gameId, this)

        //Initialise some basic properties of the tab, such as visibility of components etc
        gamePanel.initBasic(totalPlayers)

        title = gamePanel.gameTitle

        //Add the single game tab and set visible
        contentPane.add(gamePanel)
    }

    override fun fireAppearancePreferencesChanged()
    {
        gamePanel.fireAppearancePreferencesChanged()
    }

    override fun achievementUnlocked(gameId: String, playerId: String, achievement: AbstractAchievement)
    {
        gamePanel.achievementUnlocked(playerId, achievement)
    }
}
