package burlton.dartzee.test.db

import burlton.core.test.helper.exceptionLogged
import burlton.core.test.helper.getLogs
import burlton.dartzee.code.db.*
import burlton.dartzee.test.helper.*
import burlton.desktopcore.code.util.DateStatics
import burlton.desktopcore.code.util.getSqlDateNow
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.string.shouldBeEmpty
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotBeEmpty
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import java.sql.Timestamp

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
        val game = GameEntity()
        val gameId = game.assignRowId()
        game.saveToDatabase()

        insertPlayerForGame("Clive", gameId)
        insertPlayerForGame("Bob", randomGuid())

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

    @Test
    fun `Factory and save individual game`()
    {
        val game = GameEntity.factoryAndSave(GAME_TYPE_X01, "301")

        val gameId = game.rowId
        game.localId shouldNotBe -1
        game.gameType shouldBe GAME_TYPE_X01
        game.gameParams shouldBe "301"
        game.dtFinish shouldBe DateStatics.END_OF_TIME
        game.dartsMatchId shouldBe ""
        game.matchOrdinal shouldBe -1

        game.retrieveForId(gameId) shouldNotBe null
    }

    @Test
    fun `Factory and save for a match`()
    {
        val match = DartsMatchEntity.factoryFirstTo(4)
        match.gameType = GAME_TYPE_GOLF
        match.gameParams = "18"

        val matchId = match.rowId

        val gameOne = GameEntity.factoryAndSave(match)
        gameOne.matchOrdinal shouldBe 1
        gameOne.dartsMatchId shouldBe matchId
        gameOne.gameType shouldBe GAME_TYPE_GOLF
        gameOne.gameParams shouldBe "18"
        gameOne.rowId shouldNotBe ""

        val gameTwo = GameEntity.factoryAndSave(match)
        gameTwo.matchOrdinal shouldBe 2
        gameTwo.dartsMatchId shouldBe matchId
        gameTwo.gameType shouldBe GAME_TYPE_GOLF
        gameTwo.gameParams shouldBe "18"
        gameTwo.rowId shouldNotBe ""
    }

    @Test
    fun `Should retrieve games in the right order for a match`()
    {
        val matchId = randomGuid()

        val millis = System.currentTimeMillis()
        val gameTwoId = insertGame(dartsMatchId = matchId, matchOrdinal = 1, dtCreation = Timestamp(millis))
        val gameOneId = insertGame(dartsMatchId = matchId, matchOrdinal = 1, dtCreation = Timestamp(millis - 5))
        insertGame(dartsMatchId = randomGuid())
        val gameThreeId = insertGame(dartsMatchId = matchId, matchOrdinal = 2, dtCreation = Timestamp(millis - 10))

        val gameIds = GameEntity.retrieveGamesForMatch(matchId).map{it.rowId}.toList()
        gameIds.shouldContainExactly(gameOneId, gameTwoId, gameThreeId)
    }

    @Test
    fun `Sensible descriptions when no params`()
    {
        GameEntity.getTypeDesc(GAME_TYPE_X01) shouldBe "X01"
        GameEntity.getTypeDesc(GAME_TYPE_GOLF) shouldBe "Golf"
        GameEntity.getTypeDesc(GAME_TYPE_ROUND_THE_CLOCK) shouldBe "Round the Clock"
        GameEntity.getTypeDesc(GAME_TYPE_DARTZEE) shouldBe "Dartzee"
        GameEntity.getTypeDesc(-1) shouldBe "<Game Type>"
    }

    @Test
    fun `Filter panel mappings`()
    {
        GameEntity.getFilterPanel(GAME_TYPE_X01) shouldNotBe null
        GameEntity.getFilterPanel(GAME_TYPE_GOLF) shouldNotBe null
        GameEntity.getFilterPanel(GAME_TYPE_ROUND_THE_CLOCK) shouldNotBe null
        GameEntity.getFilterPanel(GAME_TYPE_DARTZEE) shouldBe null
        GameEntity.getFilterPanel(-1) shouldBe null
    }

    @Test
    fun `Should map localId to gameId`()
    {
        val gameIdOne = insertGame(localId = 1)
        val gameIdTwo = insertGame(localId = 2)

        GameEntity.getGameId(1) shouldBe gameIdOne
        GameEntity.getGameId(2) shouldBe gameIdTwo
        GameEntity.getGameId(3) shouldBe null
    }
}