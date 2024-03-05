package dartzee.reporting

import dartzee.core.util.getSqlDateNow
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertDartsMatch
import dartzee.helper.insertDartzeeTemplate
import dartzee.helper.insertGame
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerForGame
import dartzee.helper.insertTeamAndParticipants
import dartzee.helper.makeReportParameters
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestReportingSqlUtil : AbstractTest() {
    @Test
    fun `Should parse participants and finishing positions correctly`() {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")
        val clive = insertPlayer(name = "Clive")

        val match = insertDartsMatch(localId = 7)
        val g1 = insertGame(dartsMatchId = match.rowId, matchOrdinal = 1)
        insertParticipant(gameId = g1.rowId, playerId = alice.rowId, finishingPosition = 1)
        insertParticipant(gameId = g1.rowId, playerId = bob.rowId, finishingPosition = 3)
        insertParticipant(gameId = g1.rowId, playerId = clive.rowId, finishingPosition = 2)

        val results = runReport(makeReportParameters())
        results.size shouldBe 1
        val wrapper = results.first()

        val row = wrapper.getTableRow()
        row shouldBe
            arrayOf(
                g1.localId,
                "501",
                "Alice (1), Clive (2), Bob (3)",
                g1.dtCreation,
                g1.dtFinish,
                "#7 (Game 1)"
            )
    }

    @Test
    fun `Should parse teams correctly`() {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")
        val clive = insertPlayer(name = "Clive")

        val g1 = insertGame()
        insertParticipant(gameId = g1.rowId, playerId = alice.rowId, finishingPosition = 1)
        insertTeamAndParticipants(
            gameId = g1.rowId,
            playerOne = bob,
            playerTwo = clive,
            finishingPosition = 2
        )

        val results = runReport(makeReportParameters())
        results.size shouldBe 1
        val wrapper = results.first()

        val row = wrapper.getTableRow()
        row shouldBe
            arrayOf(g1.localId, "501", "Alice (1), Bob & Clive (2)", g1.dtCreation, g1.dtFinish, "")
    }

    @Test
    fun `Should return a game not part of a match`() {
        val alice = insertPlayer(name = "Alice")

        val g = insertGame(dartsMatchId = "", gameType = GameType.GOLF, gameParams = "18")
        insertParticipant(gameId = g.rowId, playerId = alice.rowId, finishingPosition = -1)

        val results = runReport(makeReportParameters())
        results.size shouldBe 1
        val wrapper = results.first()

        val row = wrapper.getTableRow()
        row shouldBe
            arrayOf(g.localId, "Golf - 18 holes", "Alice (-)", g.dtCreation, g.dtFinish, "")
    }

    @Test
    fun `Should separate participants into the correct rows`() {
        val gAlice = insertGame()
        insertPlayerForGame("Alice", gAlice.rowId)

        val gBob = insertGame(dtFinish = getSqlDateNow())
        insertPlayerForGame("Bob", gBob.rowId)

        val results = runReport(makeReportParameters())
        results.size shouldBe 2

        val rows = ReportResultWrapper.getTableRowsFromWrappers(results)
        rows[0] shouldBe
            arrayOf(gAlice.localId, "501", "Alice (-)", gAlice.dtCreation, gAlice.dtFinish, "")
        rows[1] shouldBe arrayOf(gBob.localId, "501", "Bob (-)", gBob.dtCreation, gBob.dtFinish, "")
    }

    @Test
    fun `Should retrieve Dartzee template names when appropriate`() {
        val template = insertDartzeeTemplate(name = "BTBF's House Party")
        val dartzeeGameWithTemplate =
            insertGame(gameType = GameType.DARTZEE, gameParams = template.rowId)
        val dartzeeGameStandalone = insertGame(gameType = GameType.DARTZEE, gameParams = "")
        val x01Game = insertGame(gameType = GameType.X01)

        insertPlayerForGame("Alice", dartzeeGameWithTemplate.rowId)
        insertPlayerForGame("Bob", dartzeeGameStandalone.rowId)
        insertPlayerForGame("Clive", x01Game.rowId)

        val results = runReport(makeReportParameters())
        results.first { it.localId == 1L }.templateName shouldBe "BTBF's House Party"
        results.first { it.localId == 2L }.templateName shouldBe null
        results.first { it.localId == 3L }.templateName shouldBe null

        val rows = ReportResultWrapper.getTableRowsFromWrappers(results)
        rows[0] shouldBe
            arrayOf(
                dartzeeGameWithTemplate.localId,
                "Dartzee - BTBF's House Party",
                "Alice (-)",
                dartzeeGameWithTemplate.dtCreation,
                dartzeeGameWithTemplate.dtFinish,
                ""
            )
        rows[1] shouldBe
            arrayOf(
                dartzeeGameStandalone.localId,
                "Dartzee",
                "Bob (-)",
                dartzeeGameStandalone.dtCreation,
                dartzeeGameStandalone.dtFinish,
                ""
            )
        rows[2] shouldBe
            arrayOf(x01Game.localId, "501", "Clive (-)", x01Game.dtCreation, x01Game.dtFinish, "")
    }
}
