package dartzee.achievements.dartzee

import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.achievements.AchievementType
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.retrieveAchievement
import dartzee.utils.Database
import dartzee.utils.insertDartzeeRules
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.sql.Timestamp

class TestAchievementDartzeeBingo: AbstractMultiRowAchievementTest<AchievementDartzeeBingo>()
{
    override fun factoryAchievement() = AchievementDartzeeBingo()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        insertParticipant(gameId = g.rowId, playerId = p.rowId, finalScore = 275, database = database)
        insertDartzeeRules(g.rowId, testRules, database)
    }

    @Test
    fun `Should ignore participants who were part of a team`()
    {
        val pt = insertRelevantParticipant(finalScore = 120, team = true)
        insertDartzeeRules(pt.gameId, testRules)

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `should ignore games with fewer than 5 rules`()
    {
        val pt = insertRelevantParticipant(finalScore = 120)

        val shortList = testRules.subList(0, DARTZEE_ACHIEVEMENT_MIN_RULES - 1)
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
    fun `should insert one row for the earliest example of a particular score`()
    {
        val p = insertPlayer()
        val g1 = insertRelevantGame()
        val g2 = insertRelevantGame()
        val g3 = insertRelevantGame()
        insertDartzeeRules(g1.rowId, testRules)
        insertDartzeeRules(g2.rowId, testRules)
        insertDartzeeRules(g3.rowId, testRules)

        insertParticipant(gameId = g1.rowId, playerId = p.rowId, finalScore = 107, dtFinished = Timestamp(500))
        insertParticipant(gameId = g2.rowId, playerId = p.rowId, finalScore = 7, dtFinished = Timestamp(1000))
        insertParticipant(gameId = g3.rowId, playerId = p.rowId, finalScore = 307, dtFinished = Timestamp(100))

        factoryAchievement().populateForConversion(emptyList())

        getAchievementCount() shouldBe 1
        val a = retrieveAchievement()
        a.achievementType shouldBe AchievementType.DARTZEE_BINGO
        a.gameIdEarned shouldBe g3.rowId
        a.achievementCounter shouldBe 7
        a.achievementDetail shouldBe "307"
        a.dtAchieved shouldBe Timestamp(100)
    }

    @Test
    fun `should insert a row per unique score`()
    {
        val p = insertPlayer()
        val g1 = insertRelevantGame()
        val g2 = insertRelevantGame()
        val g3 = insertRelevantGame()
        insertDartzeeRules(g1.rowId, testRules)
        insertDartzeeRules(g2.rowId, testRules)
        insertDartzeeRules(g3.rowId, testRules)

        insertParticipant(gameId = g1.rowId, playerId = p.rowId, finalScore = 6, dtFinished = Timestamp(500))
        insertParticipant(gameId = g2.rowId, playerId = p.rowId, finalScore = 36, dtFinished = Timestamp(1000))
        insertParticipant(gameId = g3.rowId, playerId = p.rowId, finalScore = 247, dtFinished = Timestamp(100))

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 3
    }
}