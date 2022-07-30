package dartzee.achievements.x01

import dartzee.achievements.AchievementType
import dartzee.achievements.AbstractAchievementTest
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.retrieveAchievement
import dartzee.utils.Database
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementX01HighestBust: AbstractAchievementTest<AchievementX01HighestBust>()
{
    override fun factoryAchievement() = AchievementX01HighestBust()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, database = database)

        insertDart(pt, ordinal = 1, startingScore = 181, score = 20, multiplier = 3, database = database)
        insertDart(pt, ordinal = 2, startingScore = 121, score = 20, multiplier = 3, database = database)
        insertDart(pt, ordinal = 3, startingScore = 61, score = 20, multiplier = 3, database = database)
    }

    @Test
    fun `Should include participants who were part of a team`()
    {
        val pt = insertRelevantParticipant(team = true)

        insertDart(pt, ordinal = 1, roundNumber = 1, startingScore = 40, score = 20, multiplier = 1)
        insertDart(pt, ordinal = 2, roundNumber = 1, startingScore = 20, score = 20, multiplier = 1)

        factoryAchievement().populateForConversion(emptyList())

        getAchievementCount() shouldBe 1
    }

    @Test
    fun `Should create an achievement with the correct fields populated`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, ordinal = 1, roundNumber = 1, startingScore = 40, score = 20, multiplier = 1)
        insertDart(pt, ordinal = 2, roundNumber = 1, startingScore = 20, score = 20, multiplier = 1)

        factoryAchievement().populateForConversion(emptyList())

        val a = retrieveAchievement()
        a.achievementCounter shouldBe 40
        a.gameIdEarned shouldBe pt.gameId
        a.playerId shouldBe pt.playerId
        a.achievementType shouldBe AchievementType.X01_HIGHEST_BUST
    }

    @Test
    fun `Should capture busts where the score was reduced exactly to 0`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, ordinal = 1, roundNumber = 1, startingScore = 18, score = 6, multiplier = 3)

        factoryAchievement().populateForConversion(emptyList())

        val a = retrieveAchievement()
        a.achievementCounter shouldBe 18
    }

    @Test
    fun `Should capture busts where the score was exceeded`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, ordinal = 1, roundNumber = 1, startingScore = 24, score = 20, multiplier = 2)

        factoryAchievement().populateForConversion(emptyList())

        val a = retrieveAchievement()
        a.achievementCounter shouldBe 24
    }

    @Test
    fun `Should capture busts where the score was reduced to exactly 1`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, ordinal = 1, roundNumber = 1, startingScore = 21, score = 20, multiplier = 1)

        factoryAchievement().populateForConversion(emptyList())

        val a = retrieveAchievement()
        a.achievementCounter shouldBe 21
    }

    @Test
    fun `Should not treat a checkout as a bust`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, ordinal = 1, roundNumber = 1, startingScore = 20, score = 10, multiplier = 2)

        factoryAchievement().populateForConversion(emptyList())

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should capture the highest bust`()
    {
        val p = insertPlayer()
        val ptOne = insertRelevantParticipant(p)
        val ptTwo = insertRelevantParticipant(p)
        val ptThree = insertRelevantParticipant(p)

        insertDart(ptOne, ordinal = 1, roundNumber = 1, startingScore = 45, score = 25, multiplier = 2)
        insertDart(ptTwo, ordinal = 1, roundNumber = 1, startingScore = 50, score = 20, multiplier = 3)
        insertDart(ptThree, ordinal = 1, roundNumber = 1, startingScore = 30, score = 20, multiplier = 3)

        factoryAchievement().populateForConversion(emptyList())

        val a = retrieveAchievement()
        a.achievementCounter shouldBe 50
        a.gameIdEarned shouldBe ptTwo.gameId
    }

    @Test
    fun `Should correctly capture a 3 dart bust`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, ordinal = 1, roundNumber = 1, startingScore = 100, score = 20, multiplier = 3)
        insertDart(pt, ordinal = 2, roundNumber = 1, startingScore = 40, score = 20, multiplier = 1)
        insertDart(pt, ordinal = 3, roundNumber = 1, startingScore = 20, score = 15, multiplier = 2)

        factoryAchievement().populateForConversion(emptyList())

        val a = retrieveAchievement()
        a.achievementCounter shouldBe 100
    }
}