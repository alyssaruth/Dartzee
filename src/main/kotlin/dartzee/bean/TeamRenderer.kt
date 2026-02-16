package dartzee.bean

import dartzee.utils.DartsColour
import dartzee.utils.translucent
import java.awt.Color
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder
import javax.swing.table.TableCellRenderer

class TeamRenderer(
    private val baseRenderer: TableCellRenderer,
    private val teamsEnabled: () -> Boolean,
) : TableCellRenderer {
    private val colors =
        listOf(
            Color.RED,
            Color.GREEN,
            Color.CYAN,
            Color.YELLOW,
            DartsColour.PURPLE,
            DartsColour.ORANGE,
        )

    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
        val c =
            baseRenderer.getTableCellRendererComponent(
                table,
                value,
                isSelected,
                hasFocus,
                row,
                column,
            ) as JComponent

        val inBounds = row / 2 < colors.size
        if (teamsEnabled() && inBounds) {
            val rawColour = colors[row / 2]
            c.background = if (isSelected) rawColour else rawColour?.translucent()
            c.foreground = Color.BLACK

            val padding = if (column == 0) 0 else 5
            val lineBorder = MatteBorder(0, 0, row % 2, 0, Color.BLACK)
            val padBorder = EmptyBorder(0, padding, 0, 0)
            c.border = CompoundBorder(lineBorder, padBorder)
        } else if (!isSelected) {
            c.background = null
        }

        return c
    }
}
