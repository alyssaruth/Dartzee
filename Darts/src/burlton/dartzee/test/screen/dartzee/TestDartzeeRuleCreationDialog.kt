package burlton.dartzee.test.screen.dartzee

import burlton.dartzee.code.bean.DartzeeDartRuleSelector
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleInner
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOdd
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOuter
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCreationDialog
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.desktopcore.code.bean.selectByClass
import burlton.desktopcore.code.util.getAllChildComponentsForType
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.collections.shouldNotContain
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

        var children = getAllChildComponentsForType(dlg, DartzeeDartRuleSelector::class.java)
        children.shouldContainExactlyInAnyOrder(dlg.dartOneSelector, dlg.dartTwoSelector, dlg.dartThreeSelector)
        children.shouldNotContain(dlg.targetSelector)

        dlg.rdbtnAtLeastOne.doClick()
        children = getAllChildComponentsForType(dlg, DartzeeDartRuleSelector::class.java)
        children.shouldContainExactly(dlg.targetSelector)

        dlg.rdbtnAllDarts.doClick()
        children = getAllChildComponentsForType(dlg, DartzeeDartRuleSelector::class.java)
        children.shouldContainExactlyInAnyOrder(dlg.dartOneSelector, dlg.dartTwoSelector, dlg.dartThreeSelector)
        children.shouldNotContain(dlg.targetSelector)
    }

    @Test
    fun `Should populate an 'at least one' rule correctly`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.rdbtnAtLeastOne.doClick()
        dlg.targetSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleOdd>()
        dlg.btnOk.doClick()

        val rule = dlg.dartzeeRule!!

        rule.dart1Rule!!.toDbString() shouldBe DartzeeDartRuleOdd().toDbString()
        rule.dart2Rule shouldBe null
        rule.dart3Rule shouldBe null
        rule.inOrder shouldBe false
    }

    @Test
    fun `Should populate an 'all darts' rule correctly`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.rdbtnAllDarts.isSelected = true
        dlg.cbInOrder.isSelected = false

        dlg.dartOneSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleInner>()
        dlg.dartTwoSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleOuter>()
        dlg.dartThreeSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleOdd>()
        dlg.btnOk.doClick()

        val rule = dlg.dartzeeRule!!

        rule.dart1Rule!!.toDbString() shouldBe DartzeeDartRuleInner().toDbString()
        rule.dart2Rule!!.toDbString() shouldBe DartzeeDartRuleOuter().toDbString()
        rule.dart3Rule!!.toDbString() shouldBe DartzeeDartRuleOdd().toDbString()
        rule.inOrder shouldBe false
    }

    @Test
    fun `Should populate in order correctly when checked`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.btnOk.doClick()

        val rule = dlg.dartzeeRule!!
        rule.inOrder shouldBe true
    }
}