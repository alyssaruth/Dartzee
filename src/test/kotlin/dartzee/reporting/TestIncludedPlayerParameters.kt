package dartzee.reporting

import dartzee.core.bean.ComboBoxNumberComparison
import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.makeIncludedPlayerParameters
import dartzee.helper.makeReportParametersGame
import dartzee.helper.makeReportParametersPlayers
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.string.shouldBeEmpty
import org.junit.jupiter.api.Test

class TestIncludedPlayerParameters : AbstractTest() {
    @Test
    fun `Should be blank by default`() {
        val ipp = makeIncludedPlayerParameters()
        ipp.generateExtraWhereSql("foo").shouldBeEmpty()
    }

    @Test
    fun `Should filter by finishing position`() {
        val player = insertPlayer("Bob")

        val winningGameId = insertGame(localId = 1).rowId
        val thirdGameId = insertGame(localId = 2).rowId
        val secondGameId = insertGame(localId = 3).rowId
        val unfinishedGameId = insertGame(localId = 4).rowId

        insertParticipant(playerId = player.rowId, finishingPosition = 1, gameId = winningGameId)
        insertParticipant(playerId = player.rowId, finishingPosition = 2, gameId = secondGameId)
        insertParticipant(playerId = player.rowId, finishingPosition = 3, gameId = thirdGameId)
        insertParticipant(
            playerId = player.rowId,
            finishingPosition = -1,
            gameId = unfinishedGameId
        )

        val ipp = makeIncludedPlayerParameters(finishingPositions = listOf(1, 3))
        val rp = makeReportParameters(player, ipp)
        val results = runReport(rp)
        results shouldHaveSize 2

        results.map { it.localId }.shouldContainExactlyInAnyOrder(1, 2)
    }

    @Test
    fun `Should filter by final score`() {
        val player = insertPlayer("Bob")

        val g40 = insertGame(localId = 1).rowId
        val g30 = insertGame(localId = 2).rowId
        val gUnfinished = insertGame(localId = 3).rowId
        val g25 = insertGame(localId = 4).rowId

        insertParticipant(
            playerId = player.rowId,
            finishingPosition = 1,
            finalScore = 40,
            gameId = g40
        )
        insertParticipant(
            playerId = player.rowId,
            finishingPosition = 2,
            finalScore = 30,
            gameId = g30
        )
        insertParticipant(
            playerId = player.rowId,
            finishingPosition = 3,
            finalScore = 25,
            gameId = g25
        )
        insertParticipant(
            playerId = player.rowId,
            finishingPosition = -1,
            finalScore = -1,
            gameId = gUnfinished
        )

        // Greater than 25
        val ippGt25 =
            makeIncludedPlayerParameters(
                finalScore = 25,
                finalScoreComparator = ComboBoxNumberComparison.FILTER_MODE_GREATER_THAN
            )

        var results = runReport(makeReportParameters(player, ippGt25))
        results.map { it.localId }.shouldContainExactlyInAnyOrder(1, 2)

        val ippEq25 =
            makeIncludedPlayerParameters(
                finalScore = 25,
                finalScoreComparator = ComboBoxNumberComparison.FILTER_MODE_EQUAL_TO
            )
        results = runReport(makeReportParameters(player, ippEq25))
        results.map { it.localId }.shouldContainExactlyInAnyOrder(4)

        // Undecided
        val ippUnset = makeIncludedPlayerParameters(finalScoreComparator = COMPARATOR_SCORE_UNSET)
        results = runReport(makeReportParameters(player, ippUnset))
        results.map { it.localId }.shouldContainExactlyInAnyOrder(3)

        // Less than 31
        val ippLt31 =
            makeIncludedPlayerParameters(
                finalScore = 31,
                finalScoreComparator = ComboBoxNumberComparison.FILTER_MODE_LESS_THAN
            )
        results = runReport(makeReportParameters(player, ippLt31))
        results.map { it.localId }.shouldContainExactlyInAnyOrder(2, 4)
    }

    private fun makeReportParameters(
        player: PlayerEntity,
        ipp: IncludedPlayerParameters
    ): ReportParameters {
        val rpPlayers = makeReportParametersPlayers(includedPlayers = mapOf(player to ipp))
        return ReportParameters(makeReportParametersGame(), rpPlayers)
    }
}
