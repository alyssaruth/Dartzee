package dartzee.bean

import dartzee.dartzee.aggregate.DartzeeTotalRuleEqualTo
import dartzee.dartzee.getAllAggregateRules
import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeAggregateRuleSelector : AbstractTest() {
    @Test
    fun `Should initialise with all the total rules`() {
        val selector = DartzeeAggregateRuleSelector("")
        selector.getRules().size shouldBe getAllAggregateRules().size
    }

    @Test
    fun `Should be optional`() {
        val selector = DartzeeAggregateRuleSelector("")
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
    fun `Should enable or disable all children`() {
        val selector = DartzeeAggregateRuleSelector("")
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
