package dartzee.db.sanity

import dartzee.game.GameType
import dartzee.getRows
import dartzee.helper.*
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import org.junit.Test
import java.util.*

class TestSanityCheckX01Finishes: AbstractTest()
{
    override fun beforeEachTest()
    {
        super.beforeEachTest()
        mainDatabase.dropUnexpectedTables()
    }

    @Test
    fun `Should return an empty list if the finishes line up`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.X01)
        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId)

        insertFinishForPlayer(p, 60, game = g)

        insertDart(pt, startingScore = 60, score = 20, multiplier = 1, roundNumber = 1, ordinal = 1)
        insertDart(pt, startingScore = 40, score = 20, multiplier = 2, roundNumber = 1, ordinal = 2)

        val result = SanityCheckX01Finishes().runCheck()
        result.shouldBeEmpty()
    }

    @Test
    fun `Should return results if mismatching playerIds`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.X01)
        val pt = insertParticipant(gameId = g.rowId, playerId = UUID.randomUUID().toString())

        insertFinishForPlayer(p, 60, game = g)

        insertDart(pt, startingScore = 60, score = 20, multiplier = 1, roundNumber = 1, ordinal = 1)
        insertDart(pt, startingScore = 40, score = 20, multiplier = 2, roundNumber = 1, ordinal = 2)

        val result = SanityCheckX01Finishes().runCheck()
        result.shouldHaveSize(1)
    }

    @Test
    fun `Should return results if mismatching gameIds`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.X01)
        val pt = insertParticipant(gameId = insertGame(gameType = GameType.X01).rowId, playerId = p.rowId)

        insertFinishForPlayer(p, 60, game = g)

        insertDart(pt, startingScore = 60, score = 20, multiplier = 1, roundNumber = 1, ordinal = 1)
        insertDart(pt, startingScore = 40, score = 20, multiplier = 2, roundNumber = 1, ordinal = 2)

        val result = SanityCheckX01Finishes().runCheck()
        result.shouldHaveSize(1)
    }

    @Test
    fun `Should return results if mismatching finish value`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.X01)
        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId)

        insertFinishForPlayer(p, 60, game = g)

        insertDart(pt, startingScore = 15, score = 15, multiplier = 1, roundNumber = 1, ordinal = 1)
        insertDart(pt, startingScore = 40, score = 20, multiplier = 2, roundNumber = 1, ordinal = 2)

        val result = SanityCheckX01Finishes().runCheck()
        result.shouldHaveSize(1)
    }

    @Test
    fun `Should report extra rows in X01Finish`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.X01)

        insertFinishForPlayer(p, 60, game = g)

        val result = assertPositiveResult()
        val rows = result.getResultsModel().getRows()
        rows.size shouldBe 1
        rows[0].shouldContainExactly("EXTRA", p.rowId, g.rowId, 60)
    }

    @Test
    fun `Should report missing rows in X01Finish`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.X01)
        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId)

        insertDart(pt, startingScore = 60, score = 20, multiplier = 1, roundNumber = 1, ordinal = 1)
        insertDart(pt, startingScore = 40, score = 20, multiplier = 2, roundNumber = 1, ordinal = 2)

        val result = assertPositiveResult()
        val rows = result.getResultsModel().getRows()
        rows.size shouldBe 1
        rows[0].shouldContainExactly("MISSING", p.rowId, g.rowId, 60)
    }

    @Test
    fun `Should report discrepancy as missing and extra`()
    {
        val p = insertPlayer()
        val g = insertGame(gameType = GameType.X01)
        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId)

        insertFinishForPlayer(p, 60, game = g)

        insertDart(pt, startingScore = 55, score = 15, multiplier = 1, roundNumber = 1, ordinal = 1)
        insertDart(pt, startingScore = 40, score = 20, multiplier = 2, roundNumber = 1, ordinal = 2)

        val result = assertPositiveResult()
        val rows = result.getResultsModel().getRows()
        rows.shouldContainExactlyInAnyOrder(
            listOf("MISSING", p.rowId, g.rowId, 55),
            listOf("EXTRA", p.rowId, g.rowId, 60))
    }

    private fun assertPositiveResult(): SanityCheckResultSimpleTableModel
        = SanityCheckX01Finishes().runCheck().first() as SanityCheckResultSimpleTableModel
}