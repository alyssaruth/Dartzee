package dartzee.achievements.x01

import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.db.GameEntity
import dartzee.game.GameType
import dartzee.db.PlayerEntity
import dartzee.helper.*
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementX01Btbf: AbstractMultiRowAchievementTest<AchievementX01Btbf>()
{
    override fun factoryAchievement() = AchievementX01Btbf()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        insertSuccessfulParticipant(g, p, database)
    }

    @Test
    fun `Should ignore games that were won on a different double`()
    {
        val g = insertRelevantGame()
        val p = insertPlayer()

        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId, finalScore = 3)
        insertDart(pt, roundNumber = 1, startingScore = 4, score = 2, multiplier = 2)

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore D1s that did not finish the game`()
    {
        val g = insertRelevantGame()
        val p = insertPlayer()

        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId, finalScore = 3)
        insertDart(pt, roundNumber = 1, startingScore = 4, score = 1, multiplier = 2)

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should insert a row for each double 1 achieved`()
    {
        val alice = insertPlayer(name = "Alice")

        val game = insertRelevantGame()

        insertSuccessfulParticipant(game, alice)
        insertSuccessfulParticipant(game, alice)
        insertSuccessfulParticipant(game, alice)

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 3
    }

    private fun insertSuccessfulParticipant(game: GameEntity, player: PlayerEntity, database: Database = mainDatabase)
    {
        val pt = insertParticipant(gameId = game.rowId, playerId = player.rowId, finalScore = 3, database = database)
        insertDart(pt, roundNumber = 1, startingScore = 2, score = 1, multiplier = 2, database = database)
    }
}