package dartzee.achievements

import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.getCountFromTable
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.utils.Database
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp

abstract class TestAbstractAchievementGamesWon<E: AbstractAchievementGamesWon>: TestAbstractAchievementRowPerGame<E>()
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

        insertParticipant(gameId = insertRelevantGame().rowId, playerId = alice.rowId, finishingPosition = 1, dtFinished = Timestamp(500))
        insertParticipant(gameId = insertRelevantGame().rowId, playerId = alice.rowId, finishingPosition = 1, dtFinished = Timestamp(1500))
        insertParticipant(gameId = insertRelevantGame().rowId, playerId = alice.rowId, finishingPosition = 1, dtFinished = Timestamp(1000))

        insertParticipant(gameId = insertRelevantGame().rowId, playerId = bob.rowId, finishingPosition = 1, dtFinished = Timestamp(2000))
        insertParticipant(gameId = insertRelevantGame().rowId, playerId = bob.rowId, finishingPosition = 1, dtFinished = Timestamp(1000))

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 5
        val achievementRows = AchievementEntity().retrieveEntities("")
        val aliceRows = achievementRows.filter { it.playerId == alice.rowId }
        aliceRows.size shouldBe 3
        aliceRows.forEach {
            it.achievementCounter shouldBe -1
            it.achievementDetail shouldBe ""
        }

        val bobRow = achievementRows.filter { it.playerId == bob.rowId }
        bobRow.size shouldBe 2
    }
}