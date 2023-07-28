package dartzee.core.screen

import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.clickCancel
import com.github.alyssaburlton.swingtest.clickOk
import dartzee.helper.AbstractTest
import dartzee.helper.logger
import dartzee.logging.LoggingCode
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JButton

class TestSimpleDialog: AbstractTest()
{
    var allowCancel = true

    @Test
    fun `Should show or hide the cancel button as appropriate`()
    {
        allowCancel = true
        val dlg = SimpleDialogTestExtension()
        dlg.getChild<JButton>(text = "Cancel").isVisible shouldBe true

        allowCancel = false
        val dlg2 = SimpleDialogTestExtension()
        dlg2.getChild<JButton>(text = "Cancel").isVisible shouldBe false
    }

    @Test
    fun `Pressing cancel should dispose the dialog by default`()
    {
        allowCancel = true

        val dlg = SimpleDialogTestExtension()
        dlg.isVisible = true

        dlg.clickCancel()

        dlg.isVisible shouldBe false
    }

    @Test
    fun `Pressing ok should do whatever has been implemented`()
    {
        val dlg = SimpleDialogTestExtension()

        dlg.clickOk()

        verifyLog(LoggingCode("OkPressed"))
    }

    inner class SimpleDialogTestExtension: SimpleDialog()
    {
        override fun okPressed()
        {
            logger.info(LoggingCode("OkPressed"), "pressed ok")
        }

        override fun allowCancel() = allowCancel

    }

}