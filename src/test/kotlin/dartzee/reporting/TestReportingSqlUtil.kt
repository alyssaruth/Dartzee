package dartzee.reporting

import dartzee.core.util.getSqlDateNow
import dartzee.game.GameType
import dartzee.helper.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestReportingSqlUtil: AbstractTest()
{
    @Test
    fun `Should parse participants and finishing positions correctly`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")
        val clive = insertPlayer(name = "Clive")

        val match = insertDartsMatch(localId = 7)
        val g1 = insertGame(dartsMatchId = match.rowId, matchOrdinal = 1)
        insertParticipant(gameId = g1.rowId, playerId = alice.rowId, finishingPosition = 1)
        insertParticipant(gameId = g1.rowId, playerId = bob.rowId, finishingPosition = 3)
        insertParticipant(gameId = g1.rowId, playerId = clive.rowId, finishingPosition = 2)

        val results = runReport(ReportParameters())
        results.size shouldBe 1
        val wrapper = results.first()

        val row = wrapper.getTableRow()
        row shouldBe arrayOf(g1.localId, "501", "Alice (1), Clive (2), Bob (3)", g1.dtCreation, g1.dtFinish, "#7 (Game 1)")
    }

    @Test
    fun `Should return a game not part of a match`()
    {
        val alice = insertPlayer(name = "Alice")

        val g = insertGame(dartsMatchId = "", gameType = GameType.GOLF, gameParams = "18")
        insertParticipant(gameId = g.rowId, playerId = alice.rowId, finishingPosition = -1)

        val results = runReport(ReportParameters())
        results.size shouldBe 1
        val wrapper = results.first()

        val row = wrapper.getTableRow()
        row shouldBe arrayOf(g.localId, "Golf - 18 holes", "Alice (-)", g.dtCreation, g.dtFinish, "")
    }

    @Test
    fun `Should separate participants into the correct rows`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        val gAlice = insertGame()
        insertParticipant(gameId = gAlice.rowId, playerId = alice.rowId, finishingPosition = -1)

        val gBob = insertGame(dtFinish = getSqlDateNow())
        insertParticipant(gameId = gBob.rowId, playerId = bob.rowId)

        val results = runReport(ReportParameters())
        results.size shouldBe 2

        val rows = ReportResultWrapper.getTableRowsFromWrappers(results)
        rows[0] shouldBe arrayOf(gAlice.localId, "501", "Alice (-)", gAlice.dtCreation, gAlice.dtFinish, "")
        rows[1] shouldBe arrayOf(gBob.localId, "501", "Bob (-)", gBob.dtCreation, gBob.dtFinish, "")
    }
}