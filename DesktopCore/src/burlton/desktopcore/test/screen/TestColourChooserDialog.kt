package burlton.desktopcore.test.screen

import burlton.desktopcore.code.screen.ColourChooserDialog
import burlton.desktopcore.test.helper.AbstractDesktopTest
import io.kotlintest.shouldBe
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import java.awt.Color

class TestColourChooserDialog: AbstractDesktopTest()
{
    @Test
    fun `Should update selectedColour and dispose on Ok`()
    {
        val dlg = spyk<ColourChooserDialog>()
        dlg.initialColour = Color.RED
        dlg.selectedColour = Color.BLACK

        dlg.colourChooser.color = Color.YELLOW
        dlg.btnOk.doClick()

        dlg.selectedColour shouldBe Color.YELLOW
        verify { dlg.dispose() }
    }

    @Test
    fun `Should set selectedColour back to initialColour and dispose on Cancel`()
    {
        val dlg = spyk<ColourChooserDialog>()
        dlg.initialColour = Color.RED
        dlg.selectedColour = Color.BLACK

        dlg.colourChooser.color = Color.YELLOW
        dlg.btnCancel.doClick()

        dlg.selectedColour shouldBe Color.RED
        verify { dlg.dispose() }
    }
}