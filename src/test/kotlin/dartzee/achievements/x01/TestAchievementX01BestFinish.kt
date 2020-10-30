package dartzee.achievements.x01

import dartzee.achievements.AbstractAchievementTest
import dartzee.core.util.DateStatics
import dartzee.core.util.getSqlDateNow
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.*
import dartzee.utils.Database
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp

class TestAchievementX01BestFinish: AbstractAchievementTest<AchievementX01BestFinish>()
{
    override fun factoryAchievement() = AchievementX01BestFinish()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, dtFinished = getSqlDateNow(), database = database)

        insertDart(pt, ordinal = 1, startingScore = 60, score = 20, multiplier = 1, database = database)
        insertDart(pt, ordinal = 2, startingScore = 40, score = 20, multiplier = 2, database = database)
    }

    @Test
    fun `Should ignore unfinished participants`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, dtFinished = DateStatics.END_OF_TIME)

        insertDart(pt, ordinal = 1, startingScore = 60, score = 20, multiplier = 1)
        insertDart(pt, ordinal = 2, startingScore = 40, score = 20, multiplier = 2)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore darts that are not doubles`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, dtFinished = getSqlDateNow())

        insertDart(pt, ordinal = 1, startingScore = 40, score = 20, multiplier = 1)
        insertDart(pt, ordinal = 2, startingScore = 20, score = 20, multiplier = 1)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore doubles that are not finishes`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, dtFinished = getSqlDateNow())

        insertDart(pt, ordinal = 1, startingScore = 100, score = 20, multiplier = 1)
        insertDart(pt, ordinal = 2, startingScore = 80, score = 20, multiplier = 2)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should take the earliest occurrence of a players best finish`()
    {
        val p = insertPlayer()

        val g1 = insertRelevantGame()
        val g2 = insertRelevantGame()

        val pt1 = insertParticipant(playerId = p.rowId, gameId = g1.rowId, dtFinished = Timestamp(500))
        val pt2 = insertParticipant(playerId = p.rowId, gameId = g2.rowId, dtFinished = Timestamp(2000))


        insertDart(pt1, ordinal = 1, startingScore = 30, score = 15, multiplier = 2)
        insertDart(pt2, ordinal = 1, startingScore = 30, score = 15, multiplier = 2)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1
        val a = retrieveAchievement()
        a.playerId shouldBe p.rowId
        a.achievementCounter shouldBe 30
        a.gameIdEarned shouldBe g1.rowId
        a.dtLastUpdate shouldBe Timestamp(500)
    }

    @Test
    fun `Should populate with the highest finish`()
    {
        val p = insertPlayer()

        val g1 = insertRelevantGame()
        val g2 = insertRelevantGame()

        val pt1 = insertParticipant(playerId = p.rowId, gameId = g1.rowId, dtFinished = Timestamp(500))
        val pt2 = insertParticipant(playerId = p.rowId, gameId = g2.rowId, dtFinished = Timestamp(2000))


        //55 finish in two darts
        insertDart(pt1, ordinal = 1, startingScore = 55, score = 15, multiplier = 1)
        insertDart(pt1, ordinal = 2, startingScore = 40, score = 20, multiplier = 2)

        //68 finish in three darts
        insertDart(pt2, ordinal = 1, startingScore = 68, score = 18, multiplier = 1)
        insertDart(pt2, ordinal = 2, startingScore = 50, score = 3, multiplier = 0)
        insertDart(pt2, ordinal = 3, startingScore = 50, score = 25, multiplier = 2)


        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1
        val a = retrieveAchievement()
        a.playerId shouldBe p.rowId
        a.achievementCounter shouldBe 68
        a.gameIdEarned shouldBe g2.rowId
        a.dtLastUpdate shouldBe Timestamp(2000)
    }
}