package dartzee.screen.game.scorer

import dartzee.*
import dartzee.core.util.TableUtil
import dartzee.helper.AbstractTest
import dartzee.`object`.Dart
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JLabel
import javax.swing.JTable

class TestGolfDartRenderer : AbstractTest()
{
    @Test
    fun `Should replace dart values with their golf score`()
    {
        checkDartConversion(drtDoubleOne(), 1)
        checkDartConversion(drtOuterOne(), 4)
        checkDartConversion(drtTrebleOne(), 2)
        checkDartConversion(drtInnerOne(), 3)
        checkDartConversion(drtOuterEighteen(), 5)
    }
    private fun checkDartConversion(dart: Dart, expectedValue: Int)
    {
        val model = TableUtil.DefaultModel()
        model.addColumn("")
        model.addColumn("Dart")
        model.addRow(arrayOf(1, dart))

        val table = JTable()
        table.model = model

        val renderer = GolfDartRenderer(false)
        val c = renderer.getTableCellRendererComponent(table, dart, false, false, 0, 0) as JLabel
        c.text shouldBe "$expectedValue"
    }
}