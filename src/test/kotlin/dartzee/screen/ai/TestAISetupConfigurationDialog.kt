package dartzee.screen.ai

import com.github.alexburlton.swingtest.clickChild
import com.github.alexburlton.swingtest.getChild
import dartzee.ai.AimDart
import dartzee.core.bean.ScrollTable
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.utils.InjectedThings
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.maps.shouldContainExactly
import io.kotlintest.shouldBe
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
        dlg.clickChild<JButton>("Remove")

        table.rowCount shouldBe 0
    }

    @Test
    fun `Should show an error if removing rules with no selection`()
    {
        val dlg = AISetupConfigurationDialog(mutableMapOf(48 to AimDart(16, 1)))

        dlg.clickChild<JButton>("Remove")

        dialogFactory.errorsShown.shouldContainExactly("You must select row(s) to remove.")
        dlg.getChild<ScrollTable>().rowCount shouldBe 1
    }

    @Test
    fun `Should support adding a new rule to the table`()
    {
        InjectedThings.aiSetupRuleFactory = MockAiSetupRuleFactory()
        val dlg = AISetupConfigurationDialog(mutableMapOf(48 to AimDart(16, 1)))

        dlg.clickChild<JButton>("Add Rule...")


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

        dlg.clickChild<JButton>("Add Rule...")
        dlg.clickChild<JButton>("Ok")

        map.shouldContainExactly(mapOf(48 to AimDart(16, 1), 10 to AimDart(2, 1)))
    }

    @Test
    fun `Should discard modifications when cancelled`()
    {
        InjectedThings.aiSetupRuleFactory = MockAiSetupRuleFactory()
        val map = mutableMapOf(48 to AimDart(16, 1))
        val dlg = AISetupConfigurationDialog(map)

        dlg.clickChild<JButton>("Add Rule...")
        dlg.clickChild<JButton>("Cancel")

        map.shouldContainExactly(mapOf(48 to AimDart(16, 1)))
    }

    private class MockAiSetupRuleFactory: AbstractAISetupRuleFactory()
    {
        override fun newSetupRule(currentRules: MutableMap<Int, AimDart>) {
            currentRules[10] = AimDart(2, 1)
        }
    }
}