package dartzee.achievements

import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.getCountFromTable
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp

abstract class TestAbstractAchievementGamesWon<E: AbstractAchievementGamesWon>: AbstractAchievementTest<E>()
{
    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        insertParticipant(gameId = g.rowId, playerId = p.rowId, finishingPosition = 1)
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
    fun `Should group by player, and take their latest finish date as DtLastUpdate`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        val game = insertRelevantGame()

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finishingPosition = 1, dtFinished = Timestamp(500))
        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finishingPosition = 1, dtFinished = Timestamp(1500))
        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finishingPosition = 1, dtFinished = Timestamp(1000))

        insertParticipant(gameId = game.rowId, playerId = bob.rowId, finishingPosition = 1, dtFinished = Timestamp(2000))
        insertParticipant(gameId = game.rowId, playerId = bob.rowId, finishingPosition = 1, dtFinished = Timestamp(1000))

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 2
        val achievementRows = AchievementEntity().retrieveEntities("")
        val aliceRow = achievementRows.find{ it.playerId == alice.rowId }!!
        aliceRow.achievementCounter shouldBe 3
        aliceRow.dtLastUpdate shouldBe Timestamp(1500)
        aliceRow.gameIdEarned shouldBe ""
        aliceRow.achievementDetail shouldBe ""

        val bobRow = achievementRows.find{ it.playerId == bob.rowId }!!
        bobRow.achievementCounter shouldBe 2
        bobRow.dtLastUpdate shouldBe Timestamp(2000)
        bobRow.gameIdEarned shouldBe ""
        bobRow.achievementDetail shouldBe ""
    }
}