package dartzee.achievements

import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.getCountFromTable
import dartzee.helper.insertAchievement
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.utils.Database
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp

abstract class TestAbstractAchievementGamesWon<E: AbstractAchievementGamesWon>: AbstractMultiRowAchievementTest<E>()
{
    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        insertParticipant(gameId = g.rowId, playerId = p.rowId, finishingPosition = 1, database = database)
    }

    @Test
    fun `Should ignore participants who did not come 1st`()
    {
        val alice = insertPlayer(name = "Alice")
        val game = insertRelevantGame()
        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finishingPosition = 2)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should insert a row per player and game, and take their latest finish date as DtLastUpdate`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        val pt1 = insertParticipant(gameId = insertRelevantGame().rowId, playerId = alice.rowId, finishingPosition = 1, dtFinished = Timestamp(500), finalScore = 20)
        val pt2 = insertParticipant(gameId = insertRelevantGame().rowId, playerId = alice.rowId, finishingPosition = 1, dtFinished = Timestamp(1500), finalScore = 45)
        val pt3 = insertParticipant(gameId = insertRelevantGame().rowId, playerId = alice.rowId, finishingPosition = 1, dtFinished = Timestamp(1000), finalScore = 26)

        insertParticipant(gameId = insertRelevantGame().rowId, playerId = bob.rowId, finishingPosition = 1, dtFinished = Timestamp(2000))
        insertParticipant(gameId = insertRelevantGame().rowId, playerId = bob.rowId, finishingPosition = 1, dtFinished = Timestamp(1000))

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 5
        val achievementRows = AchievementEntity().retrieveEntities("")
        val aliceRows = achievementRows.filter { it.playerId == alice.rowId }
        aliceRows.size shouldBe 3
        val gameIdAndScore = aliceRows.map { Pair(it.gameIdEarned, it.achievementDetail) }
        gameIdAndScore.shouldContainExactlyInAnyOrder(
            Pair(pt1.gameId, "20"),
            Pair(pt2.gameId, "45"),
            Pair(pt3.gameId, "26")
        )

        val bobRow = achievementRows.filter { it.playerId == bob.rowId }
        bobRow.size shouldBe 2
    }
}