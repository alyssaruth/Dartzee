package dartzee.bean

import dartzee.core.bean.findByClass
import dartzee.core.bean.items
import dartzee.dartzee.dart.AbstractDartzeeDartRule
import dartzee.dartzee.dart.DartzeeDartRuleColour
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.dartzee.dart.DartzeeDartRuleInner
import dartzee.dartzee.dart.DartzeeDartRuleScore
import dartzee.dartzee.getAllDartRules
import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class TestAbstractDartzeeRuleSelector : AbstractTest() {
    @Test
    fun `Should render the description passed to it`() {
        val selector = FakeDartzeeRuleSelector("foo")
        selector.lblDesc.text shouldBe "foo"
        selector.cbDesc.text shouldBe "foo"
    }

    @Test
    fun `Should initialise with the options specified`() {
        val selector = FakeDartzeeRuleSelector("foo")

        val items = selector.comboBoxRuleType.items()

        getAllDartRules().forEach { rule ->
            items.find { rule.javaClass.isInstance(it) }.shouldNotBeNull()
        }
    }

    @Test
    fun `Should show an error if the rule is invalid`() {
        val selector = FakeDartzeeRuleSelector("foo")
        selector.populate(DartzeeDartRuleColour())

        selector.valid() shouldBe false
        dialogFactory.errorsShown shouldContain "foo: You must select at least one colour."
    }

    @Test
    fun `Should pass validation if the rule is valid`() {
        val selector = FakeDartzeeRuleSelector("foo")
        selector.populate(DartzeeDartRuleEven())

        selector.valid() shouldBe true
        dialogFactory.errorsShown.shouldBeEmpty()
    }

    @Test
    fun `Should populate from an existing rule successfully`() {
        val selector = DartzeeDartRuleSelector("foo")
        selector.populate(DartzeeDartRuleInner())
        selector.getSelection().shouldBeInstanceOf<DartzeeDartRuleInner>()
    }

    @Test
    fun `Should populate a rule's configPanel successfully`() {
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
    fun `Should swap in and out the configPanel based on the selected rule`() {
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

    private class FakeDartzeeRuleSelector(desc: String) :
        AbstractDartzeeRuleSelector<AbstractDartzeeDartRule>(desc) {
        override fun getRules() = getAllDartRules()
    }
}
