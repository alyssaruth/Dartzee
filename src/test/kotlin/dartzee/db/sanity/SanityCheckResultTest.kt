package dartzee.db.sanity

import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.getChild
import dartzee.core.bean.ScrollTable
import dartzee.core.helper.processKeyPress
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.helper.AbstractTest
import dartzee.runAsync
import io.kotest.matchers.shouldBe
import java.awt.event.KeyEvent
import javax.swing.JComponent
import javax.swing.KeyStroke
import javax.swing.table.DefaultTableModel
import org.junit.jupiter.api.Test

class SanityCheckResultTest : AbstractTest() {
    private val deleteKey = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, JComponent.WHEN_FOCUSED)

    @Test
    fun `Should display a dialog with the right title and no delete action`() {
        val tm = DefaultTableModel()
        tm.addColumn("Thing")
        tm.addColumn("Problem")
        tm.addRow(arrayOf("Phalange", "Wonky"))

        val result = SanityCheckResult(tm, "Things with problems")
        val dlg = result.getResultsDialog()

        val table = dlg.getChild<ScrollTable>()
        table.model shouldBe tm
        table.table.inputMap.get(deleteKey) shouldBe null

        dlg.title shouldBe "Things with problems"
    }

    @Test
    fun `Should show an error on attempted autofix`() {
        val result = SanityCheckResult(DefaultTableModel(), "Things with problems")
        runAsync { result.autoFix() }

        val error = getErrorDialog()
        error.getDialogMessage() shouldBe "No auto-fix available."
        error.clickOk(async = true)
    }

    @Test
    fun `Should add a delete action onto the table, which triggers when delete is pressed`() {
        var called = false
        val action: () -> Unit = { called = true }

        val tm = DefaultTableModel()
        tm.addColumn("Thing")
        tm.addRow(arrayOf("Phalange"))
        val result = ResultWithDeleteAction(tm, action)

        val dlg = result.getResultsDialog()
        val table = dlg.getChild<ScrollTable>()
        table.processKeyPress(KeyEvent.VK_DELETE)

        called shouldBe false

        table.selectRow(0)
        table.processKeyPress(KeyEvent.VK_DELETE)

        called shouldBe true
    }

    inner class ResultWithDeleteAction(model: DefaultTableModel, private val action: (() -> Unit)) :
        SanityCheckResult(model, "Bah") {

        override fun getDeleteAction(t: ScrollTable): (() -> Unit)? {
            return action
        }
    }
}
