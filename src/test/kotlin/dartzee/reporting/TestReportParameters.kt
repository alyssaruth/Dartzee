package dartzee.reporting

import dartzee.core.util.DateStatics
import dartzee.core.util.getSqlDateNow
import dartzee.game.GameType
import dartzee.helper.*
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.Test
import java.sql.Timestamp

class TestReportParameters: AbstractTest()
{
    @Test
    fun `Should be able to filter by game type`()
    {
        val gameOne = insertGameForReport(gameType = GameType.X01)
        val gameTwo = insertGameForReport(gameType = GameType.GOLF)

        val rpAll = ReportParameters()
        val resultsAll = runReportForTest(rpAll)
        resultsAll.shouldContainExactlyInAnyOrder(gameOne.localId, gameTwo.localId)

        val rpX01 = ReportParameters()
        rpX01.gameType = GameType.X01

        val resultsX01 = runReportForTest(rpX01)
        resultsX01.shouldContainExactly(gameOne.localId)
    }

    @Test
    fun `Should be able to filter by game params`()
    {
        val gameOne = insertGameForReport(gameParams = "foo")
        val gameTwo = insertGameForReport(gameParams = "bar")

        val rpAll = ReportParameters()
        val resultsAll = runReportForTest(rpAll)
        resultsAll.shouldContainExactlyInAnyOrder(gameOne.localId, gameTwo.localId)

        val rpBar = ReportParameters()
        rpBar.gameParams = "bar"

        val resultsX01 = runReportForTest(rpBar)
        resultsX01.shouldContainExactly(gameTwo.localId)
    }

    @Test
    fun `Should be able to report on only unfinished games`()
    {
        val gameOne = insertGameForReport(dtFinish = DateStatics.END_OF_TIME)
        val gameTwo = insertGameForReport(dtFinish = getSqlDateNow())

        val rpAll = ReportParameters()
        val resultsAll = runReportForTest(rpAll)
        resultsAll.shouldContainExactlyInAnyOrder(gameOne.localId, gameTwo.localId)

        val rpUnfinished = ReportParameters()
        rpUnfinished.unfinishedOnly = true

        val resultsX01 = runReportForTest(rpUnfinished)
        resultsX01.shouldContainExactly(gameOne.localId)
    }

    @Test
    fun `Should be able to report on creation date`()
    {
        val gameOne = insertGameForReport(dtCreation = Timestamp(999))
        val gameTwo = insertGameForReport(dtCreation = Timestamp(1000))
        val gameThree = insertGameForReport(dtCreation = Timestamp(1001))

        val rpAll = ReportParameters()
        val resultsAll = runReportForTest(rpAll)
        resultsAll.shouldContainExactlyInAnyOrder(gameOne.localId, gameTwo.localId, gameThree.localId)

        val rpAfter = ReportParameters()
        rpAfter.dtStartFrom = Timestamp(1000)

        val resultsAfter = runReportForTest(rpAfter)
        resultsAfter.shouldContainExactlyInAnyOrder(gameTwo.localId, gameThree.localId)

        val rpUpTo = ReportParameters()
        rpUpTo.dtStartTo = Timestamp(1000)

        val resultsUpTo = runReportForTest(rpUpTo)
        resultsUpTo.shouldContainExactlyInAnyOrder(gameOne.localId, gameTwo.localId)
    }

    @Test
    fun `Should be able to report on finish date`()
    {
        val gameOne = insertGameForReport(dtFinish = Timestamp(999))
        val gameTwo = insertGameForReport(dtFinish = Timestamp(1000))
        val gameThree = insertGameForReport(dtFinish = Timestamp(1001))

        val rpAll = ReportParameters()
        val resultsAll = runReportForTest(rpAll)
        resultsAll.shouldContainExactlyInAnyOrder(gameOne.localId, gameTwo.localId, gameThree.localId)

        val rpAfter = ReportParameters()
        rpAfter.dtFinishFrom = Timestamp(1000)

        val resultsAfter = runReportForTest(rpAfter)
        resultsAfter.shouldContainExactlyInAnyOrder(gameTwo.localId, gameThree.localId)

        val rpUpTo = ReportParameters()
        rpUpTo.dtFinishTo = Timestamp(1000)

        val resultsUpTo = runReportForTest(rpUpTo)
        resultsUpTo.shouldContainExactlyInAnyOrder(gameOne.localId, gameTwo.localId)
    }

