package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.AbstractAchievementBestGame
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.utils.ResourceCache
import burlton.dartzee.test.helper.*
import io.kotlintest.shouldBe
import org.junit.Test
import java.net.URL
import java.sql.Timestamp

class TestAbstractAchievementBestGame: AbstractDartsTest()
{
    override fun beforeEachTest()
    {
        wipeTable("Achievement")
        wipeTable("Player")
        wipeTable("Game")
        wipeTable("Participant")
    }

    @Test
    fun `Should only generate data for specified players`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        val game = insertGame(gameType = GAME_TYPE_X01, gameParams = "501")

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 30)
        insertParticipant(gameId = game.rowId, playerId = bob.rowId, finalScore = 25)

        MockAchievementBestGame().populateForConversion("'${alice.rowId}'")

        getCountFromTable("Achievement") shouldBe 1
        val achievementRow = AchievementEntity().retrieveEntities("")[0]
        achievementRow.playerId shouldBe alice.rowId
        achievementRow.achievementCounter shouldBe 30
    }

    @Test
    fun `Should generate data for all players by default`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        val game = insertGame(gameType = GAME_TYPE_X01, gameParams = "501")

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 30)
        insertParticipant(gameId = game.rowId, playerId = bob.rowId, finalScore = 25)

        MockAchievementBestGame().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 2
    }

    @Test
    fun `Should ignore games that are the wrong type`()
    {
        val alice = insertPlayer(name = "Alice")
        val game = insertGame(gameType = GAME_TYPE_GOLF, gameParams = "501")

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 20)

        MockAchievementBestGame().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore games that are the wrong params`()
    {
        val alice = insertPlayer(name = "Alice")
        val game = insertGame(gameType = GAME_TYPE_X01, gameParams = "18")

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 20)

        MockAchievementBestGame().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore games where the finalScore is unset`()
    {
        val alice = insertPlayer(name = "Alice")
        val game = insertGame(gameType = GAME_TYPE_X01, gameParams = "501")

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = -1)

        MockAchievementBestGame().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 20)
        MockAchievementBestGame().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1
        val achievementRow = AchievementEntity().retrieveEntities("")[0]
        achievementRow.achievementCounter shouldBe 20
    }

    @Test
    fun `Should return the lowest scoring game`()
    {
        val alice = insertPlayer(name = "Alice")

        val game = insertGame(gameType = GAME_TYPE_X01, gameParams = "501")

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 15, dtFinished = Timestamp(1000))
        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 12, dtFinished = Timestamp(1500))

        MockAchievementBestGame().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1
        val achievementRow = AchievementEntity().retrieveEntities("")[0]
        achievementRow.achievementCounter shouldBe 12
    }

    @Test
    fun `Should return the earliest game if there is a tie for best score`()
    {
        val alice = insertPlayer(name = "Alice")

        val game = insertGame(gameType = GAME_TYPE_X01, gameParams = "501")

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 12, dtFinished = Timestamp(1000))
        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 12, dtFinished = Timestamp(800))
        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 12, dtFinished = Timestamp(1500))

        MockAchievementBestGame().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1
        val achievementRow = AchievementEntity().retrieveEntities("")[0]
        achievementRow.achievementCounter shouldBe 12
        achievementRow.dtLastUpdate shouldBe Timestamp(800)
    }

    @Test
    fun `Should set the correct values on the generated achievement row`()
    {
        val alice = insertPlayer(name = "Alice")

        val game = insertGame(gameType = GAME_TYPE_X01, gameParams = "501")

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 12, dtFinished = Timestamp(1000))

        MockAchievementBestGame().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1
        val achievementRow = AchievementEntity().retrieveEntities("")[0]
        achievementRow.achievementCounter shouldBe 12
        achievementRow.dtLastUpdate shouldBe Timestamp(1000)
        achievementRow.playerId shouldBe alice.rowId
        achievementRow.gameIdEarned shouldBe game.rowId
        achievementRow.achievementRef shouldBe 50
    }

    class MockAchievementBestGame: AbstractAchievementBestGame()
    {
        override val achievementRef = 50
        override val name = "Test"
        override val desc = "foo"

        override val gameType = GAME_TYPE_X01
        override val gameParams = "501"

        override val redThreshold = 99
        override val orangeThreshold = 60
        override val yellowThreshold = 42
        override val greenThreshold = 30
        override val blueThreshold = 24
        override val pinkThreshold = 12
        override val maxValue = 9

        override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_X01_BEST_GAME
    }
}