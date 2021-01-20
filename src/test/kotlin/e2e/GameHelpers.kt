package e2e

import com.github.alexburlton.swingtest.awaitCondition
import com.github.alexburlton.swingtest.clickChild
import com.github.alexburlton.swingtest.getChild
import dartzee.`object`.Dart
import dartzee.ai.AimDart
import dartzee.ai.DartsAiModel
import dartzee.core.util.DateStatics
import dartzee.core.util.getSortedValues
import dartzee.db.DartEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.beastDartsModel
import dartzee.helper.insertPlayer
import dartzee.helper.makeDartsModel
import dartzee.helper.retrieveParticipant
import dartzee.listener.DartboardListener
import dartzee.screen.ScreenCache
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.DartsGamePanel
import dartzee.screen.game.scorer.AbstractDartsScorerPausable
import dartzee.screen.game.scorer.DartsScorerX01
import dartzee.utils.ResourceCache.ICON_RESUME
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import java.awt.Window
import java.util.*
import javax.swing.JButton
import javax.swing.JToggleButton
import javax.swing.SwingUtilities

fun AbstractDartsScorerPausable<*>.shouldBePaused()
{
    getChild<JButton> { it.icon == ICON_RESUME }
}

fun AbstractDartsScorerPausable<*>.resume()
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

    val loserModel = makeDartsModel(standardDeviation = 20.0)
    val loser = insertPlayer(model = loserModel, name = "Loser")

    return TwoPlayers(winner, loser)
}

fun closeOpenGames()
{
    ScreenCache.getDartsGameScreens().forEach { it.dispose() }
}

fun getWindow(fn: (window: Window) -> Boolean) = Window.getWindows().find(fn)

data class GamePanelTestSetup(val gamePanel: DartsGamePanel<*, *, *>, val listener: DartboardListener)

fun setUpGamePanel(game: GameEntity): GamePanelTestSetup
{
    val parentWindow = mockk<AbstractDartsGameScreen>(relaxed = true)
    every { parentWindow.isVisible } returns true

    val panel = DartsGamePanel.factory(parentWindow, game, 1)
    val listener = mockk<DartboardListener>(relaxed = true)
    panel.dartboard.addDartboardListener(listener)

    return GamePanelTestSetup(panel, listener)
}

fun awaitGameFinish(game: GameEntity)
{
    awaitCondition { game.isFinished() }
}

fun makePlayerWithModel(model: DartsAiModel): PlayerEntity
{
    val player = mockk<PlayerEntity>(relaxed = true)
    every { player.getModel() } returns model
    every { player.rowId } returns UUID.randomUUID().toString()
    every { player.isAi() } returns true
    every { player.isHuman() } returns false
    return player

}

fun verifyState(panel: DartsGamePanel<*, *, *>,
                        listener: DartboardListener,
                        dartRounds: List<List<Dart>>,
                        finalScore: Int,
                        scoreSuffix: String = "",
                        expectedScorerRows: Int = dartRounds.size)
{
    // ParticipantEntity on the database
    val pt = retrieveParticipant()
    pt.finalScore shouldBe finalScore
    pt.dtFinished shouldNotBe DateStatics.END_OF_TIME
    pt.gameId shouldBe panel.gameEntity.rowId
    pt.finishingPosition shouldBe -1
    pt.ordinal shouldBe 0

    // Screen state
    panel.scorersOrdered[0].lblResult.text shouldBe "$finalScore$scoreSuffix"
    panel.scorersOrdered[0].tableScores.rowCount shouldBe expectedScorerRows

    // Use our dartboardListener to verify that the right throws were registered
    val darts = dartRounds.flatten()
    verifySequence {
        darts.forEach {
            listener.dartThrown(it)
        }
    }

    // Check that the dart entities on the database line up
    val dartEntities = DartEntity().retrieveEntities().sortedWith(compareBy( { it.roundNumber }, { it.ordinal }))
    dartEntities.forEach {
        it.participantId shouldBe pt.rowId
        it.playerId shouldBe pt.playerId
    }

    val chunkedDartEntities: List<List<DartEntity>> = dartEntities.groupBy { it.roundNumber }.getSortedValues().map { it.sortedBy { drt -> drt.ordinal } }
    val retrievedDartRounds = chunkedDartEntities.map { rnd -> rnd.map { drt -> Dart(drt.score, drt.multiplier) } }
    retrievedDartRounds shouldBe dartRounds
}