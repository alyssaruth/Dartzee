package dartzee.e2e

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.waitForAssertion
import dartzee.achievements.getAllAchievements
import dartzee.achievements.runConversionsWithProgressBar
import dartzee.ai.AI_DARTBOARD
import dartzee.ai.DartsAiModel
import dartzee.ai.getPointForScore
import dartzee.bean.GameSetupPlayerSelector
import dartzee.bean.ParticipantAvatar
import dartzee.clickButton
import dartzee.core.bean.ScrollTable
import dartzee.core.util.DateStatics
import dartzee.core.util.getSortedValues
import dartzee.db.DartEntity
import dartzee.db.EntityName
import dartzee.db.GameEntity
import dartzee.db.IParticipant
import dartzee.db.PlayerEntity
import dartzee.game.prepareParticipants
import dartzee.game.state.IWrappedParticipant
import dartzee.helper.beastDartsModel
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.helper.makeDartsModel
import dartzee.helper.retrieveAchievementsForPlayer
import dartzee.helper.retrieveParticipant
import dartzee.helper.wipeTable
import dartzee.listener.DartboardListener
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.screen.GameplayDartboard
import dartzee.screen.ScreenCache
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.DartsGamePanel
import dartzee.screen.game.DartsGameScreen
import dartzee.screen.game.scorer.AbstractDartsScorer
import dartzee.screen.game.scorer.AbstractDartsScorerPausable
import dartzee.screen.game.scorer.DartsScorerX01
import dartzee.utils.ResourceCache.ICON_RESUME
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import io.mockk.verifySequence
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JToggleButton
import javax.swing.border.LineBorder

fun AbstractDartsScorerPausable<*>.shouldBePaused() {
    getChild<JButton> { it.icon == ICON_RESUME }
}

fun AbstractDartsScorerPausable<*>.resume() {
    clickChild<JButton> { it.icon == ICON_RESUME }
}

fun AbstractDartsGameScreen.toggleStats() {
    clickChild<JToggleButton> { it.icon == dartzee.utils.ResourceCache.ICON_STATS_LARGE }
}

fun AbstractDartsGameScreen.getScorer(playerName: String) =
    getChild<DartsScorerX01> { it.playerName.contains(playerName) }

data class TwoPlayers(val winner: PlayerEntity, val loser: PlayerEntity)

fun createPlayers(): TwoPlayers {
    val aiModel = beastDartsModel()
    val winner = insertPlayer(model = aiModel, name = "Winner")

    val loserModel = makeDartsModel(standardDeviation = 20.0)
    val loser = insertPlayer(model = loserModel, name = "Loser")

    return TwoPlayers(winner, loser)
}

fun closeOpenGames() {
    ScreenCache.getDartsGameScreens().forEach { it.dispose() }
}

data class GamePanelTestSetup(
    val gamePanel: DartsGamePanel<*, *>,
    val listener: DartboardListener,
    val participants: List<IWrappedParticipant>
)

fun setUpGamePanelAndStartGame(game: GameEntity, players: List<PlayerEntity>): GamePanelTestSetup {
    val participants = prepareParticipants(game.rowId, players, false)
    return setUpGamePanel(game, participants).also { it.gamePanel.startNewGame(participants) }
}

fun setUpGamePanel(game: GameEntity, participants: List<IWrappedParticipant>): GamePanelTestSetup {
    val parentWindow = DartsGameScreen(game, participants)
    parentWindow.isVisible = true
    val gamePanel = parentWindow.gamePanel

    val listener = mockk<DartboardListener>(relaxed = true)
    gamePanel.dartboard.addDartboardListener(listener)

    return GamePanelTestSetup(gamePanel, listener, participants)
}

fun GameSetupPlayerSelector.selectTopPlayer() {
    getChild<ScrollTable>("TableUnselected").selectRow(0)
    clickButton("Select")
}

fun JPanel.throwHumanRound(vararg darts: Dart) {
    darts.forEach { throwHumanDart(it.score, it.segmentType) }

    confirmRound()
}

fun JPanel.throwHumanDart(score: Int, segmentType: SegmentType) {
    val computedPt =
        if (segmentType == SegmentType.MISS) AI_DARTBOARD.getDeliberateMissPoint()
        else {
            val singleTwentyPt = getPointForScore(score, segmentType)
            AI_DARTBOARD.toComputedPoint(singleTwentyPt)
        }

    getChild<GameplayDartboard>().dartThrown(computedPt)
}

fun JPanel.confirmRound() {
    clickChild<JButton> { it.toolTipText == "Confirm round" }
}

fun DartsGamePanel<*, *>.awaitTurn(participant: IWrappedParticipant) {
    val scorer = getChild<AbstractDartsScorer<*>> { it.participant == participant }
    waitForAssertion { scorer.lblAvatar.shouldBeSelected() }
}

fun ParticipantAvatar.shouldBeSelected() = border.shouldBeInstanceOf<LineBorder>()

fun awaitGameFinish(game: GameEntity) {
    waitForAssertion(timeout = 30000) { game.isFinished() shouldBe true }

    // Flush the EDT to ensure UI actions fired off from the AI threads are all completed
    flushEdt()
}

fun makePlayerWithModel(
    model: DartsAiModel,
    name: String = "Clive",
    image: String = "BaboOne"
): PlayerEntity {
    val playerImage = insertPlayerImage(resource = image)

    val player = insertPlayer(playerImageId = playerImage.rowId, name = name)
    return player.toDeterministicPlayer(model)
}

fun verifyState(
    panel: DartsGamePanel<*, *>,
    listener: DartboardListener,
    dartRounds: List<List<Dart>>,
    finalScore: Int,
    scoreSuffix: String = "",
    expectedScorerRows: Int = dartRounds.size,
    pt: IParticipant = retrieveParticipant()
) {
    // ParticipantEntity on the database
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
    verifySequence { darts.forEach { listener.dartThrown(it) } }

    // Check that the dart entities on the database line up
    val dartEntities =
        DartEntity().retrieveEntities().sortedWith(compareBy({ it.roundNumber }, { it.ordinal }))
    val chunkedDartEntities: List<List<DartEntity>> =
        dartEntities
            .groupBy { it.roundNumber }
            .getSortedValues()
            .map { it.sortedBy { drt -> drt.ordinal } }
    val retrievedDartRounds =
        chunkedDartEntities.map { rnd -> rnd.map { drt -> Dart(drt.score, drt.multiplier) } }
    retrievedDartRounds shouldBe dartRounds
}

fun checkAchievementConversions(playerId: String) {
    checkAchievementConversions(listOf(playerId))
}

fun checkAchievementConversions(playerIds: List<String>) {
    val transactionalStates = playerIds.associateWith(::retrieveAchievementsForPlayer)
    wipeTable(EntityName.Achievement)

    val t = runConversionsWithProgressBar(getAllAchievements(), playerIds)
    t.join()

    transactionalStates.forEach { (playerId, transactionalState) ->
        val retrieved = retrieveAchievementsForPlayer(playerId)
        retrieved.shouldContainExactlyInAnyOrder(transactionalState)
    }
}
