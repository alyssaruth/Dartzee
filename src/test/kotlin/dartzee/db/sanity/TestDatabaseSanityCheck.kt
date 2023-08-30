package dartzee.db.sanity

import com.github.alyssaburlton.swingtest.findWindow
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import com.github.alyssaburlton.swingtest.waitForAssertion
import dartzee.clickTableButton
import dartzee.core.bean.ScrollTable
import dartzee.core.screen.TableModelDialog
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.logging.CODE_SANITY_CHECK_COMPLETED
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TestDatabaseSanityCheck : AbstractTest()
{
    @Test
    fun `Should produce no results on an empty database, and tidy up all temp tables`()
    {
        DatabaseSanityCheck.runSanityCheck()

        awaitSanityCheck()

        mainDatabase.dropUnexpectedTables().shouldBeEmpty()
        findResultsWindow() shouldBe null
        dialogFactory.infosShown.shouldContainExactly("Sanity check completed and found no issues")
    }

    @Test
    fun `Should display failed checks with correct descriptions and counts`()
    {
        val game = insertGame()
        val p1 = insertPlayer()
        val p2 = insertPlayer()

        val checks = listOf(DummySanityCheckBadGames(listOf(game)), DummySanityCheckMultipleThings(listOf(p1, p2)))
        DatabaseSanityCheck.runSanityCheck(checks)

        awaitSanityCheck()

        val resultsWindow = findResultsWindow()!!
        val rows = resultsWindow.table.getRows()
        rows.shouldContainExactly(
            listOf("Games where something's wrong", 1, "View Results >", "Auto-fix"),
            listOf("Players with thing one wrong", 2, "View Results >", "Auto-fix"),
            listOf("Players with thing two wrong", 2, "View Results >", "Auto-fix")
        )
    }

    @Test
    fun `Should support auto-fixing`()
    {
        val mockResult = mockk<AbstractSanityCheckResult>(relaxed = true)
        every { mockResult.getDescription() } returns "Foo"

        val check = mockk<AbstractSanityCheck>()
        every { check.runCheck() } returns listOf(mockResult)

        DatabaseSanityCheck.runSanityCheck(listOf(check))
        awaitSanityCheck()

        val resultsWindow = findResultsWindow()!!
        resultsWindow.table.clickTableButton(0, 3)
        verify { mockResult.autoFix() }
    }

    @Test
    fun `Should support viewing results breakdown`()
    {
        val breakdownDialog = TableModelDialog("Mock breakdown", ScrollTable())
        breakdownDialog.shouldNotBeVisible()

        val mockResult = mockk<AbstractSanityCheckResult>(relaxed = true)
        every { mockResult.getDescription() } returns "Foo"
        every { mockResult.getResultsDialog() } returns breakdownDialog

        val check = mockk<AbstractSanityCheck>()
        every { check.runCheck() } returns listOf(mockResult)

        DatabaseSanityCheck.runSanityCheck(listOf(check))
        awaitSanityCheck()

        val resultsWindow = findResultsWindow()!!
        resultsWindow.table.clickTableButton(0, 2)
        breakdownDialog.shouldBeVisible()
    }

    private fun findResultsWindow() =
        findWindow<TableModelDialog> { it.title == "Sanity Results" }

    private fun awaitSanityCheck()
    {
        waitForAssertion {
            findLog(CODE_SANITY_CHECK_COMPLETED) shouldNotBe null
        }

        flushEdt()
    }
}

private class DummySanityCheckBadGames(private val games: List<GameEntity>) : AbstractSanityCheck()
{
    override fun runCheck(): List<AbstractSanityCheckResult> =
        listOf(SanityCheckResultEntitiesSimple(games, "Games where something's wrong"))
}

private class DummySanityCheckMultipleThings(private val players: List<PlayerEntity>): AbstractSanityCheck()
{
    override fun runCheck(): List<AbstractSanityCheckResult> {
        return listOf(
            SanityCheckResultEntitiesSimple(players, "Players with thing one wrong"),
            SanityCheckResultEntitiesSimple(players, "Players with thing two wrong")
        )
    }
}