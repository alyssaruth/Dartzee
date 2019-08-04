package burlton.dartzee.test.screen.stats.overall

import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.stats.overall.LeaderboardTopX01Finishes
import burlton.dartzee.code.utils.PREFERENCES_INT_LEADERBOARD_SIZE
import burlton.dartzee.code.utils.PreferenceUtil
import burlton.dartzee.test.helper.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test

class TestLeaderboardTopX01Finishes: AbstractRegistryTest()
{
    override fun getPreferencesAffected() = listOf(PREFERENCES_INT_LEADERBOARD_SIZE)

    @Test
    fun `Should count finishes with any remainder of 3`()
    {
        val p = insertPlayer(name = "Clive")
        val g1 = insertFinishForPlayer(p, 141, 9, 3)
        val g2 = insertFinishForPlayer(p, 120, 10, 4)
        val g3 = insertFinishForPlayer(p, 110, 11, 4)

        val leaderboard = LeaderboardTopX01Finishes()
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 3

        leaderboard.getNameAt(0) shouldBe "Clive"
        leaderboard.getGameIdAt(0) shouldBe g1.localId
        leaderboard.getGameIdAt(1) shouldBe g2.localId
        leaderboard.getGameIdAt(2) shouldBe g3.localId
        leaderboard.getScoreAt(0) shouldBe 141
    }

    @Test
    fun `Should take the startingScore of the 1st dart in the final round`()
    {
        val p = insertPlayer()

        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = 12)

        insertDart(pt, roundNumber = 4, startingScore = 120, ordinal = 1, score = 20, multiplier = 1)
        insertDart(pt, roundNumber = 4, startingScore = 100, ordinal = 2, score = 20, multiplier = 3)
        insertDart(pt, roundNumber = 4, startingScore = 40, ordinal = 3, score = 20, multiplier = 2)

        val leaderboard = LeaderboardTopX01Finishes()
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 1
        leaderboard.getScoreAt(0) shouldBe 120
    }

    @Test
    fun `Should ignore games of the wrong type`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GAME_TYPE_GOLF)

        insertFinishForPlayer(p, 100, game = g)

        val leaderboard = LeaderboardTopX01Finishes()
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 0
    }

    @Test
    fun `Should ignore unfinished participants`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = -1)

        insertDart(pt, roundNumber = 4, startingScore = 100, ordinal = 1)

        val leaderboard = LeaderboardTopX01Finishes()
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 0
    }

    @Test
    fun `Should respect the preference value for the number of rows to be returned`()
    {
        PreferenceUtil.saveInt(PREFERENCES_INT_LEADERBOARD_SIZE, 2)

        val p = insertPlayer()

        insertFinishForPlayer(p, 100)
        insertFinishForPlayer(p, 150)
        insertFinishForPlayer(p, 90)

        val leaderboard = LeaderboardTopX01Finishes()
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 2
        leaderboard.getScoreAt(0) shouldBe 150
        leaderboard.getScoreAt(1) shouldBe 100

        PreferenceUtil.saveInt(PREFERENCES_INT_LEADERBOARD_SIZE, 3)
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 3
    }

    @Test
    fun `Should respond to changing player filters`()
    {
        val robot = insertPlayer(name = "Robot", strategy = 1)
        val human = insertPlayer(name = "Human", strategy = -1)

        insertFinishForPlayer(robot, 75)
        insertFinishForPlayer(human, 83)

        val leaderboard = LeaderboardTopX01Finishes()
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 2
        leaderboard.getNameAt(0) shouldBe "Human"
        leaderboard.getNameAt(1) shouldBe "Robot"

        leaderboard.panelPlayerFilters.rdbtnAi.doClick()
        leaderboard.rowCount() shouldBe 1
        leaderboard.getNameAt(0) shouldBe "Robot"

        leaderboard.panelPlayerFilters.rdbtnHuman.doClick()
        leaderboard.rowCount() shouldBe 1
        leaderboard.getNameAt(0) shouldBe "Human"
    }

    @Test
    fun `Should not leave temp tables lying around`()
    {
        val player = insertPlayer()
        insertFinishForPlayer(player, 50)

        LeaderboardTopX01Finishes().buildTable()

        dropUnexpectedTables().shouldBeEmpty()
    }

    private fun insertFinishForPlayer(p: PlayerEntity, finish: Int, numberOfDarts: Int = 15, roundNumber: Int = 5, game: GameEntity = insertRelevantGame()): GameEntity
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = game.rowId, finalScore = numberOfDarts)

        insertDart(pt, roundNumber = roundNumber, startingScore = finish, ordinal = 1)

        return game
    }

    private fun insertRelevantGame() = insertGame(gameType = GAME_TYPE_X01)

    private fun LeaderboardTopX01Finishes.rowCount() = tableTopFinishes.rowCount
    private fun LeaderboardTopX01Finishes.getNameAt(row: Int) = tableTopFinishes.getValueAt(row, 1)
    private fun LeaderboardTopX01Finishes.getGameIdAt(row: Int) = tableTopFinishes.getValueAt(row, 2)
    private fun LeaderboardTopX01Finishes.getScoreAt(row: Int) = tableTopFinishes.getValueAt(row, 3)
}