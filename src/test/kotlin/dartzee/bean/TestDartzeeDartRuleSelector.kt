package dartzee.bean

import dartzee.dartzee.getAllDartRules
import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeDartRuleSelector : AbstractTest()
{
    @Test
    fun `Should initialise with all the dart rules`()
    {
        val selector = DartzeeDartRuleSelector("")
        selector.getRules().size shouldBe getAllDartRules().size
    }

    @Test
    fun `Should not be optional`()
    {
        val selector = DartzeeDartRuleSelector("")
        selector.isOptional() shouldBe false
        selector.shouldBeEnabled() shouldBe true

        val children = selector.components.toList()
        children.shouldContain(selector.lblDesc)
        children.shouldNotContain(selector.cbDesc)
    }
}