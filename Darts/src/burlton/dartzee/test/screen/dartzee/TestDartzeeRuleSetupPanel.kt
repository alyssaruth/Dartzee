package burlton.dartzee.test.screen.dartzee

import burlton.dartzee.code.dartzee.DartzeeRuleFactory
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleEven
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOdd
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOuter
import burlton.dartzee.code.screen.dartzee.DartzeeRuleSetupPanel
import burlton.dartzee.code.utils.InjectedThings
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.FakeDartzeeRuleFactory
import burlton.dartzee.test.helper.makeDartzeeRuleCalculationResult
import burlton.dartzee.test.helper.makeDartzeeRuleDto
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeRuleSetupPanel: AbstractDartsTest()
{
    override fun afterEachTest()
    {
        super.afterEachTest()

        InjectedThings.dartzeeRuleFactory = DartzeeRuleFactory()
    }

    @Test
    fun `should allow pre-populated rules to be added`()
    {
        val rule = makeDartzeeRuleDto(DartzeeDartRuleEven())
        val ruleTwo = makeDartzeeRuleDto(DartzeeDartRuleOdd())

        val panel = DartzeeRuleSetupPanel()
        panel.addRulesToTable(listOf(rule, ruleTwo))
        panel.tableRules.rowCount shouldBe 2

        panel.getRules() shouldBe listOf(rule, ruleTwo)
    }

    @Test
    fun `should sort rules into difficulty order`()
    {
        val tenPercentRule = makeDartzeeRuleDto(DartzeeDartRuleEven(), calculationResult = makeDartzeeRuleCalculationResult(10))
        val twentyPercentRule = makeDartzeeRuleDto(DartzeeDartRuleOdd(), calculationResult = makeDartzeeRuleCalculationResult(20))
        val thirtyPercentRule = makeDartzeeRuleDto(DartzeeDartRuleOuter(), calculationResult = makeDartzeeRuleCalculationResult(30))

        val panel = DartzeeRuleSetupPanel()
        panel.addRulesToTable(listOf(tenPercentRule, thirtyPercentRule, twentyPercentRule))

        panel.btnCalculateOrder.doClick()

        panel.getRules().shouldContainExactly(thirtyPercentRule, twentyPercentRule, tenPercentRule)
    }

    @Test
    fun `Should toggle amend & remove buttons based on whether a row is selected`()
    {
        val rule = makeDartzeeRuleDto()

        val panel = DartzeeRuleSetupPanel()
        panel.addRulesToTable(listOf(rule))

        panel.btnAmendRule.isEnabled shouldBe false
        panel.btnRemoveRule.isEnabled shouldBe false

        panel.tableRules.selectRow(0)
        panel.btnAmendRule.isEnabled shouldBe true
        panel.btnRemoveRule.isEnabled shouldBe true

        panel.tableRules.selectRow(-1)
        panel.btnAmendRule.isEnabled shouldBe false
        panel.btnRemoveRule.isEnabled shouldBe false
    }

    @Test
    fun `Should remove the selected rule from the table`()
    {
        val rule = makeDartzeeRuleDto(DartzeeDartRuleEven())
        val ruleTwo = makeDartzeeRuleDto(DartzeeDartRuleOdd())

        val panel = DartzeeRuleSetupPanel()
        panel.addRulesToTable(listOf(rule, ruleTwo))

        panel.tableRules.selectRow(1)
        panel.btnRemoveRule.doClick()

        panel.getRules() shouldBe listOf(rule)
    }

    @Test
    fun `Should support creation of rules`()
    {
        val newRule = makeDartzeeRuleDto()
        InjectedThings.dartzeeRuleFactory = FakeDartzeeRuleFactory(newRule)

        val panel = DartzeeRuleSetupPanel()
        panel.btnAddRule.doClick()

        panel.tableRules.rowCount shouldBe 1
        panel.getRules() shouldBe listOf(newRule)
    }

    @Test
    fun `Should handle cancelling a new rule`()
    {
        InjectedThings.dartzeeRuleFactory = FakeDartzeeRuleFactory(null)

        val panel = DartzeeRuleSetupPanel()
        panel.btnAddRule.doClick()

        panel.tableRules.rowCount shouldBe 0
        panel.getRules().shouldBeEmpty()
    }

    @Test
    fun `Should handle amending a rule`()
    {
        val originalRule = makeDartzeeRuleDto(DartzeeDartRuleEven())
        val newRule = makeDartzeeRuleDto(DartzeeDartRuleOdd())

        InjectedThings.dartzeeRuleFactory = FakeDartzeeRuleFactory(newRule)

        val panel = DartzeeRuleSetupPanel()
        panel.addRulesToTable(listOf(originalRule))
        panel.tableRules.selectRow(0)
        panel.btnAmendRule.doClick()

        panel.tableRules.rowCount shouldBe 1
        panel.getRules() shouldBe listOf(newRule)
    }
}