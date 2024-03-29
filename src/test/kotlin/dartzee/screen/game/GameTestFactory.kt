package dartzee.screen.game

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.AchievementType
import dartzee.ai.DartsAiModel
import dartzee.db.DartsMatchEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.ClockType
import dartzee.game.GameType
import dartzee.game.RoundTheClockConfig
import dartzee.game.X01Config
import dartzee.game.prepareParticipants
import dartzee.game.state.AbstractPlayerState
import dartzee.game.state.IWrappedParticipant
import dartzee.game.state.SingleParticipant
import dartzee.game.state.TeamParticipant
import dartzee.helper.DEFAULT_X01_CONFIG
import dartzee.helper.insertDartsMatch
import dartzee.helper.insertGame
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.insertTeam
import dartzee.helper.randomGuid
import dartzee.`object`.Dart
import dartzee.screen.game.golf.GamePanelGolf
import dartzee.screen.game.rtc.GamePanelRoundTheClock
import dartzee.screen.game.x01.GamePanelX01
import dartzee.screen.game.x01.GameStatisticsPanelX01

fun makeSingleParticipant(player: PlayerEntity, gameId: String? = null) =
    makeSingleParticipant(
        insertParticipant(playerId = player.rowId, gameId = gameId ?: insertGame().rowId)
    )

fun makeSingleParticipant(pt: ParticipantEntity = insertParticipant()) = SingleParticipant(pt)

fun makeTeam(vararg players: PlayerEntity, gameId: String = randomGuid()): TeamParticipant {
    val team = insertTeam(gameId = gameId)
    val pts = players.map { insertParticipant(playerId = it.rowId, gameId = gameId) }
    return TeamParticipant(team, pts)
}

fun makeGolfGamePanel(currentPlayerId: String = randomGuid(), gameParams: String = "18") =
    GamePanelGolf(
            FakeDartsScreen(),
            insertGame(gameType = GameType.GOLF, gameParams = gameParams),
            1
        )
        .apply { testInit(currentPlayerId) }

fun makeGolfGamePanel(
    players: List<PlayerEntity>,
    teamMode: Boolean,
    gameParams: String
): GamePanelGolf {
    val g = insertGame(gameType = GameType.GOLF, gameParams = gameParams)
    val participants = prepareParticipants(g.rowId, players, teamMode)

    val panel = GamePanelGolf(FakeDartsScreen(), g, participants.size)
    panel.startNewGame(participants)
    return panel
}

fun makeX01GamePanel(
    currentPlayerId: String = randomGuid(),
    gameParams: X01Config = DEFAULT_X01_CONFIG
) =
    GamePanelX01(
            FakeDartsScreen(),
            insertGame(gameType = GameType.X01, gameParams = gameParams.toJson()),
            1
        )
        .apply { testInit(currentPlayerId) }

fun makeX01GamePanel(pt: IWrappedParticipant, gameParams: X01Config = DEFAULT_X01_CONFIG) =
    GamePanelX01(
            FakeDartsScreen(),
            insertGame(gameType = GameType.X01, gameParams = gameParams.toJson()),
            1
        )
        .apply { testInit(pt) }

fun makeRoundTheClockGamePanel(playerId: String = randomGuid()) =
    GamePanelRoundTheClock(
            FakeDartsScreen(),
            insertGame(
                gameType = GameType.ROUND_THE_CLOCK,
                gameParams = RoundTheClockConfig(ClockType.Standard, true).toJson()
            ),
            1
        )
        .apply { testInit(playerId) }

fun makeRoundTheClockGamePanel(pt: IWrappedParticipant) =
    GamePanelRoundTheClock(
            FakeDartsScreen(),
            insertGame(
                gameType = GameType.ROUND_THE_CLOCK,
                gameParams = RoundTheClockConfig(ClockType.Standard, true).toJson()
            ),
            1
        )
        .apply { testInit(pt) }

fun DartsGamePanel<*, *>.testInit(playerId: String) {
    val player = insertPlayer(playerId)
    val pt = makeSingleParticipant(player)
    startNewGame(listOf(pt))
}

fun DartsGamePanel<*, *>.testInit(pt: IWrappedParticipant) {
    startNewGame(listOf(pt))
}

fun DartsGamePanel<*, *>.setDartsThrown(dartsThrown: List<Dart>) {
    btnReset.doClick()
    dartsThrown.forEach(::dartThrown)
}

fun DartsGamePanel<*, *>.addCompletedRound(vararg dartsThrown: Dart) {
    addCompletedRound(dartsThrown.toList())
}

fun DartsGamePanel<*, *>.addCompletedRound(dartsThrown: List<Dart>) {
    setDartsThrown(dartsThrown)
    btnConfirm.doClick()
}

fun <PlayerState : AbstractPlayerState<PlayerState>> DartsGamePanel<*, PlayerState>
    .updateAchievementsForFinish(finishingPosition: Int, score: Int) {
    val state = getPlayerStates().first()
    state.participantFinished(finishingPosition, score)
    updateAchievementsForFinish(state, score)
}

fun DartsGamePanel<*, *>.doAiTurn(model: DartsAiModel) {
    val pt = computeAiDart(model) ?: return
    dartboard.dartThrown(pt)
}

fun makeMatchSummaryPanel(
    match: DartsMatchEntity = insertDartsMatch(),
    statsPanel: GameStatisticsPanelX01 = GameStatisticsPanelX01(DEFAULT_X01_CONFIG.toJson())
) = MatchSummaryPanel(match, statsPanel)

class FakeDartsScreen : AbstractDartsGameScreen() {
    override val windowName = "Fake"

    var gameId: String? = null
    var playerId: String? = null
    var achievementType: AchievementType? = null
    var attainedValue: Int? = null

    override fun achievementUnlocked(
        gameId: String,
        playerId: String,
        achievement: AbstractAchievement
    ) {
        this.gameId = gameId
        this.playerId = playerId
        this.achievementType = achievement.achievementType
        this.attainedValue = achievement.attainedValue
    }

    override fun fireAppearancePreferencesChanged() {}
}
