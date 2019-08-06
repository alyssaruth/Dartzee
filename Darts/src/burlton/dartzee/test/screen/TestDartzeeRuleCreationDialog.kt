package burlton.dartzee.test.screen

import burlton.dartzee.code.bean.DartzeeRuleSelector
import burlton.dartzee.code.dartzee.DartzeeDartRuleEven
import burlton.dartzee.code.dartzee.DartzeeDartRuleInner
import burlton.dartzee.code.dartzee.DartzeeDartRuleOuter
import burlton.dartzee.code.screen.DartzeeRuleCreationDialog
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.desktopcore.code.util.getAllChildComponentsForType
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeRuleCreationDialog : AbstractDartsTest()
{
    @Test
    fun `Should not return a rule when cancelled`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.btnCancel.doClick()
        dlg.dartzeeRule shouldBe null
    }

    @Test
    fun `Should toggle the rule selectors based on radio selection`()
    {
        val dlg = DartzeeRuleCreationDialog()

        var children = getAllChildComponentsForType(dlg, DartzeeRuleSelector::class.java)
        children.shouldContainExactly(dlg.dartOneSelector, dlg.dartTwoSelector, dlg.dartThreeSelector)

        dlg.rdbtnAtLeastOne.doClick()
        children = getAllChildComponentsForType(dlg, DartzeeRuleSelector::class.java)
        children.shouldContainExactly(dlg.targetSelector)

        dlg.rdbtnAllDarts.doClick()
        children = getAllChildComponentsForType(dlg, DartzeeRuleSelector::class.java)
        children.shouldContainExactly(dlg.dartOneSelector, dlg.dartTwoSelector, dlg.dartThreeSelector)
    }

    @Test
    fun `Should populate an 'at least one' rule correctly`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.rdbtnAtLeastOne.doClick()
        dlg.targetSelector.comboBoxRuleType.selectedItem = DartzeeDartRuleOuter()
        dlg.btnOk.doClick()

        val rule = dlg.dartzeeRule!!

        rule.dart1Rule shouldBe DartzeeDartRuleOuter()
        rule.dart2Rule shouldBe null
        rule.dart3Rule shouldBe null
        rule.inOrder shouldBe false
    }

    @Test
    fun `Should populate an 'all darts' rule correctly`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.rdbtnAllDarts.doClick()
        dlg.dartOneSelector.comboBoxRuleType.selectedItem = DartzeeDartRuleOuter()
        dlg.dartTwoSelector.comboBoxRuleType.selectedItem = DartzeeDartRuleInner()
        dlg.dartThreeSelector.comboBoxRuleType.selectedItem = DartzeeDartRuleEven()
        dlg.btnOk.doClick()

        val rule = dlg.dartzeeRule!!

        rule.dart1Rule shouldBe DartzeeDartRuleOuter()
        rule.dart2Rule shouldBe DartzeeDartRuleInner()
        rule.dart3Rule shouldBe DartzeeDartRuleEven()
        rule.inOrder shouldBe false
    }

    @Test
    fun `Should populate in order correctly when checked`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.cbInOrder.doClick()
        dlg.btnOk.doClick()

        val rule = dlg.dartzeeRule!!
        rule.inOrder shouldBe true
    }
}