package dartzee.core.bean

import com.github.alyssaburlton.swingtest.doClick
import com.github.alyssaburlton.swingtest.doHover
import com.github.alyssaburlton.swingtest.doHoverAway
import dartzee.core.helper.getIconImage
import dartzee.core.helper.verifyNotCalled
import dartzee.core.util.InjectedDesktopCore
import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Cursor

class TestColourPicker: AbstractTest()
{
    @Test
    fun `Should change the cursor to a hand on hover`()
    {
        val cp = ColourPicker()
        cp.doHover()
        cp.cursor.type shouldBe Cursor.HAND_CURSOR

        cp.doHoverAway()
        cp.cursor.type shouldBe Cursor.DEFAULT_CURSOR
    }

    @Test
    fun `Should support updating the current colour`()
    {
        val cp = ColourPicker()
        cp.updateSelectedColor(Color.RED)

        cp.selectedColour shouldBe Color.RED
        val img = cp.getIconImage()
        Color(img.getRGB(10, 10)) shouldBe Color.RED
    }

    @Test
    fun `Should update the colour on mouse click to whatever was selected in the dialog`()
    {
        val mockSelector = mockk<IColourSelector>(relaxed = true)
        every { mockSelector.selectColour(any()) } returns Color.BLUE

        InjectedDesktopCore.colourSelector = mockSelector

        val cp = ColourPicker()
        cp.updateSelectedColor(Color.RED)
        cp.doClick()

        verify { mockSelector.selectColour(Color.RED) }

        cp.selectedColour shouldBe Color.BLUE
        val img = cp.getIconImage()
        Color(img.getRGB(10, 10)) shouldBe Color.BLUE
    }

    @Test
    fun `Should notify its listener if a new colour is selected`()
    {
        val mockSelector = mockk<IColourSelector>(relaxed = true)
        every { mockSelector.selectColour(any()) } returns Color.BLUE
        InjectedDesktopCore.colourSelector = mockSelector

        val listener = mockk<ColourSelectionListener>(relaxed = true)

        val cp = ColourPicker()
        cp.addColourSelectionListener(listener)
        cp.doClick()

        verify(exactly = 1) { listener.colourSelected(Color.BLUE) }
    }

    @Test
    fun `Should not notify its listener if told not to`()
    {
        val listener = mockk<ColourSelectionListener>(relaxed = true)

        val cp = ColourPicker()
        cp.addColourSelectionListener(listener)
        cp.updateSelectedColor(Color.BLUE, notify = false)

        verifyNotCalled { listener.colourSelected(Color.BLUE) }
    }
}