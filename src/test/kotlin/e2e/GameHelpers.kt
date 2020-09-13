package e2e

import com.github.alexburlton.swingtest.clickChild
import com.github.alexburlton.swingtest.getChild
import dartzee.ai.AimDart
import dartzee.db.PlayerEntity
import dartzee.helper.beastDartsModel
import dartzee.helper.insertPlayer
import dartzee.helper.makeDartsModel
import dartzee.screen.ScreenCache
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.scorer.DartsScorerPausable
import dartzee.screen.game.scorer.DartsScorerX01
import dartzee.utils.ResourceCache.ICON_RESUME
import java.awt.Window
import javax.swing.JButton
import javax.swing.JToggleButton
import javax.swing.SwingUtilities

fun DartsScorerPausable.shouldBePaused()
{
    getChild<JButton> { it.icon == ICON_RESUME }
}

fun DartsScorerPausable.resume()
{
    SwingUtilities.invokeAndWait { clickChild<JButton> { it.icon == ICON_RESUME } }
}

fun AbstractDartsGameScreen.toggleStats()
{
    SwingUtilities.invokeAndWait { clickChild<JToggleButton> { it.icon == dartzee.utils.ResourceCache.ICON_STATS_LARGE } }
}

fun AbstractDartsGameScreen.getScorer(playerName: String): DartsScorerX01
{
    return getChild { it.playerName == playerName }
}

data class TwoPlayers(val winner: PlayerEntity, val loser: PlayerEntity)
fun createPlayers(): TwoPlayers
{
    val aiModel = beastDartsModel(hmScoreToDart = mapOf(81 to AimDart(19, 3)))
    val winner = insertPlayer(model = aiModel, name = "Winner")

    val loserModel = makeDartsModel(standardDeviation = 150.0)
    val loser = insertPlayer(model = loserModel, name = "Loser")

    return TwoPlayers(winner, loser)
}

fun closeOpenGames()
{
    ScreenCache.getDartsGameScreens().forEach { it.dispose() }
}

fun getWindow(fn: (window: Window) -> Boolean) = Window.getWindows().find(fn)