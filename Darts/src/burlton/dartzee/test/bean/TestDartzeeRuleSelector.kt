package burlton.dartzee.test.bean

import burlton.dartzee.code.bean.DartzeeRuleSelector
import burlton.dartzee.code.dartzee.DartzeeDartRuleColour
import burlton.dartzee.code.dartzee.DartzeeDartRuleEven
import burlton.dartzee.code.dartzee.DartzeeDartRuleScore
import burlton.dartzee.code.dartzee.getAllDartRules
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.desktopcore.code.bean.findByClass
import burlton.desktopcore.code.bean.items
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeRuleSelector : AbstractDartsTest()
{
    @Test
    fun `Should render the description passed to it`()
    {
        val selector = DartzeeRuleSelector("foo")
        selector.lblDesc.text shouldBe "foo"
    }

    @Test
    fun `Should initialise with all valid options`()
    {
        val selector = DartzeeRuleSelector("foo")

        val items = selector.comboBoxRuleType.items()
        getAllDartRules().forEach { rule ->
            items.find { rule.javaClass.isInstance(it) }
        }
    }

    @Test
    fun `Should show an error if the rule is invalid`()
    {
        val selector = DartzeeRuleSelector("foo")
        selector.populate(DartzeeDartRuleColour().toDbString())

        selector.valid() shouldBe false
        dialogFactory.errorsShown shouldContain "foo: You must select at least one colour."
    }

    @Test
    fun `Should pass validation if the rule is valid`()
    {
        val selector = DartzeeRuleSelector("foo")
        selector.populate(DartzeeDartRuleEven().toDbString())

        selector.valid() shouldBe true
        dialogFactory.errorsShown.shouldBeEmpty()
    }

    @Test
    fun `Should swap in and out the configPanel based on the selected rule`()
    {
        val selector = DartzeeRuleSelector("foo")
        val comboBox = selector.comboBoxRuleType

        val item = comboBox.findByClass<DartzeeDartRuleScore>()!!
        comboBox.selectedItem = item

        val configPanel = item.configPanel
        configPanel.parent shouldBe selector

        val otherItem = comboBox.findByClass<DartzeeDartRuleEven>()!!
        comboBox.selectedItem = otherItem
        configPanel.parent shouldBe null
    }
}