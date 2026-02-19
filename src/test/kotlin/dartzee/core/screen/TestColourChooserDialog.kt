package dartzee.core.screen

import com.github.alyssaburlton.swingtest.clickCancel
import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import java.awt.Color
import org.junit.jupiter.api.Test

class TestColourChooserDialog : AbstractTest() {
    @Test
    fun `Should update selectedColour and dispose on Ok`() {
        val dlg = ColourChooserDialog()
        dlg.isVisible = true
        dlg.initialColour = Color.RED
        dlg.selectedColour = Color.BLACK

        dlg.colourChooser.color = Color.YELLOW
        dlg.clickOk()

        dlg.selectedColour shouldBe Color.YELLOW
        dlg.shouldNotBeVisible()
    }

    @Test
    fun `Should set selectedColour back to initialColour and dispose on Cancel`() {
        val dlg = ColourChooserDialog()
        dlg.isVisible = true
        dlg.initialColour = Color.RED
        dlg.selectedColour = Color.BLACK

        dlg.colourChooser.color = Color.YELLOW
        dlg.clickCancel()

        dlg.selectedColour shouldBe Color.RED
        dlg.shouldNotBeVisible()
    }
}
