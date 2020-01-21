package burlton.dartzee.test.core.bean

import burlton.dartzee.code.core.bean.ColourPicker
import burlton.dartzee.code.core.bean.ColourSelectionListener
import burlton.dartzee.code.core.bean.IColourSelector
import burlton.dartzee.code.core.util.InjectedDesktopCore
import burlton.dartzee.test.helper.AbstractTest
import burlton.dartzee.test.core.helper.getIconImage
import burlton.dartzee.test.core.helper.makeMouseEvent
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.awt.Color
import java.awt.Cursor

class TestColourPicker: AbstractTest()
{
    @Test
    fun `Should change the cursor to a hand on hover`()
    {
        val cp = ColourPicker()
        cp.mouseEntered(makeMouseEvent())
        cp.cursor.type shouldBe Cursor.HAND_CURSOR

        cp.mouseExited(makeMouseEvent())
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
        cp.mouseClicked(makeMouseEvent())

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
        cp.mouseClicked(makeMouseEvent())

        verify { listener.colourSelected(Color.BLUE) }
    }
}