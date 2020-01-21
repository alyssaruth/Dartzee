package dartzee.test.utils

import dartzee.db.*
import dartzee.utils.X01FinishConversion
import dartzee.test.helper.*
import dartzee.core.util.getSqlDateNow
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test

class TestX01FinishConversion: AbstractTest()
{
    @Test
    fun `Should count finishes with any remainder of 3`()
    {
        val p = insertPlayer(name = "Clive")
        val g1 = insertFinishForPlayer(p, 141, 9, 3)
        val g2 = insertFinishForPlayer(p, 120, 10, 4)
        val g3 = insertFinishForPlayer(p, 110, 11, 4)

        X01FinishConversion.convertX01Finishes()
        getCountFromTable("X01Finish") shouldBe 3

        val rows = X01FinishEntity().retrieveEntities().sortedBy { it.finish }

        rows[0].playerId shouldBe p.rowId
        rows[0].finish shouldBe 110
        rows[0].gameId shouldBe g3.rowId

        rows[1].playerId shouldBe p.rowId
        rows[1].finish shouldBe 120
        rows[1].gameId shouldBe g2.rowId

        rows[2].playerId shouldBe p.rowId
        rows[2].finish shouldBe 141
        rows[2].gameId shouldBe g1.rowId
    }

    @Test
    fun `Should take the startingScore of the 1st dart in the final round`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = 12)

        insertDart(pt, roundNumber = 4, startingScore = 120, ordinal = 1, score = 20, multiplier = 1)
        insertDart(pt, roundNumber = 4, startingScore = 100, ordinal = 2, score = 20, multiplier = 3)
        insertDart(pt, roundNumber = 4, startingScore = 40, ordinal = 3, score = 20, multiplier = 2)

        X01FinishConversion.convertX01Finishes()

        val rows = X01FinishEntity().retrieveEntities()
        rows.size shouldBe 1
        rows[0].finish shouldBe 120
    }

    @Test
    fun `Should ignore games of the wrong type`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GAME_TYPE_GOLF)

        insertFinishForPlayer(p, 100, game = g)

        X01FinishConversion.convertX01Finishes()
        getCountFromTable("X01Finish") shouldBe 0
    }

    @Test
    fun `Should ignore unfinished participants`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = -1)

        insertDart(pt, roundNumber = 4, startingScore = 100, ordinal = 1)

        X01FinishConversion.convertX01Finishes()
        getCountFromTable("X01Finish") shouldBe 0
    }

    @Test
    fun `Should not leave temp tables lying around`()
    {
        val player = insertPlayer()
        insertFinishForPlayer(player, 50)

        X01FinishConversion.convertX01Finishes()

        dropUnexpectedTables().shouldBeEmpty()
    }

    @Test
    fun `Should set dtCreation to be when the player finished`()
    {
        val dtFinished = getSqlDateNow()

        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = 15, dtFinished = dtFinished)

        insertDart(pt, roundNumber = 5, startingScore = 100, ordinal = 1)

        X01FinishConversion.convertX01Finishes()

        val entity = X01FinishEntity().retrieveEntities().first()
        entity.dtCreation shouldBe dtFinished
    }

    private fun insertFinishForPlayer(p: PlayerEntity, finish: Int, numberOfDarts: Int = 15, roundNumber: Int = 5, game: GameEntity = insertRelevantGame()): GameEntity
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = game.rowId, finalScore = numberOfDarts)

        insertDart(pt, roundNumber = roundNumber, startingScore = finish, ordinal = 1)

        return game
    }

    private fun insertRelevantGame() = insertGame(gameType = GAME_TYPE_X01)
}