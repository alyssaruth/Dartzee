package burlton.dartzee.test.screen.dartzee

import burlton.core.test.helper.verifyNotCalled
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleEven
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOdd
import burlton.dartzee.code.db.DARTZEE_TEMPLATE
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.screen.dartzee.DartzeeTemplateDialog
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.insertDartzeeTemplate
import burlton.dartzee.test.helper.makeDartzeeRuleCalculationResult
import burlton.dartzee.test.helper.makeDartzeeRuleDto
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import kotlin.test.assertNotNull

class TestDartzeeTemplateDialog: AbstractDartsTest()
{
    @Test
    fun `Should show an error if template name not specified`()
    {
        val dialog = spyk<DartzeeTemplateDialog>()

        dialog.btnOk.doClick()

        dialogFactory.errorsShown.shouldContainExactly("You must enter a name.")
        verifyNotCalled { dialog.dispose() }
    }

    @Test
    fun `Should show an error if 0 rules are specified`()
    {
        val dialog = spyk<DartzeeTemplateDialog>()

        dialog.tfName.text = "My template"
        dialog.btnOk.doClick()

        dialogFactory.errorsShown.shouldContainExactly("You must create at least 2 rules.")
        verifyNotCalled { dialog.dispose() }
    }

    @Test
    fun `Should show an error if only 1 rule is specified`()
    {
        val dialog = spyk<DartzeeTemplateDialog>()

        dialog.tfName.text = "My template"
        dialog.rulePanel.addRulesToTable(listOf(makeDartzeeRuleDto()))
        dialog.btnOk.doClick()

        dialogFactory.errorsShown.shouldContainExactly("You must create at least 2 rules.")
        verifyNotCalled { dialog.dispose() }
    }

    @Test
    fun `Should insert the template and rules and dispose if valid`()
    {
        val dialog = spyk<DartzeeTemplateDialog>()

        dialog.tfName.text = "ABC"

        val tenPercentRule = makeDartzeeRuleDto(DartzeeDartRuleEven(), calculationResult = makeDartzeeRuleCalculationResult(10))
        val twentyPercentRule = makeDartzeeRuleDto(DartzeeDartRuleOdd(), calculationResult = makeDartzeeRuleCalculationResult(20))

        dialog.rulePanel.addRulesToTable(listOf(twentyPercentRule, tenPercentRule))
        dialog.btnOk.doClick()

        dialogFactory.errorsShown.shouldBeEmpty()
        verify { dialog.dispose() }

        //Template should be set on the dialog, and should have been saved to the DB
        val template = assertNotNull(dialog.dartzeeTemplate)
        template.difficulty shouldBe 15.0
        template.name shouldBe "ABC"
        template.ruleCount shouldBe 2
        template.retrievedFromDb shouldBe true

        val rules = DartzeeRuleEntity().retrieveForTemplate(template.rowId).sortedBy { it.ordinal }
        rules.size shouldBe 2

        rules[0].toDto().generateRuleDescription() shouldBe twentyPercentRule.generateRuleDescription()
        rules[1].toDto().generateRuleDescription() shouldBe tenPercentRule.generateRuleDescription()
    }

    @Test
    fun `Should not set the dartzeeTemplate variable if cancelled`()
    {
        val dialog = spyk<DartzeeTemplateDialog>()

        dialog.btnCancel.doClick()

        dialog.dartzeeTemplate shouldBe null
        verify { dialog.dispose() }
    }

    @Test
    fun `Should copy the name and rules from the provided template`()
    {
        val template = insertDartzeeTemplate(name = "ABC")

        val rule = makeDartzeeRuleDto(DartzeeDartRuleEven())
        val ruleTwo = makeDartzeeRuleDto(DartzeeDartRuleOdd())

        rule.toEntity(1, DARTZEE_TEMPLATE, template.rowId).saveToDatabase()
        ruleTwo.toEntity(2, DARTZEE_TEMPLATE, template.rowId).saveToDatabase()

        val dialog = DartzeeTemplateDialog()
        dialog.copy(template)

        dialog.tfName.text shouldBe "ABC - Copy"
        val rules = dialog.rulePanel.getRules()
        rules.size shouldBe 2
        rules[0].generateRuleDescription() shouldBe rule.generateRuleDescription()
        rules[1].generateRuleDescription() shouldBe ruleTwo.generateRuleDescription()
    }
}