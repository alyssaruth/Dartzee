package dartzee.db.sanity

import io.github.alyssaruth.swingtest.findWindow
import io.github.alyssaruth.swingtest.flushEdt
import io.github.alyssaruth.swingtest.shouldBeVisible
import io.github.alyssaruth.swingtest.shouldNotBeVisible
import io.github.alyssaruth.swingtest.waitForAssertion
import dartzee.clickTableButton
import dartzee.core.bean.ScrollTable
import dartzee.core.screen.TableModelDialog
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.expectInfoDialog
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

class DatabaseSanityCheckTest : AbstractTest() {
    @Test
    fun `Should produce no results on an empty database, and tidy up all temp tables`() {
        DatabaseSanityCheck.runSanityCheck()

        awaitSanityCheck()

        mainDatabase.dropUnexpectedTables().shouldBeEmpty()
        findResultsWindow() shouldBe null

        expectInfoDialog("Sanity check completed and found no issues")
    }

    @Test
    fun `Should display failed checks with correct descriptions and counts`() {
        val game = insertGame()
        val p1 = insertPlayer()
        val p2 = insertPlayer()

        val checks =
            listOf(
                DummySanityCheckBadGames(listOf(game)),
                DummySanityCheckMultipleThings(listOf(p1, p2)),
            )
        DatabaseSanityCheck.runSanityCheck(checks)

        awaitSanityCheck()

        val resultsWindow = findResultsWindow()!!
        val rows = resultsWindow.table.getRows()
        rows.shouldContainExactly(
            listOf("Game rows where something's wrong", 1, "View Results >", "Auto-fix"),
            listOf("Player rows where thing one is wrong", 2, "View Results >", "Auto-fix"),
            listOf("Player rows where thing two is wrong", 2, "View Results >", "Auto-fix"),
        )
    }

    @Test
    fun `Should support auto-fixing`() {
        val mockResult = mockk<SanityCheckResult>(relaxed = true)

        val check = mockk<ISanityCheck>()
        every { check.runCheck() } returns listOf(mockResult)

        DatabaseSanityCheck.runSanityCheck(listOf(check))
        awaitSanityCheck()

        val resultsWindow = findResultsWindow()!!
        resultsWindow.table.clickTableButton(0, 3)
        verify { mockResult.autoFix() }
    }

    @Test
    fun `Should support viewing results breakdown`() {
        val breakdownDialog = TableModelDialog("Mock breakdown", ScrollTable())
        breakdownDialog.shouldNotBeVisible()

        val mockResult = mockk<SanityCheckResult>(relaxed = true)
        every { mockResult.getResultsDialog() } returns breakdownDialog

        val check = mockk<ISanityCheck>()
        every { check.runCheck() } returns listOf(mockResult)

        DatabaseSanityCheck.runSanityCheck(listOf(check))
        awaitSanityCheck()

        val resultsWindow = findResultsWindow()!!
        resultsWindow.table.clickTableButton(0, 2)
        breakdownDialog.shouldBeVisible()
    }

    private fun findResultsWindow() = findWindow<TableModelDialog> { it.title == "Sanity Results" }

    private fun awaitSanityCheck() {
        waitForAssertion { findLog(CODE_SANITY_CHECK_COMPLETED) shouldNotBe null }

        flushEdt()
    }
}

private class DummySanityCheckBadGames(private val games: List<GameEntity>) : ISanityCheck {
    override fun runCheck(): List<SanityCheckResult> =
        listOf(SanityCheckResultEntities(games, "something's wrong"))
}

private class DummySanityCheckMultipleThings(private val players: List<PlayerEntity>) :
    ISanityCheck {
    override fun runCheck(): List<SanityCheckResult> {
        return listOf(
            SanityCheckResultEntities(players, "thing one is wrong"),
            SanityCheckResultEntities(players, "thing two is wrong"),
        )
    }
}
