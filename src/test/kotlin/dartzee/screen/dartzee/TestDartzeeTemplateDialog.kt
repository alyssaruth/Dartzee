package dartzee.screen.dartzee

import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import dartzee.clickCancel
import dartzee.clickOk
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.db.DartzeeRuleEntity
import dartzee.db.EntityName
import dartzee.helper.AbstractTest
import dartzee.helper.insertDartzeeTemplate
import dartzee.helper.makeDartzeeRuleCalculationResult
import dartzee.helper.makeDartzeeRuleDto
import dartzee.typeText
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JTextField

class TestDartzeeTemplateDialog: AbstractTest()
{
    @Test
    fun `Should show an error if template name not specified`()
    {
        val dialog = showDialog()
        dialog.clickOk()

        dialogFactory.errorsShown.shouldContainExactly("You must enter a name.")
        dialog.shouldBeVisible()
    }

    @Test
    fun `Should show an error if 0 rules are specified`()
    {
        val dialog = showDialog()
        dialog.getChild<JTextField>().typeText("My template")
        dialog.clickOk()

        dialogFactory.errorsShown.shouldContainExactly("You must create at least 2 rules.")
        dialog.shouldBeVisible()
    }

    @Test
    fun `Should show an error if only 1 rule is specified`()
    {
        val dialog = showDialog()

        dialog.getChild<JTextField>().typeText("My template")
        dialog.rulePanel.addRulesToTable(listOf(makeDartzeeRuleDto()))
        dialog.clickOk()

        dialogFactory.errorsShown.shouldContainExactly("You must create at least 2 rules.")
        dialog.shouldBeVisible()
    }

    @Test
    fun `Should insert the template and rules and dispose if valid`()
    {
        val dialog = showDialog()

        dialog.getChild<JTextField>().typeText("ABC")

        val tenPercentRule = makeDartzeeRuleDto(DartzeeDartRuleEven(), calculationResult = makeDartzeeRuleCalculationResult(10))
        val twentyPercentRule = makeDartzeeRuleDto(DartzeeDartRuleOdd(), calculationResult = makeDartzeeRuleCalculationResult(20))

        dialog.rulePanel.addRulesToTable(listOf(twentyPercentRule, tenPercentRule))
        dialog.clickOk()

        dialogFactory.errorsShown.shouldBeEmpty()
        dialog.shouldNotBeVisible()

        //Template should be set on the dialog, and should have been saved to the DB
        val template = dialog.dartzeeTemplate!!
        template.name shouldBe "ABC"
        template.retrievedFromDb shouldBe true

        val rules = DartzeeRuleEntity().retrieveForTemplate(template.rowId).sortedBy { it.ordinal }
        rules.size shouldBe 2

        rules[0].toDto().generateRuleDescription() shouldBe twentyPercentRule.generateRuleDescription()
        rules[1].toDto().generateRuleDescription() shouldBe tenPercentRule.generateRuleDescription()
    }

    @Test
    fun `Should not set the dartzeeTemplate variable if cancelled`()
    {
        val dialog = showDialog()

        dialog.clickCancel()

        dialog.dartzeeTemplate shouldBe null
        dialog.shouldNotBeVisible()
    }

    @Test
    fun `Should copy the name and rules from the provided template`()
    {
        val template = insertDartzeeTemplate(name = "ABC")

        val rule = makeDartzeeRuleDto(DartzeeDartRuleEven())
        val ruleTwo = makeDartzeeRuleDto(DartzeeDartRuleOdd())

        rule.toEntity(1, EntityName.DartzeeTemplate, template.rowId).saveToDatabase()
        ruleTwo.toEntity(2, EntityName.DartzeeTemplate, template.rowId).saveToDatabase()

        val dialog = DartzeeTemplateDialog()
        dialog.copy(template)

        dialog.getChild<JTextField>().text shouldBe "ABC - Copy"
        val rules = dialog.rulePanel.getRules()
        rules.size shouldBe 2
        rules[0].generateRuleDescription() shouldBe rule.generateRuleDescription()
        rules[1].generateRuleDescription() shouldBe ruleTwo.generateRuleDescription()
    }

    private fun showDialog(): DartzeeTemplateDialog
    {
        val dialog = DartzeeTemplateDialog()
        dialog.isModal = false
        dialog.isVisible = true
        return dialog
    }
}