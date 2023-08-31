package dartzee.screen.ai

import com.github.alyssaburlton.swingtest.clickCancel
import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.getChild
import dartzee.ai.AimDart
import dartzee.core.bean.ScrollTable
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.utils.InjectedThings
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JButton

class TestAISetupConfigurationDialog: AbstractTest()
{
    @Test
    fun `Should populate the table with rows from existing hashmap`()
    {
        val map = mutableMapOf(48 to AimDart(16, 1), 100 to AimDart(25, 2))

        val dlg = AISetupConfigurationDialog(map)

        val table = dlg.getChild<ScrollTable>()
        table.rowCount shouldBe 2

        val rows = table.getRows()

        rows.shouldContainExactlyInAnyOrder(
                listOf(48, AimDart(16, 1), 32),
                listOf(100, AimDart(25, 2), 50)
        )
    }

    @Test
    fun `Should support removing setup rules`()
    {
        val dlg = AISetupConfigurationDialog(mutableMapOf(48 to AimDart(16, 1)))

        val table = dlg.getChild<ScrollTable>()
        table.rowCount shouldBe 1

        table.selectRow(0)
        dlg.clickChild<JButton>(text = "Remove")

        table.rowCount shouldBe 0
    }

    @Test
    fun `Should show an error if removing rules with no selection`()
    {
        val dlg = AISetupConfigurationDialog(mutableMapOf(48 to AimDart(16, 1)))

        dlg.clickChild<JButton>(text = "Remove")

        dialogFactory.errorsShown.shouldContainExactly("You must select row(s) to remove.")
        dlg.getChild<ScrollTable>().rowCount shouldBe 1
    }

    @Test
    fun `Should support adding a new rule to the table`()
    {
        InjectedThings.aiSetupRuleFactory = MockAiSetupRuleFactory()
        val dlg = AISetupConfigurationDialog(mutableMapOf(48 to AimDart(16, 1)))

        dlg.clickChild<JButton>(text = "Add Rule...")


        val table = dlg.getChild<ScrollTable>()
        table.rowCount shouldBe 2

        val rows = table.getRows()
        rows.shouldContainExactlyInAnyOrder(
            listOf(48, AimDart(16, 1), 32),
            listOf(10, AimDart(2, 1), 8)
        )
    }

    @Test
    fun `Should commit modifications to the hash map when Ok is pressed`()
    {
        InjectedThings.aiSetupRuleFactory = MockAiSetupRuleFactory()
        val map = mutableMapOf(48 to AimDart(16, 1))
        val dlg = AISetupConfigurationDialog(map)

        dlg.clickChild<JButton>(text = "Add Rule...")
        dlg.clickOk()

        map.shouldContainExactly(mapOf(48 to AimDart(16, 1), 10 to AimDart(2, 1)))
    }

    @Test
    fun `Should discard modifications when cancelled`()
    {
        InjectedThings.aiSetupRuleFactory = MockAiSetupRuleFactory()
        val map = mutableMapOf(48 to AimDart(16, 1))
        val dlg = AISetupConfigurationDialog(map)

        dlg.clickChild<JButton>(text = "Add Rule...")
        dlg.clickCancel()

        map.shouldContainExactly(mapOf(48 to AimDart(16, 1)))
    }

    private class MockAiSetupRuleFactory: IAISetupRuleFactory
    {
        override fun newSetupRule(currentRules: MutableMap<Int, AimDart>) {
            currentRules[10] = AimDart(2, 1)
        }
    }
}