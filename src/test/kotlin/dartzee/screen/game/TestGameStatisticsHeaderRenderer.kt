package dartzee.screen.game

import dartzee.helper.AbstractTest
import dartzee.shouldHaveBorderThickness
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import java.awt.Color
import javax.swing.JComponent
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class TestGameStatisticsHeaderRenderer: AbstractTest()
{
    private fun GameStatisticsHeaderRenderer.apply(column: Int): JComponent
    {
        val tm = DefaultTableModel()
        tm.addColumn("")
        tm.addColumn("Player 1")
        tm.addColumn("Player 2")
        tm.addColumn("Player 3")

        return getTableCellRendererComponent(JTable(tm), "Foo", false, false, -1, column) as JComponent
    }

    @Test
    fun `Should set correct borders`()
    {
        val renderer = GameStatisticsHeaderRenderer()

        renderer.apply(0).shouldHaveBorderThickness(0, 1, 0, 2)
        renderer.apply(1).shouldHaveBorderThickness(1, 1, 2, 2)
        renderer.apply(2).shouldHaveBorderThickness(1, 1, 2, 2)
        renderer.apply(3).shouldHaveBorderThickness(1, 2, 2, 2)
    }

    @Test
    fun `Should make first header column transparent, leaving the rest white`()
    {
        val firstHeader = GameStatisticsHeaderRenderer().apply(0)

        firstHeader.isOpaque shouldBe false
        firstHeader.background shouldBe Color(0, 0, 0, 0)

        val otherHeader = GameStatisticsHeaderRenderer().apply(1)
        otherHeader.isOpaque shouldBe true
        otherHeader.background.alpha shouldNotBe 0
    }

}
