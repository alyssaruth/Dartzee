package dartzee.bean

import dartzee.dartzee.getAllAggregateRules
import dartzee.dartzee.aggregate.DartzeeTotalRuleEqualTo
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldNotContain
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeTotalRuleSelector : AbstractTest()
{
    @Test
    fun `Should initialise with all the total rules`()
    {
        val selector = DartzeeTotalRuleSelector("")
        selector.getRules().size shouldBe getAllAggregateRules().size
    }

    @Test
    fun `Should be optional`()
    {
        val selector = DartzeeTotalRuleSelector("")
        selector.isOptional() shouldBe true
        val children = selector.components.toList()
        children.shouldNotContain(selector.lblDesc)
        children.shouldContain(selector.cbDesc)

        selector.cbDesc.isSelected = true
        selector.shouldBeEnabled() shouldBe true

        selector.cbDesc.isSelected = false
        selector.shouldBeEnabled() shouldBe false
    }

    @Test
    fun `Should enable or disable all children`()
    {
        val selector = DartzeeTotalRuleSelector("")
        selector.populate(DartzeeTotalRuleEqualTo())

        val populatedRule = selector.getSelection() as DartzeeTotalRuleEqualTo

        selector.isEnabled = false
        selector.cbDesc.isEnabled shouldBe true
        selector.comboBoxRuleType.isEnabled shouldBe false
        populatedRule.configPanel.isEnabled shouldBe false
        populatedRule.spinner.isEnabled shouldBe false

        selector.isEnabled = true
        selector.cbDesc.isEnabled shouldBe true
        selector.comboBoxRuleType.isEnabled shouldBe true
        populatedRule.configPanel.isEnabled shouldBe true
        populatedRule.spinner.isEnabled shouldBe true
    }
}