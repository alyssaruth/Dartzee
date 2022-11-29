package dartzee.achievements.x01

import dartzee.achievements.AbstractAchievementTest
import dartzee.core.util.getSqlDateNow
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.*
import dartzee.utils.Database
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.sql.Timestamp

class TestAchievementX01BestFinish: AbstractAchievementTest<AchievementX01BestFinish>()
{
    override fun factoryAchievement() = AchievementX01BestFinish()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        insertParticipant(playerId = p.rowId, gameId = g.rowId, dtFinished = getSqlDateNow(), database = database)
        insertFinishForPlayer(p, 60, game = g, database = database)
    }

    @Test
    fun `Should ignore darts that are not doubles`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, dtFinished = getSqlDateNow())

        insertDart(pt, ordinal = 1, startingScore = 40, score = 20, multiplier = 1)
        insertDart(pt, ordinal = 2, startingScore = 20, score = 20, multiplier = 1)

        factoryAchievement().populateForConversion(emptyList())

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

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should take the earliest occurrence of a players best finish`()
    {
        val p = insertPlayer()

        val game = insertFinishForPlayer(p, 30, dtCreation = Timestamp(500))
        insertFinishForPlayer(p, 30, dtCreation = Timestamp(2000))

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 1
        val a = retrieveAchievement()
        a.playerId shouldBe p.rowId
        a.achievementCounter shouldBe 30
        a.gameIdEarned shouldBe game.rowId
        a.dtAchieved shouldBe Timestamp(500)
    }

    @Test
    fun `Should populate with the highest finish`()
    {
        val p = insertPlayer()

        insertFinishForPlayer(p, 55, dtCreation = Timestamp(500))
        val game = insertFinishForPlayer(p, 68, dtCreation = Timestamp(2000))

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 1
        val a = retrieveAchievement()
        a.playerId shouldBe p.rowId
        a.achievementCounter shouldBe 68
        a.gameIdEarned shouldBe game.rowId
        a.dtAchieved shouldBe Timestamp(2000)
    }
}