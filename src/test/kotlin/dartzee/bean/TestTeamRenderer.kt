package dartzee.bean

import dartzee.helper.AbstractTest
import dartzee.utils.DartsColour
import dartzee.utils.translucent
import io.kotest.matchers.shouldBe
import java.awt.Color
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer
import org.junit.jupiter.api.Test

class TestTeamRenderer : AbstractTest() {
    private val defaultRenderer = DefaultTableCellRenderer()
    private val table = JTable()
    private val value = "foo"

    @Test
    fun `Should do nothing if teams disabled`() {
        val renderer = TeamRenderer(defaultRenderer) { false }

        val c = renderer.getRendererComponent()
        val defaultC = defaultRenderer.getRendererComponent()

        c.background shouldBe defaultC.background
        c.foreground shouldBe defaultC.foreground
        c.border shouldBe defaultC.border
    }

    @Test
    fun `Should do nothing if row out of bounds`() {
        val renderer = TeamRenderer(defaultRenderer) { true }

        val c = renderer.getRendererComponent(row = 12)
        val defaultC = defaultRenderer.getRendererComponent()

        c.background shouldBe defaultC.background
        c.foreground shouldBe defaultC.foreground
        c.border shouldBe defaultC.border
    }

    @Test
    fun `Should render the correct colours based on row index`() {
        val renderer = TeamRenderer(defaultRenderer) { true }

        checkColoursForRowIndex(renderer, 0, Color.RED)
        checkColoursForRowIndex(renderer, 1, Color.RED)
        checkColoursForRowIndex(renderer, 2, Color.GREEN)
        checkColoursForRowIndex(renderer, 3, Color.GREEN)
        checkColoursForRowIndex(renderer, 4, Color.CYAN)
        checkColoursForRowIndex(renderer, 5, Color.CYAN)
        checkColoursForRowIndex(renderer, 6, Color.YELLOW)
        checkColoursForRowIndex(renderer, 7, Color.YELLOW)
        checkColoursForRowIndex(renderer, 8, DartsColour.PURPLE)
        checkColoursForRowIndex(renderer, 9, DartsColour.PURPLE)
        checkColoursForRowIndex(renderer, 10, DartsColour.ORANGE)
        checkColoursForRowIndex(renderer, 11, DartsColour.ORANGE)
    }

    private fun checkColoursForRowIndex(renderer: TeamRenderer, rowIndex: Int, rawColour: Color) {
        renderer.getRendererComponent(row = rowIndex, isSelected = true).background shouldBe
            rawColour
        renderer.getRendererComponent(row = rowIndex, isSelected = false).background shouldBe
            rawColour.translucent()
    }

    private fun TableCellRenderer.getRendererComponent(
        isSelected: Boolean = false,
        hasFocus: Boolean = false,
        row: Int = 0,
        column: Int = 0,
    ) = getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column) as JComponent
}