    @Test
    fun `Should be able to report on whether a game was part of a match`()
    {
        val singleGame = insertGameForReport(dartsMatchId = "")
        val matchGame = insertGameForReport(dartsMatchId = randomGuid())

        val rpAll = ReportParameters()
        val resultsAll = runReportForTest(rpAll)
        resultsAll.shouldContainExactlyInAnyOrder(singleGame.localId, matchGame.localId)

        val rpSingleGames = ReportParameters()
        rpSingleGames.setEnforceMatch(false)
        val resultsSingleGames = runReportForTest(rpSingleGames)
        resultsSingleGames.shouldContainExactly(singleGame.localId)

        val rpMatchGames = ReportParameters()
        rpMatchGames.setEnforceMatch(true)
        val resultsMatchGames = runReportForTest(rpMatchGames)
        resultsMatchGames.shouldContainExactly(matchGame.localId)
    }

    @Test
    fun `Should be able to exclude games with certain players`()
    {
        val gAllPlayers = insertGame()
        val alice = insertPlayerForGame("Alice", gAllPlayers.rowId)
        val bob = insertPlayerForGame("Bob", gAllPlayers.rowId)
        val clive = insertPlayerForGame("Clive", gAllPlayers.rowId)
        val daisy = insertPlayerForGame("Daisy", gAllPlayers.rowId)

        val gAliceAndBob = insertGame()
        insertParticipant(playerId = alice.rowId, gameId = gAliceAndBob.rowId)
        insertParticipant(playerId = bob.rowId, gameId = gAliceAndBob.rowId)

        val gAliceCliveDaisy = insertGame()
        insertParticipant(playerId = alice.rowId, gameId = gAliceCliveDaisy.rowId)
        insertParticipant(playerId = clive.rowId, gameId = gAliceCliveDaisy.rowId)
        insertParticipant(playerId = daisy.rowId, gameId = gAliceCliveDaisy.rowId)

        val gBobAndDaisy = insertGame()
        insertParticipant(playerId = bob.rowId, gameId = gBobAndDaisy.rowId)
        insertParticipant(playerId = daisy.rowId, gameId = gBobAndDaisy.rowId)

        val gCliveDaisy = insertGame()
        insertParticipant(playerId = clive.rowId, gameId = gCliveDaisy.rowId)
        insertParticipant(playerId = daisy.rowId, gameId = gCliveDaisy.rowId)

        val rpAll = ReportParameters()
        val resultsAll = runReportForTest(rpAll)
        resultsAll.shouldContainExactlyInAnyOrder(gAllPlayers.localId, gAliceAndBob.localId, gAliceCliveDaisy.localId, gBobAndDaisy.localId, gCliveDaisy.localId)

        val rpExcludeAlice = ReportParameters()
        rpExcludeAlice.excludedPlayers = listOf(alice)
        val resultsNoAlice = runReportForTest(rpExcludeAlice)
        resultsNoAlice.shouldContainExactlyInAnyOrder(gBobAndDaisy.localId, gCliveDaisy.localId)

        val rpExcludeAliceAndBob = ReportParameters()
        rpExcludeAliceAndBob.excludedPlayers = listOf(alice, bob)
        val resultsNoAliceOrBob = runReportForTest(rpExcludeAliceAndBob)
        resultsNoAliceOrBob.shouldContainExactly(gCliveDaisy.localId)
    }

