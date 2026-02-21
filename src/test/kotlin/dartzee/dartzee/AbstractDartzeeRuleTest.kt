package dartzee.dartzee

import dartzee.dartzee.dart.AbstractDartzeeDartRule
import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

abstract class AbstractDartzeeRuleTest<E : AbstractDartzeeRule> : AbstractTest() {
    abstract fun factory(): E

    open val emptyIsValid = true

    @Test
    fun `Should be parsable from an atomic tag`() {
        val rule = factory()
        val tag = "<${rule.getRuleIdentifier()}/>"

        val parsedRule = parseRule(tag, getRuleList())!!
        parsedRule.getRuleIdentifier() shouldBe rule.getRuleIdentifier()
    }

    @Test
    open fun `Validate empty rule`() {
        val rule = factory()
        rule.validate().isEmpty() shouldBe emptyIsValid
    }

    @Test
    fun `Should be in the correct rule list`() {
        val rule = factory()
        getRuleList().filter { rule.javaClass.isInstance(it) } shouldHaveSize 1
    }

    private fun getRuleList(): List<AbstractDartzeeRule> {
        return if (factory() is AbstractDartzeeDartRule) {
            getAllDartRules()
        } else {
            getAllAggregateRules()
        }
    }
}
