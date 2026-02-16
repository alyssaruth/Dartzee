package dartzee.achievements

import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.helper.getCountFromTable
import dartzee.helper.insertGame
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.insertTeam
import dartzee.helper.retrieveAchievement
import dartzee.utils.Database
import io.kotest.matchers.shouldBe
import java.sql.Timestamp
import org.junit.jupiter.api.Test

abstract class TestAbstractAchievementBestGame<E : AbstractAchievementBestGame> :
    AbstractAchievementTest<E>() {
    override fun insertRelevantGame(dtLastUpdate: Timestamp, database: Database): GameEntity {
        return insertGame(
            gameType = factoryAchievement().gameType!!,
            gameParams = factoryAchievement().gameParams,
            dtLastUpdate = dtLastUpdate,
            database = database,
        )
    }

    override fun setUpAchievementRowForPlayerAndGame(
        p: PlayerEntity,
        g: GameEntity,
        database: Database,
    ) {
        insertParticipant(
            gameId = g.rowId,
            playerId = p.rowId,
            finalScore = 30,
            database = database,
        )
    }

    @Test
    fun `Should ignore games that are the wrong type`() {
        val otherType = GameType.values().find { it != factoryAchievement().gameType }!!
        val alice = insertPlayer(name = "Alice")
        val game = insertGame(gameType = otherType, gameParams = factoryAchievement().gameParams)

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 20)

        runConversion()

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore games that were completed as a team`() {
        val alice = insertPlayer(name = "Alice")
        val game = insertRelevantGame()
        val team = insertTeam(gameId = game.rowId)

        insertParticipant(
            gameId = game.rowId,
            playerId = alice.rowId,
            finalScore = 20,
            teamId = team.rowId,
        )

        runConversion()

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore games that are the wrong params`() {
        val alice = insertPlayer(name = "Alice")
        val game = insertGame(gameType = factoryAchievement().gameType!!, gameParams = "blah")

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 20)

        runConversion()

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore games of the wrong params with better scores`() {
        val alice = insertPlayer(name = "Alice")
        val badGame = insertGame(gameType = factoryAchievement().gameType!!, gameParams = "blah")
        val goodGame = insertRelevantGame()

        insertParticipant(gameId = badGame.rowId, playerId = alice.rowId, finalScore = 20)
        insertParticipant(gameId = goodGame.rowId, playerId = alice.rowId, finalScore = 31)

        runConversion()
        val result = retrieveAchievement()
        result.achievementCounter shouldBe 31
    }

    @Test
    fun `Should ignore games where the finalScore is unset`() {
        val alice = insertPlayer(name = "Alice")
        val game = insertRelevantGame()

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = -1)

        runConversion()

        getCountFromTable("Achievement") shouldBe 0

        insertParticipant(gameId = game.rowId, playerId = alice.rowId, finalScore = 20)
        runConversion()

        getCountFromTable("Achievement") shouldBe 1
        val achievementRow = AchievementEntity().retrieveEntities("")[0]
        achievementRow.achievementCounter shouldBe 20
    }

    @Test
    fun `Should return the lowest scoring game`() {
        val alice = insertPlayer(name = "Alice")

        val game = insertRelevantGame()

        insertParticipant(
            gameId = game.rowId,
            playerId = alice.rowId,
            finalScore = 15,
            dtFinished = Timestamp(1000),
        )
        insertParticipant(
            gameId = game.rowId,
            playerId = alice.rowId,
            finalScore = 12,
            dtFinished = Timestamp(1500),
        )

        runConversion()

        getCountFromTable("Achievement") shouldBe 1
        val achievementRow = AchievementEntity().retrieveEntities("")[0]
        achievementRow.achievementCounter shouldBe 12
    }

    @Test
    fun `Should return the earliest game if there is a tie for best score`() {
        val alice = insertPlayer(name = "Alice")

        val game = insertRelevantGame()

        insertParticipant(
            gameId = game.rowId,
            playerId = alice.rowId,
            finalScore = 12,
            dtFinished = Timestamp(1000),
        )
        insertParticipant(
            gameId = game.rowId,
            playerId = alice.rowId,
            finalScore = 12,
            dtFinished = Timestamp(800),
        )
        insertParticipant(
            gameId = game.rowId,
            playerId = alice.rowId,
            finalScore = 12,
            dtFinished = Timestamp(1500),
        )

        runConversion()

        getCountFromTable("Achievement") shouldBe 1
        val achievementRow = AchievementEntity().retrieveEntities("")[0]
        achievementRow.achievementCounter shouldBe 12
        achievementRow.dtAchieved shouldBe Timestamp(800)
    }

    @Test
    fun `Should set the correct values on the generated achievement row`() {
        val alice = insertPlayer(name = "Alice")

        val game = insertRelevantGame()

        insertParticipant(
            gameId = game.rowId,
            playerId = alice.rowId,
            finalScore = 12,
            dtFinished = Timestamp(1000),
        )

        runConversion()

        getCountFromTable("Achievement") shouldBe 1
        val achievementRow = AchievementEntity().retrieveEntities("")[0]
        achievementRow.achievementCounter shouldBe 12
        achievementRow.dtAchieved shouldBe Timestamp(1000)
        achievementRow.playerId shouldBe alice.rowId
        achievementRow.gameIdEarned shouldBe game.rowId
        achievementRow.achievementType shouldBe factoryAchievement().achievementType
    }
}
