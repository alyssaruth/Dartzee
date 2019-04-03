package burlton.dartzee.test.db

import burlton.core.test.helper.exceptionLogged
import burlton.core.test.helper.getLogs
import burlton.dartzee.code.db.*
import burlton.dartzee.test.helper.*
import burlton.desktopcore.code.util.getSqlDateNow
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.string.shouldBeEmpty
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test

class TestGameEntity: AbstractEntityTest<GameEntity>()
{
    override fun factoryDao() = GameEntity()

    @Test
    fun `LocalId field should be unique`()
    {
        wipeTable("Game")

        insertGame(localId = 5)
        exceptionLogged() shouldBe false

        insertGame(localId = 5)
        exceptionLogged() shouldBe true
        getLogs().shouldContain("duplicate key")

        getCountFromTable("Game") shouldBe 1
    }

    @Test
    fun `LocalIds should be assigned along with RowId`()
    {
        wipeTable("Game")

        val entity = GameEntity()
        entity.assignRowId()

        entity.rowId.shouldNotBeEmpty()
        entity.localId shouldBe 1
    }

    @Test
    fun `isFinished should work correctly`()
    {
        val entity = GameEntity()
        entity.isFinished() shouldBe false

        entity.dtFinish = getSqlDateNow()
        entity.isFinished() shouldBe true
    }

    @Test
    fun `Should get the participant count based on its own row ID`()
    {
        wipeTable("Participant")
        wipeTable("Game")

        val game = GameEntity()
        val gameId = game.assignRowId()
        game.saveToDatabase()
        game.getParticipantCount() shouldBe 0

        insertParticipant()
        game.getParticipantCount() shouldBe 0

        insertParticipant(gameId = gameId)
        game.getParticipantCount() shouldBe 1
    }

    @Test
    fun `Should handle no participants when getting players vector`()
    {
        wipeTable("Participant")
        wipeTable("Game")

        val game = GameEntity()
        game.saveToDatabase()

        game.retrievePlayersVector().shouldBeEmpty()
    }

    @Test
    fun `Should return the player vector correctly`()
    {
        wipeTable("Player")
        wipeTable("Participant")
        wipeTable("Game")

        //Insert a random player
        val playerId = insertPlayer(name = "Clive")
        val game = GameEntity()
        val gameId = game.assignRowId()
        game.saveToDatabase()

        insertParticipant(gameId = gameId, playerId = playerId)
        insertParticipant(gameId = randomGuid(), playerId = playerId)

        val players = game.retrievePlayersVector()
        players.shouldHaveSize(1)

        players.first().name shouldBe "Clive"
    }


    @Test
    fun `Game descriptions`()
    {
        val game = GameEntity()

        game.getTypeDesc().shouldBeEmpty()

        game.gameParams = "foo"

        game.gameType = GAME_TYPE_X01
        game.getTypeDesc() shouldBe "foo"

        game.gameType = GAME_TYPE_GOLF
        game.getTypeDesc() shouldBe "Golf - foo holes"

        game.gameType = GAME_TYPE_ROUND_THE_CLOCK
        game.getTypeDesc() shouldBe "Round the Clock - foo"

        game.gameType = GAME_TYPE_DARTZEE
        game.getTypeDesc() shouldBe "Dartzee"
    }
}