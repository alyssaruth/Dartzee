package dartzee.screen.game

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.AchievementType
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.game.state.SingleParticipant
import dartzee.game.state.TeamParticipant
import dartzee.helper.*
import dartzee.`object`.Dart
import dartzee.screen.game.golf.GamePanelGolf

fun makeSingleParticipant(player: PlayerEntity) = makeSingleParticipant(insertParticipant(playerId = player.rowId))
fun makeSingleParticipant(pt: ParticipantEntity = insertParticipant()) = SingleParticipant(pt)

fun makeTeam(vararg players: PlayerEntity): TeamParticipant
{
    val team = insertTeam()
    val pts = players.map { insertParticipant(playerId = it.rowId) }
    return TeamParticipant(team, pts)
}

fun makeGolfGamePanel(currentPlayerId: String = randomGuid()) =
    GamePanelGolf(
        FakeDartsScreen(GameType.GOLF),
        GameEntity.factoryAndSave(GameType.GOLF, "18"),
        1).apply { testInit(currentPlayerId) }

fun DartsGamePanel<*, *, *>.testInit(playerId: String)
{
    val player = insertPlayer(playerId)
    startNewGame(listOf(player))
}

fun DartsGamePanel<*, *, *>.setDartsThrown(dartsThrown: List<Dart>)
{
    btnReset.doClick()
    dartsThrown.forEach { dartThrown(it) }
}

class FakeDartsScreen(gameType: GameType = GameType.X01) : AbstractDartsGameScreen(2, gameType)
{
    override val windowName = "Fake"

    var gameId: String? = null
    var playerId: String? = null
    var achievementType: AchievementType? = null
    var attainedValue: Int? = null

    override fun achievementUnlocked(gameId: String, playerId: String, achievement: AbstractAchievement)
    {
        this.gameId = gameId
        this.playerId = playerId
        this.achievementType = achievement.achievementType
        this.attainedValue = achievement.attainedValue
    }

    override fun fireAppearancePreferencesChanged()
    {

    }
}