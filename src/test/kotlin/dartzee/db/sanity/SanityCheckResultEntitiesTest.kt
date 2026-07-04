package dartzee.db.sanity

import io.github.alyssaruth.swingtest.clickNo
import io.github.alyssaruth.swingtest.clickYes
import io.github.alyssaruth.swingtest.flushEdt
import io.github.alyssaruth.swingtest.getChild
import dartzee.core.bean.ScrollTable
import dartzee.core.helper.processKeyPress
import dartzee.db.EntityName
import dartzee.db.FakeEntity
import dartzee.db.PlayerEntity
import dartzee.expectErrorDialog
import dartzee.getDialogMessage
import dartzee.getQuestionDialog
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.helper.insertPlayer
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.logging.Severity
import io.kotest.matchers.shouldBe
import java.awt.event.KeyEvent
import org.junit.jupiter.api.Test

class SanityCheckResultEntitiesTest : AbstractTest() {
    @Test
    fun `Should not delete an entity if not confirmed`() {
        val p = insertPlayer(name = "Alyssa")

        val result = SanityCheckResultEntities(listOf(p), "No Alyssas allowed!")
        val dialog = result.getResultsDialog()

        val scrollTable = dialog.getChild<ScrollTable>()
        scrollTable.selectRow(0)
        scrollTable.processKeyPress(KeyEvent.VK_DELETE, async = true)

        val question = getQuestionDialog()
        question.getDialogMessage() shouldBe "Are you sure you want to delete 1 row(s) from Player?"
        question.clickNo()
        flushEdt()

        getCountFromTable(EntityName.Player) shouldBe 1
    }

    @Test
    fun `Should delete the selected entity`() {
        val p1 = insertPlayer(name = "Alyssa")
        val p2 = insertPlayer(name = "Bob")
        val p3 = insertPlayer(name = "Claire")

        val result = SanityCheckResultEntities(listOf(p1, p3), "foo")
        val dialog = result.getResultsDialog()

        val scrollTable = dialog.getChild<ScrollTable>()
        scrollTable.selectRow(0)
        scrollTable.processKeyPress(KeyEvent.VK_DELETE, async = true)

        val question = getQuestionDialog()
        question.getDialogMessage() shouldBe "Are you sure you want to delete 1 row(s) from Player?"
        question.clickYes()
        flushEdt()

        getCountFromTable(EntityName.Player) shouldBe 2
        PlayerEntity.retrieveForName("Alyssa") shouldBe null
        PlayerEntity.retrieveForName("Bob")!!.rowId shouldBe p2.rowId
        PlayerEntity.retrieveForName("Claire")!!.rowId shouldBe p3.rowId
    }

    @Test
    fun `Should allow multi-select deletion`() {
        val p1 = insertPlayer(name = "Alyssa")
        val p2 = insertPlayer(name = "Bob")
        val p3 = insertPlayer(name = "Claire")

        val result = SanityCheckResultEntities(listOf(p1, p2, p3), "foo")
        val dialog = result.getResultsDialog()

        val scrollTable = dialog.getChild<ScrollTable>()
        scrollTable.table.addRowSelectionInterval(0, 0)
        scrollTable.table.addRowSelectionInterval(2, 2)
        scrollTable.processKeyPress(KeyEvent.VK_DELETE, async = true)

        val question = getQuestionDialog()
        question.getDialogMessage() shouldBe "Are you sure you want to delete 2 row(s) from Player?"
        question.clickYes()
        flushEdt()

        getCountFromTable(EntityName.Player) shouldBe 1
        PlayerEntity.retrieveForName("Alyssa") shouldBe null
        PlayerEntity.retrieveForName("Bob")!!.rowId shouldBe p2.rowId
        PlayerEntity.retrieveForName("Claire") shouldBe null
    }

    @Test
    fun `Should show an error if deletion fails`() {
        val p1 = FakeEntity()

        val result = SanityCheckResultEntities(listOf(p1), "foo")
        val dialog = result.getResultsDialog()

        val scrollTable = dialog.getChild<ScrollTable>()
        scrollTable.selectRow(0)
        scrollTable.processKeyPress(KeyEvent.VK_DELETE, async = true)

        val question = getQuestionDialog()
        question.getDialogMessage() shouldBe
            "Are you sure you want to delete 1 row(s) from TestTable?"

        question.clickYes()
        flushEdt()

        expectErrorDialog(
            "An error occurred deleting the rows. You should re-run the sanity check and check logs."
        )

        verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)
    }
}
