package dartzee.screen.game

import dartzee.helper.AbstractTest
import dartzee.shouldHaveBorderThickness
import dartzee.shouldHaveColours
import dartzee.utils.DartsColour
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.Color
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class TestGameStatisticsCellRenderer: AbstractTest()
{
    private fun makeGameStatisticsCellRenderer(sectionStarts: List<String> = emptyList(),
                                               highestWins: List<String> = emptyList(),
                                               lowestWins: List<String> = emptyList(),
                                               histogramRows: List<String> = emptyList()) =
        GameStatisticsCellRenderer(sectionStarts, highestWins, lowestWins, histogramRows)

    private fun GameStatisticsCellRenderer.apply(tm: DefaultTableModel, row: Int, col: Int) =
        getTableCellRendererComponent(JTable(tm), null, false, false, row, col) as JComponent

    private fun prepareTableModel(playerCount: Int): DefaultTableModel
    {
        val tm = DefaultTableModel()
        tm.addColumn("")

        (0 until playerCount).forEach { tm.addColumn("Player $it") }

        return tm
    }

    @Test
    fun `Should colour highest wins rows correctly`()
    {
        val tm = prepareTableModel(6)
        tm.addRow(arrayOf("Example", 4, 2, 3, 5, 1, 0))

        val renderer = makeGameStatisticsCellRenderer(highestWins = listOf("Example"))

        renderer.apply(tm, 0, 1).shouldHaveColours(DartsColour.SECOND_COLOURS)
        renderer.apply(tm, 0, 2).shouldHaveColours(DartsColour.FOURTH_COLOURS)
        renderer.apply(tm, 0, 3).shouldHaveColours(DartsColour.THIRD_COLOURS)
        renderer.apply(tm, 0, 4).shouldHaveColours(DartsColour.FIRST_COLOURS)
        renderer.apply(tm, 0, 5).shouldHaveColours(DartsColour.FOURTH_COLOURS)
        renderer.apply(tm, 0, 6).shouldHaveColours(DartsColour.FOURTH_COLOURS)
    }

    @Test
    fun `Should colour lowest wins rows correctly`()
    {
        val tm = prepareTableModel(6)
        tm.addRow(arrayOf("Example", 4, 2, 3, 5, 1, 0))

        val renderer = makeGameStatisticsCellRenderer(lowestWins = listOf("Example"))

        renderer.apply(tm, 0, 1).shouldHaveColours(DartsColour.FOURTH_COLOURS)
        renderer.apply(tm, 0, 2).shouldHaveColours(DartsColour.THIRD_COLOURS)
        renderer.apply(tm, 0, 3).shouldHaveColours(DartsColour.FOURTH_COLOURS)
        renderer.apply(tm, 0, 4).shouldHaveColours(DartsColour.FOURTH_COLOURS)
        renderer.apply(tm, 0, 5).shouldHaveColours(DartsColour.SECOND_COLOURS)
        renderer.apply(tm, 0, 6).shouldHaveColours(DartsColour.FIRST_COLOURS)
    }

    @Test
    fun `Should colour highest wins rows correctly when there is a tie`()
    {
        val tm = prepareTableModel(4)
        tm.addRow(arrayOf("Example", 4, 2, 4, 5))

        val renderer = makeGameStatisticsCellRenderer(highestWins = listOf("Example"))

        renderer.apply(tm, 0, 1).shouldHaveColours(DartsColour.SECOND_COLOURS)
        renderer.apply(tm, 0, 2).shouldHaveColours(DartsColour.FOURTH_COLOURS)
        renderer.apply(tm, 0, 3).shouldHaveColours(DartsColour.SECOND_COLOURS)
        renderer.apply(tm, 0, 4).shouldHaveColours(DartsColour.FIRST_COLOURS)
    }

    @Test
    fun `Should set correct edge borders`()
    {
        val tm = prepareTableModel(2)
        tm.addRow(arrayOf("Row 0", 3, 1))
        tm.addRow(arrayOf("Row 1", 2, 0))

        val renderer = makeGameStatisticsCellRenderer()
        renderer.apply(tm, 0, 0).shouldHaveBorderThickness(2, 1, 0, 0)
        renderer.apply(tm, 0, 1).shouldHaveBorderThickness(1, 1, 0, 0)
        renderer.apply(tm, 0, 2).shouldHaveBorderThickness(1, 2, 0, 0)
    }

    @Test
    fun `Should additionally set top borders if row is start of section`()
    {
        val tm = prepareTableModel(2)
        tm.addRow(arrayOf("Row 0", 3, 1))
        tm.addRow(arrayOf("Row 1", 2, 0))
        tm.addRow(arrayOf("Row 2", 5, 7))

        val renderer = makeGameStatisticsCellRenderer(sectionStarts = listOf("Row 1"))
        renderer.apply(tm, 1, 0).shouldHaveBorderThickness(2, 1, 2, 0)
        renderer.apply(tm, 1, 1).shouldHaveBorderThickness(1, 1, 2, 0)
        renderer.apply(tm, 1, 2).shouldHaveBorderThickness(1, 2, 2, 0)
    }

    @Test
    fun `Should additionally set bottom borders for the last row`()
    {
        val tm = prepareTableModel(2)
        tm.addRow(arrayOf("Row 0", 3, 1))
        tm.addRow(arrayOf("Row 1", 2, 0))
        tm.addRow(arrayOf("Row 2", 5, 7))

        val renderer = makeGameStatisticsCellRenderer()
        renderer.apply(tm, 2, 0).shouldHaveBorderThickness(2, 1, 0, 2)
        renderer.apply(tm, 2, 1).shouldHaveBorderThickness(1, 1, 0, 2)
        renderer.apply(tm, 2, 2).shouldHaveBorderThickness(1, 2, 0, 2)
    }

    @Test
    fun `Should render the title column correctly`()
    {
        val tm = prepareTableModel(2)
        tm.addRow(arrayOf("Row 0", 3, 1))

        val renderer = makeGameStatisticsCellRenderer()

        renderer.apply(tm, 0, 0).shouldHaveColours(Pair(Color.WHITE, null))
        renderer.apply(tm, 0, 0).font.isBold shouldBe true
    }

    @Test
    fun `Should colour histogram rows correctly`()
    {
        val tm = prepareTableModel(2)
        tm.addRow(arrayOf("Irrelevant", 7, 8))
        tm.addRow(arrayOf("Row 1", 2, 1))
        tm.addRow(arrayOf("Row 2", 3, 3))
        tm.addRow(arrayOf("Ignore me", 2, 5))

        val renderer = makeGameStatisticsCellRenderer(histogramRows = listOf("Row 1", "Row 2"))

        //Column 1: 40% - 60%
        renderer.apply(tm, 1, 1).shouldHaveColours(Pair(Color.getHSBColor(0.5.toFloat(), 0.4f, 1f) , null))
        renderer.apply(tm, 2, 1).shouldHaveColours(Pair(Color.getHSBColor(0.5.toFloat(), 0.6f, 1f) , null))

        //Column 2: 25% - 75%
        renderer.apply(tm, 1, 2).shouldHaveColours(Pair(Color.getHSBColor(0.5.toFloat(), 0.25f, 1f) , null))
        renderer.apply(tm, 2, 2).shouldHaveColours(Pair(Color.getHSBColor(0.5.toFloat(), 0.75f, 1f) , null))
    }
}