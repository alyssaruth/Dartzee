package dartzee.achievements.dartzee

import dartzee.achievements.AbstractAchievementTest
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.retrieveAchievement
import dartzee.helper.testRules
import dartzee.utils.Database
import dartzee.utils.insertDartzeeRules
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.sql.Timestamp

class TestAchievementDartzeeBestGame: AbstractAchievementTest<AchievementDartzeeBestGame>()
{
    override fun factoryAchievement() = AchievementDartzeeBestGame()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        insertParticipant(gameId = g.rowId, playerId = p.rowId, finalScore = 127, database = database)

        insertDartzeeRules(g.rowId, testRules, database)
    }

    @Test
    fun `should ignore participants who were part of a team`()
    {
        val pt = insertRelevantParticipant(finalScore = 120, team = true)
        insertDartzeeRules(pt.gameId, testRules)

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `should ignore games with fewer than 5 rounds`()
    {
        val pt = insertRelevantParticipant(finalScore = 120)

        val shortList = testRules.subList(0, DARTZEE_ACHIEVEMENT_MIN_ROUNDS - 2)
        insertDartzeeRules(pt.gameId, shortList)

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `should ignore unfinished participants`()
    {
        val pt = insertRelevantParticipant(finalScore = -1)
        insertDartzeeRules(pt.gameId, testRules)

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should compute score as average per round, rounded down`()
    {
        setUpAchievementRowForPlayer(insertPlayer())

        factoryAchievement().populateForConversion(emptyList())
        retrieveAchievement().achievementCounter shouldBe 25
    }

    @Test
    fun `Should arrive at the correct average for longer games`()
    {
        val pt = insertRelevantParticipant(finalScore = 120)
        val rules = testRules + testRules
        insertDartzeeRules(pt.gameId, rules)

        factoryAchievement().populateForConversion(emptyList())
        retrieveAchievement().achievementCounter shouldBe 13
    }

    @Test
    fun `Should take the earliest game with the best computed score`()
    {
        val player = insertPlayer()
        setUpGame(player, 100, Timestamp(100))
        val expectedGameId = setUpGame(player, 120, Timestamp(150))
        setUpGame(player, 120, Timestamp(200))
        setUpGame(player, 121, Timestamp(250))

        factoryAchievement().populateForConversion(emptyList())

        val achievement = retrieveAchievement()
        achievement.gameIdEarned shouldBe expectedGameId
        achievement.dtAchieved shouldBe Timestamp(150)
        achievement.achievementCounter shouldBe 24
        achievement.playerId shouldBe player.rowId
    }

    private fun setUpGame(player: PlayerEntity, finalScore: Int, dtFinished: Timestamp): String
    {
        val g = insertRelevantGame()
        insertParticipant(playerId = player.rowId, gameId = g.rowId, finalScore = finalScore, dtFinished = dtFinished)
        insertDartzeeRules(g.rowId, testRules)

        return g.rowId
    }
}