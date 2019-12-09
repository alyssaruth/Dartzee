package burlton.dartzee.test.bean

import burlton.dartzee.code.bean.AbstractDartzeeRuleSelector
import burlton.dartzee.code.bean.DartzeeDartRuleSelector
import burlton.dartzee.code.dartzee.dart.*
import burlton.dartzee.code.dartzee.getAllDartRules
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.desktopcore.code.bean.findByClass
import burlton.desktopcore.code.bean.items
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.Test
import kotlin.test.assertNotNull

class TestAbstractDartzeeRuleSelector: AbstractDartsTest()
{
    @Test
    fun `Should render the description passed to it`()
    {
        val selector = FakeDartzeeRuleSelector("foo")
        selector.lblDesc.text shouldBe "foo"
        selector.cbDesc.text shouldBe "foo"
    }

    @Test
    fun `Should initialise with the options specified`()
    {
        val selector = FakeDartzeeRuleSelector("foo")

        val items = selector.comboBoxRuleType.items()

        getAllDartRules().forEach { rule ->
            assertNotNull(items.find { rule.javaClass.isInstance(it) })
        }
    }

    @Test
    fun `Should show an error if the rule is invalid`()
    {
        val selector = FakeDartzeeRuleSelector("foo")
        selector.populate(DartzeeDartRuleColour())

        selector.valid() shouldBe false
        dialogFactory.errorsShown shouldContain "foo: You must select at least one colour."
    }

    @Test
    fun `Should pass validation if the rule is valid`()
    {
        val selector = FakeDartzeeRuleSelector("foo")
        selector.populate(DartzeeDartRuleEven())

        selector.valid() shouldBe true
        dialogFactory.errorsShown.shouldBeEmpty()
    }

    @Test
    fun `Should populate from an existing rule successfully`()
    {
        val selector = DartzeeDartRuleSelector("foo")
        selector.populate(DartzeeDartRuleInner())
        selector.getSelection().shouldBeInstanceOf<DartzeeDartRuleInner>()
    }

    @Test
    fun `Should populate a rule's configPanel successfully`()
    {
        val selector = DartzeeDartRuleSelector("foo")
        val rule = DartzeeDartRuleColour()
        rule.black = true
        rule.red = true

        selector.populate(rule)

        val populatedRule = selector.getSelection() as DartzeeDartRuleColour
        populatedRule.black shouldBe true
        populatedRule.red shouldBe true
        populatedRule.white shouldBe false
        populatedRule.green shouldBe false
    }

    @Test
    fun `Should swap in and out the configPanel based on the selected rule`()
    {
        val selector = FakeDartzeeRuleSelector("foo")
        val comboBox = selector.comboBoxRuleType

        val item = comboBox.findByClass<DartzeeDartRuleScore>()!!
        comboBox.selectedItem = item

        val configPanel = item.configPanel
        configPanel.parent shouldBe selector

        val otherItem = comboBox.findByClass<DartzeeDartRuleEven>()!!
        comboBox.selectedItem = otherItem
        configPanel.parent shouldBe null
    }

    private class FakeDartzeeRuleSelector(desc: String): AbstractDartzeeRuleSelector<AbstractDartzeeDartRule>(desc)
    {
        override fun getRules() = getAllDartRules()

    }
}