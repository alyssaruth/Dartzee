package dartzee.core.screen

import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.awt.Color

class TestColourChooserDialog: AbstractTest()
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