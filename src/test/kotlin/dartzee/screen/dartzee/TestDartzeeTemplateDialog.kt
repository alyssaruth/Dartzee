package dartzee.screen.dartzee

import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import dartzee.clickCancel
import dartzee.clickOk
import dartzee.core.helper.verifyNotCalled
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.db.DartzeeRuleEntity
import dartzee.db.DartzeeTemplateEntity
import dartzee.db.EntityName
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.helper.insertDartzeeTemplate
import dartzee.helper.makeDartzeeRuleCalculationResult
import dartzee.helper.makeDartzeeRuleDto
import dartzee.only
import dartzee.typeText
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import javax.swing.JTextField

class TestDartzeeTemplateDialog: AbstractTest()
{
    @Test
    fun `Should show an error if template name not specified`()
    {
        val (dialog, callback) = showDialog()
        dialog.clickOk()

        dialogFactory.errorsShown.shouldContainExactly("You must enter a name.")
        dialog.shouldBeVisible()
        verifyNotCalled { callback() }
    }

    @Test
    fun `Should show an error if 0 rules are specified`()
    {
        val (dialog) = showDialog()
        dialog.getChild<JTextField>().typeText("My template")
        dialog.clickOk()

        dialogFactory.errorsShown.shouldContainExactly("You must create at least 2 rules.")
        dialog.shouldBeVisible()
    }

    @Test
    fun `Should show an error if only 1 rule is specified`()
    {
        val (dialog) = showDialog()

        dialog.getChild<JTextField>().typeText("My template")
        dialog.rulePanel.addRulesToTable(listOf(makeDartzeeRuleDto()))
        dialog.clickOk()

        dialogFactory.errorsShown.shouldContainExactly("You must create at least 2 rules.")
        dialog.shouldBeVisible()
    }

    @Test
    fun `Should insert the template and rules and dispose if valid`()
    {
        val (dialog, callback) = showDialog()

        dialog.getChild<JTextField>().typeText("ABC")

        val tenPercentRule = makeDartzeeRuleDto(DartzeeDartRuleEven(), calculationResult = makeDartzeeRuleCalculationResult(10))
        val twentyPercentRule = makeDartzeeRuleDto(DartzeeDartRuleOdd(), calculationResult = makeDartzeeRuleCalculationResult(20))

        dialog.rulePanel.addRulesToTable(listOf(twentyPercentRule, tenPercentRule))
        dialog.clickOk()

        dialogFactory.errorsShown.shouldBeEmpty()
        dialog.shouldNotBeVisible()
        verify { callback() }

        //Template should be set on the dialog, and should have been saved to the DB
        val template = DartzeeTemplateEntity().retrieveEntities().only()
        template.name shouldBe "ABC"
        template.retrievedFromDb shouldBe true

        val rules = DartzeeRuleEntity().retrieveForTemplate(template.rowId).sortedBy { it.ordinal }
        rules.size shouldBe 2

        rules[0].toDto().generateRuleDescription() shouldBe twentyPercentRule.generateRuleDescription()
        rules[1].toDto().generateRuleDescription() shouldBe tenPercentRule.generateRuleDescription()
    }

    @Test
    fun `Should not create a template if cancelled`()
    {
        val (dialog, callback) = showDialog()

        dialog.clickCancel()

        getCountFromTable(EntityName.DartzeeTemplate) shouldBe 0
        dialog.shouldNotBeVisible()
        verifyNotCalled { callback() }
    }

    @Test
    fun `Should copy the name and rules from the provided template`()
    {
        val template = insertDartzeeTemplate(name = "ABC")

        val rule = makeDartzeeRuleDto(DartzeeDartRuleEven())
        val ruleTwo = makeDartzeeRuleDto(DartzeeDartRuleOdd())

        rule.toEntity(1, EntityName.DartzeeTemplate, template.rowId).saveToDatabase()
        ruleTwo.toEntity(2, EntityName.DartzeeTemplate, template.rowId).saveToDatabase()

        val dialog = DartzeeTemplateDialog(mockk())
        dialog.copy(template)

        dialog.getChild<JTextField>().text shouldBe "ABC - Copy"
        val rules = dialog.rulePanel.getRules()
        rules.size shouldBe 2
        rules[0].generateRuleDescription() shouldBe rule.generateRuleDescription()
        rules[1].generateRuleDescription() shouldBe ruleTwo.generateRuleDescription()
    }

    private fun showDialog(): Pair<DartzeeTemplateDialog, () -> Unit>
    {
        val callback = mockk<() -> Unit>(relaxed = true)
        val dialog = DartzeeTemplateDialog(callback)
        dialog.isVisible = true
        return dialog to callback
    }
}