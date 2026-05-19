package dartzee.core.util

import com.github.alyssaburlton.swingtest.clickCancel
import com.github.alyssaburlton.swingtest.clickNo
import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.clickYes
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.purgeWindows
import com.github.alyssaburlton.swingtest.shouldBeVisible
import dartzee.cancelOptionDialog
import dartzee.dismissOptionDialog
import dartzee.expectInfoDialog
import dartzee.getErrorDialog
import dartzee.getFileChooser
import dartzee.getQuestionDialog
import dartzee.helper.AbstractTest
import dartzee.helper.TEST_ROOT
import dartzee.logging.CODE_DIALOG_CLOSED
import dartzee.logging.CODE_DIALOG_SHOWN
import dartzee.logging.Severity
import dartzee.runAsync
import dartzee.selectFile
import dartzee.selectFromOptionDialog
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.io.File
import javax.swing.JLabel
import org.junit.jupiter.api.Test

class DialogUtilTest : AbstractTest() {
    @Test
    fun `Should log for INFO dialogs`() {
        runAsync { DialogUtil.showInfo("Something useful") }

        verifyLog(CODE_DIALOG_SHOWN, Severity.INFO).message shouldBe
            "Info dialog shown: Something useful"

        expectInfoDialog("Something useful")

        verifyLog(CODE_DIALOG_CLOSED, Severity.INFO).message shouldBe "Info dialog closed"
    }

    @Test
    fun `Should log for ERROR dialogs`() {
        runAsync { DialogUtil.showError("Something bad") }

        verifyLog(CODE_DIALOG_SHOWN, Severity.INFO).message shouldBe
            "Error dialog shown: Something bad"
        getErrorDialog().clickOk()
        flushEdt()
        verifyLog(CODE_DIALOG_CLOSED, Severity.INFO).message shouldBe "Error dialog closed"
    }

    @Test
    fun `Should log for QUESTION dialogs, with the correct selection`() {
        runAsync { DialogUtil.showQuestion("Do you like cheese?") }
        verifyLog(CODE_DIALOG_SHOWN, Severity.INFO).message shouldBe
            "Question dialog shown: Do you like cheese?"
        getQuestionDialog().clickYes()
        flushEdt()
        verifyLog(CODE_DIALOG_CLOSED, Severity.INFO).message shouldBe
            "Question dialog closed - selected Yes"

        clearLogs()
        purgeWindows()

        runAsync { DialogUtil.showQuestion("Do you like mushrooms?") }
        verifyLog(CODE_DIALOG_SHOWN, Severity.INFO).message shouldBe
            "Question dialog shown: Do you like mushrooms?"
        getQuestionDialog().clickNo()
        flushEdt()
        verifyLog(CODE_DIALOG_CLOSED, Severity.INFO).message shouldBe
            "Question dialog closed - selected No"

        clearLogs()
        purgeWindows()

        runAsync { DialogUtil.showQuestion("Do you want to delete all data?", true) }
        verifyLog(CODE_DIALOG_SHOWN, Severity.INFO).message shouldBe
            "Question dialog shown: Do you want to delete all data?"
        getQuestionDialog().clickCancel()
        flushEdt()
        verifyLog(CODE_DIALOG_CLOSED, Severity.INFO).message shouldBe
            "Question dialog closed - selected Cancel"
    }

    @Test
    fun `Should log when showing and dismissing loading dialog`() {
        DialogUtil.showLoadingDialog("One moment...")
        flushEdt()
        verifyLog(CODE_DIALOG_SHOWN, Severity.INFO).message shouldBe
            "Loading dialog shown: One moment..."

        DialogUtil.dismissLoadingDialog()
        verifyLog(CODE_DIALOG_CLOSED, Severity.INFO).message shouldBe "Loading dialog closed"
    }

    @Test
    fun `Should not log if loading dialog wasn't visible`() {
        DialogUtil.dismissLoadingDialog()
        verifyNoLogs(CODE_DIALOG_CLOSED)
    }

