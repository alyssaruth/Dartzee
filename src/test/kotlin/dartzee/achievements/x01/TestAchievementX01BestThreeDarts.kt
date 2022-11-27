package dartzee.achievements.x01

import dartzee.achievements.AbstractAchievementTest
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.retrieveAchievement
import dartzee.utils.Database
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.sql.Timestamp

class TestAchievementX01BestThreeDarts: AbstractAchievementTest<AchievementX01BestThreeDarts>()
{
    override fun factoryAchievement() = AchievementX01BestThreeDarts()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, database = database)

        insertDart(pt, ordinal = 1, startingScore = 501, score = 20, multiplier = 3, database = database)
        insertDart(pt, ordinal = 2, startingScore = 441, score = 20, multiplier = 3, database = database)
        insertDart(pt, ordinal = 3, startingScore = 381, score = 20, multiplier = 3, database = database)
    }

    @Test
    fun `Should include rounds that were thrown as part of a team`()
    {
        val pt = insertRelevantParticipant(team = true)

        insertDart(pt, ordinal = 1, startingScore = 501, score = 20, multiplier = 3)
        insertDart(pt, ordinal = 2, startingScore = 441, score = 20, multiplier = 3)
        insertDart(pt, ordinal = 3, startingScore = 381, score = 20, multiplier = 3)

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 1
    }


    @Test
    fun `Should ignore busts`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, ordinal = 1, startingScore = 100, score = 20, multiplier = 3)
        insertDart(pt, ordinal = 2, startingScore = 40, score = 20, multiplier = 1)
        insertDart(pt, ordinal = 3, startingScore = 20, score = 20, multiplier = 1)

        factoryAchievement().populateForConversion(emptyList())

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore rounds of fewer than 3 darts`()
    {
        val pt = setUpParticipant()

        insertDart(pt, ordinal = 1, startingScore = 501, score = 20, multiplier = 3)
        insertDart(pt, ordinal = 2, startingScore = 441, score = 20, multiplier = 3)

        factoryAchievement().populateForConversion(emptyList())

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should pick the earliest instance of the same three dart score`()
    {
        val p = insertPlayer()

        val pt = setUpParticipant(p)
        insertDart(pt, ordinal = 1, startingScore = 501, score = 20, multiplier = 3)
        insertDart(pt, ordinal = 2, startingScore = 441, score = 20, multiplier = 3)
        insertDart(pt, ordinal = 3, startingScore = 381, score = 20, multiplier = 3, dtCreation = Timestamp(1000))

        val pt2 = setUpParticipant(p)
        insertDart(pt2, ordinal = 1, startingScore = 501, score = 20, multiplier = 3)
        insertDart(pt2, ordinal = 2, startingScore = 441, score = 20, multiplier = 3)
        insertDart(pt2, ordinal = 3, startingScore = 381, score = 20, multiplier = 3, dtCreation = Timestamp(500))

        factoryAchievement().populateForConversion(emptyList())

        getAchievementCount() shouldBe 1

        val a = retrieveAchievement()
        a.playerId shouldBe p.rowId
        a.gameIdEarned shouldBe pt2.gameId
        a.achievementCounter shouldBe 180
        a.achievementDetail shouldBe ""
        a.dtAchieved shouldBe Timestamp(500)
    }

    @Test
    fun `Should return the players highest three darts`()
    {
        val p = insertPlayer()

        //140
        val pt = setUpParticipant(p)
        insertDart(pt, ordinal = 1, startingScore = 501, score = 20, multiplier = 3)
        insertDart(pt, ordinal = 2, startingScore = 441, score = 20, multiplier = 3)
        insertDart(pt, ordinal = 3, startingScore = 381, score = 20, multiplier = 1)

        //101
        val pt2 = setUpParticipant(p)
        insertDart(pt2, ordinal = 1, startingScore = 501, score = 17, multiplier = 3)
        insertDart(pt2, ordinal = 2, startingScore = 450, score = 19, multiplier = 2)
        insertDart(pt2, ordinal = 3, startingScore = 412, score = 12, multiplier = 1)

        //150
        val pt3 = setUpParticipant(p)
        insertDart(pt3, ordinal = 1, startingScore = 501, score = 25, multiplier = 2)
        insertDart(pt3, ordinal = 2, startingScore = 451, score = 25, multiplier = 2)
        insertDart(pt3, ordinal = 3, startingScore = 401, score = 25, multiplier = 2)

        factoryAchievement().populateForConversion(emptyList())

        getAchievementCount() shouldBe 1

        val a = retrieveAchievement()
        a.achievementCounter shouldBe 150
        a.gameIdEarned shouldBe pt3.gameId
        a.playerId shouldBe p.rowId
    }

    private fun setUpParticipant(p: PlayerEntity = insertPlayer()): ParticipantEntity
    {
        val g = insertRelevantGame()

        return insertParticipant(playerId = p.rowId, gameId = g.rowId)
    }
}