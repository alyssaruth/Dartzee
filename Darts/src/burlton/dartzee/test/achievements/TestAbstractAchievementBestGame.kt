package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.AbstractAchievementBestGame
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.helper.*
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp

abstract class TestAbstractAchievementBestGame<E: AbstractAchievementBestGame>: AbstractAchievementTest<E>()
{
    override fun beforeEachTest()
    {
        wipeTable("Achievement")
        wipeTable("Player")
        wipeTable("Game")
        wipeTable("Participant")
    }

    private fun insertRelevantGame(): GameEntity
    {
        return insertGame(gameType = factoryAchievement().gameType, gameParams = factoryAchievement().gameParams)
    }

    override fun setUpAchievementRowForPlayer(p: PlayerEntity)
    {
        val game = insertRelevantGame()

        insertParticipant(gameId = game.rowId, playerId = p.rowId, finalScore = 30)
    }

    @Test
    fun `Should ignore games that are the wrong type`()
    {
        val alice = insertPlayer(name = "Alice")
        val game = insertGame(gameType = 5000, gameParams = factoryAchievement().gameParams)

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 20)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore games that are the wrong params`()
    {
        val alice = insertPlayer(name = "Alice")
        val game = insertGame(gameType = factoryAchievement().gameType, gameParams = "blah")

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 20)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore games where the finalScore is unset`()
    {
        val alice = insertPlayer(name = "Alice")
        val game = insertRelevantGame()

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = -1)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 20)
        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1
        val achievementRow = AchievementEntity().retrieveEntities("")[0]
        achievementRow.achievementCounter shouldBe 20
    }

    @Test
    fun `Should return the lowest scoring game`()
    {
        val alice = insertPlayer(name = "Alice")

        val game = insertRelevantGame()

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 15, dtFinished = Timestamp(1000))
        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 12, dtFinished = Timestamp(1500))

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1
        val achievementRow = AchievementEntity().retrieveEntities("")[0]
        achievementRow.achievementCounter shouldBe 12
    }

    @Test
    fun `Should return the earliest game if there is a tie for best score`()
    {
        val alice = insertPlayer(name = "Alice")

        val game = insertRelevantGame()

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 12, dtFinished = Timestamp(1000))
        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 12, dtFinished = Timestamp(800))
        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 12, dtFinished = Timestamp(1500))

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1
        val achievementRow = AchievementEntity().retrieveEntities("")[0]
        achievementRow.achievementCounter shouldBe 12
        achievementRow.dtLastUpdate shouldBe Timestamp(800)
    }

    @Test
    fun `Should set the correct values on the generated achievement row`()
    {
        val alice = insertPlayer(name = "Alice")

        val game = insertRelevantGame()

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 12, dtFinished = Timestamp(1000))

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1
        val achievementRow = AchievementEntity().retrieveEntities("")[0]
        achievementRow.achievementCounter shouldBe 12
        achievementRow.dtLastUpdate shouldBe Timestamp(1000)
        achievementRow.playerId shouldBe alice.rowId
        achievementRow.gameIdEarned shouldBe game.rowId
        achievementRow.achievementRef shouldBe factoryAchievement().achievementRef
    }
}