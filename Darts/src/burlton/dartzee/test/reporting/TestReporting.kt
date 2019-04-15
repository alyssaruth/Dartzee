package burlton.dartzee.test.reporting

import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.reporting.ReportParameters
import burlton.dartzee.code.reporting.runReport
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.insertGameForReport
import burlton.dartzee.test.helper.wipeTable
import burlton.desktopcore.code.util.DateStatics
import burlton.desktopcore.code.util.getSqlDateNow
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.Test
import java.sql.Timestamp

class TestReporting: AbstractDartsTest()
{
    override fun beforeEachTest()
    {
        super.beforeEachTest()

        wipeTable("Game")
    }

    @Test
    fun `Should be able to filter by game type`()
    {
        val gameOne = insertGameForReport(gameType = GAME_TYPE_X01)
        val gameTwo = insertGameForReport(gameType = GAME_TYPE_GOLF)

        val rpAll = ReportParameters()
        val resultsAll = runReportForTest(rpAll)
        resultsAll.shouldContainExactlyInAnyOrder(gameOne.localId, gameTwo.localId)

        val rpX01 = ReportParameters()
        rpX01.gameType = GAME_TYPE_X01

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
    fun `Should be able to report on games starter after X`()
    {
        val gameOne = insertGameForReport(dtCreation = Timestamp(999))
        val gameTwo = insertGameForReport(dtCreation = Timestamp(1000))
        val gameThree = insertGameForReport(dtCreation = Timestamp(1001))

        val rpAll = ReportParameters()
        val resultsAll = runReportForTest(rpAll)
        resultsAll.shouldContainExactlyInAnyOrder(gameOne.localId, gameTwo.localId, gameThree.localId)

        val rpAfter = ReportParameters()
        rpAfter.dtStartFrom = Timestamp(1000)

        val resultsX01 = runReportForTest(rpAfter)
        resultsX01.shouldContainExactlyInAnyOrder(gameTwo.localId, gameThree.localId)
    }

    private fun runReportForTest(rp: ReportParameters): List<Long>
    {
        val wrappers = runReport(rp)
        return wrappers.map{it.localId}.toList()
    }
}