    @Test
    fun `Should show an option pane and return the selection`() {
        var selection: String? = null
        runAsync {
            selection =
                DialogUtil.showOption(
                    "Free Pizza",
                    "Would you like some?",
                    listOf("Yes please", "No thanks", "Maybe later"),
                )
        }

        verifyLog(CODE_DIALOG_SHOWN, Severity.INFO).message shouldBe
            "Option dialog shown: Would you like some?"

        selectFromOptionDialog("Free Pizza", "Yes please")

        verifyLog(CODE_DIALOG_CLOSED, Severity.INFO).message shouldBe
            "Option dialog closed - selected Yes please"
        selection shouldBe "Yes please"
    }

    @Test
    fun `Should handle cancelling an option dialog`() {
        var selection: String? = null
        runAsync {
            selection =
                DialogUtil.showOption(
                    "Free Pizza",
                    "Would you like some?",
                    listOf("Yes please", "No thanks", "Maybe later"),
                )
        }

        verifyLog(CODE_DIALOG_SHOWN, Severity.INFO).message shouldBe
            "Option dialog shown: Would you like some?"

        cancelOptionDialog("Free Pizza")

        verifyLog(CODE_DIALOG_CLOSED, Severity.INFO).message shouldBe
            "Option dialog closed - selected Cancel"
        selection shouldBe null
    }

    @Test
    fun `Should handle dismissing an option dialog`() {
        var selection: String? = null
        runAsync {
            selection =
                DialogUtil.showOption(
                    "Free Pizza",
                    "Would you like some?",
                    listOf("Yes please", "No thanks", "Maybe later"),
                )
        }

        verifyLog(CODE_DIALOG_SHOWN, Severity.INFO).message shouldBe
            "Option dialog shown: Would you like some?"

        dismissOptionDialog("Free Pizza")

        verifyLog(CODE_DIALOG_CLOSED, Severity.INFO).message shouldBe "Option dialog closed"
        selection shouldBe null
    }

    @Test
    fun `Should log for INPUT dialogs, with the selection`() {
        dialogFactory.inputSelection = "Camembert"
        DialogUtil.showInput<String>("Cheezoid", "Enter your favourite cheese")

        verifyLog(CODE_DIALOG_SHOWN, Severity.INFO).message shouldBe
            "Input dialog shown: Enter your favourite cheese"
        verifyLog(CODE_DIALOG_CLOSED, Severity.INFO).message shouldBe
            "Input dialog closed - selected Camembert"
    }

    @Test
    fun `Should handle a cancelling directory selection`() {
        var selected: File? = null
        runAsync { selected = DialogUtil.chooseDirectory(null) }

        getFileChooser("Select").clickCancel(async = true)

        verifyLog(CODE_DIALOG_SHOWN, Severity.INFO).message shouldBe "File selector dialog shown: "
        verifyLog(CODE_DIALOG_CLOSED, Severity.INFO).message shouldBe "File selector dialog closed"

        selected.shouldBeNull()
    }

    @Test
    fun `Should be able to select a directory`() {
        val f = File(TEST_ROOT)

        var selected: File? = null
        runAsync { selected = DialogUtil.chooseDirectory(null) }

        selectFile(f.absolutePath, "Select")

        verifyLog(CODE_DIALOG_SHOWN, Severity.INFO).message shouldBe "File selector dialog shown: "
        verifyLog(CODE_DIALOG_CLOSED, Severity.INFO).message shouldBe
            "File selector dialog closed - selected ${f.absolutePath}"

        selected shouldBe File(f.absolutePath)
    }

    @Test
    fun `Should show a custom error message`() {
        val component = JLabel("My custom message")

        runAsync { DialogUtil.showCustomError(component) }

        val log = verifyLog(CODE_DIALOG_SHOWN)
        log.message shouldBe "CustomError dialog shown: ?"

        val dlg = getErrorDialog()
        dlg.shouldBeVisible()
        dlg.getChild<JLabel>(text = "My custom message") shouldBe component
        dlg.clickOk(async = true)

        verifyLog(CODE_DIALOG_CLOSED).message shouldBe "CustomError dialog closed"
    }
}