    @Test
    fun `Should be able to only include games with the specified players`()
    {
        val gAllPlayers = insertGame()
        val alice = insertPlayerForGame("Alice", gAllPlayers.rowId)
        val bob = insertPlayerForGame("Bob", gAllPlayers.rowId)
        val clive = insertPlayerForGame("Clive", gAllPlayers.rowId)
        val daisy = insertPlayerForGame("Daisy", gAllPlayers.rowId)

        val gAliceAndBob = insertGame()
        insertParticipant(playerId = alice.rowId, gameId = gAliceAndBob.rowId)
        insertParticipant(playerId = bob.rowId, gameId = gAliceAndBob.rowId)

        val gAliceCliveDaisy = insertGame()
        insertParticipant(playerId = alice.rowId, gameId = gAliceCliveDaisy.rowId)
        insertParticipant(playerId = clive.rowId, gameId = gAliceCliveDaisy.rowId)
        insertParticipant(playerId = daisy.rowId, gameId = gAliceCliveDaisy.rowId)

        val gBobAndDaisy = insertGame()
        insertParticipant(playerId = bob.rowId, gameId = gBobAndDaisy.rowId)
        insertParticipant(playerId = daisy.rowId, gameId = gBobAndDaisy.rowId)

        val gCliveDaisy = insertGame()
        insertParticipant(playerId = clive.rowId, gameId = gCliveDaisy.rowId)
        insertParticipant(playerId = daisy.rowId, gameId = gCliveDaisy.rowId)

        val rpIncludeAlice = ReportParameters()
        rpIncludeAlice.hmIncludedPlayerToParms = mapOf(alice to IncludedPlayerParameters())
        val resultsAlice = runReportForTest(rpIncludeAlice)
        resultsAlice.shouldContainExactlyInAnyOrder(gAllPlayers.localId, gAliceAndBob.localId, gAliceCliveDaisy.localId)

        val rpIncludeAliceAndBob = ReportParameters()
        rpIncludeAliceAndBob.hmIncludedPlayerToParms = mapOf(alice to IncludedPlayerParameters(), bob to IncludedPlayerParameters())
        val resultsAliceAndBob = runReportForTest(rpIncludeAliceAndBob)
        resultsAliceAndBob.shouldContainExactly(gAllPlayers.localId, gAliceAndBob.localId)
    }

    @Test
    fun `Should only include games with at least one human player if specified`()
    {
        val gAllPlayers = insertGame()
        val ai = insertPlayerForGame("AI", gAllPlayers.rowId, strategy = 1)
        val aiTwo = insertPlayerForGame("AI2", gAllPlayers.rowId, strategy = 1)
        val human = insertPlayerForGame("Human", gAllPlayers.rowId, strategy = -1)

        val gBothAi = insertGame()
        insertParticipant(playerId = ai.rowId, gameId = gBothAi.rowId)
        insertParticipant(playerId = aiTwo.rowId, gameId = gBothAi.rowId)

        val gHumanAndBothAI = insertGame()
        insertParticipant(playerId = human.rowId, gameId = gHumanAndBothAI.rowId)
        insertParticipant(playerId = ai.rowId, gameId = gHumanAndBothAI.rowId)
        insertParticipant(playerId = aiTwo.rowId, gameId = gHumanAndBothAI.rowId)

        val gSingleHuman = insertGame()
        insertParticipant(playerId = human.rowId, gameId = gSingleHuman.rowId)

        val gSingleAi = insertGame()
        insertParticipant(playerId = ai.rowId, gameId = gSingleAi.rowId)

        val rp = ReportParameters()
        rp.excludeOnlyAi = true

        val results = runReportForTest(rp)
        results.shouldContainExactlyInAnyOrder(listOf(gAllPlayers.localId, gHumanAndBothAI.localId, gSingleHuman.localId))
    }

    private fun runReportForTest(rp: ReportParameters): List<Long>
    {
        val wrappers = runReport(rp)
        return wrappers.map { it.localId }.toList()
    }
}