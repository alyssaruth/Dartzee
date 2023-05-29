package dartzee.screen.dartzee

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.makeActionEvent
import com.github.alyssaburlton.swingtest.shouldBeDisabled
import com.github.alyssaburlton.swingtest.shouldBeEnabled
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import dartzee.bean.DartzeeDartRuleSelector
import dartzee.clickCancel
import dartzee.clickOk
import dartzee.core.bean.selectByClass
import dartzee.core.util.getAllChildComponentsForType
import dartzee.dartzee.DartzeeCalculator
import dartzee.dartzee.aggregate.DartzeeTotalRuleEqualTo
import dartzee.dartzee.aggregate.DartzeeTotalRuleOdd
import dartzee.dartzee.aggregate.DartzeeTotalRulePrime
import dartzee.dartzee.dart.DartzeeDartRuleAny
import dartzee.dartzee.dart.DartzeeDartRuleColour
import dartzee.dartzee.dart.DartzeeDartRuleCustom
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.dartzee.dart.DartzeeDartRuleInner
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.dartzee.dart.DartzeeDartRuleOuter
import dartzee.dartzee.dart.DartzeeDartRuleScore
import dartzee.helper.AbstractTest
import dartzee.helper.makeColourRule
import dartzee.helper.makeDartzeeRuleDto
import dartzee.helper.makeScoreRule
import dartzee.helper.makeTotalScoreRule
import dartzee.utils.InjectedThings
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import javax.swing.JCheckBox
import javax.swing.SwingUtilities

class TestDartzeeRuleAmendment: AbstractTest()
{
    @Test
    fun `Should adjust the dialog title appropriately`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.amendRule(makeDartzeeRuleDto())

        dlg.title shouldBe "Amend Dartzee Rule"
    }

    @Test
    fun `Should populate the ruleName correctly`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.amendRule(makeDartzeeRuleDto(ruleName = "My Rule"))

        dlg.getChild<JCheckBox>(text = "Custom rule name").isSelected shouldBe true
        dlg.tfRuleName.text shouldBe "My Rule"

        dlg.amendRule(makeDartzeeRuleDto())
        dlg.getChild<JCheckBox>(text = "Custom rule name").isSelected shouldBe false
        dlg.tfRuleName.text shouldBe ""
    }

    @Test
    fun `Should populate from a 'no darts' rule correctly`()
    {
        val rule = makeDartzeeRuleDto()

        val dlg = DartzeeRuleCreationDialog()
        dlg.amendRule(rule)

        dlg.rdbtnNoDarts.isSelected shouldBe true
    }

    @Test
    fun `Should populate from an 'at least one' rule correctly`()
    {
        val rule = makeDartzeeRuleDto(makeScoreRule(12))

        val dlg = DartzeeRuleCreationDialog()
        dlg.amendRule(rule)

        dlg.rdbtnAtLeastOne.isSelected shouldBe true
        val dartRule = dlg.targetSelector.getSelection()

        val totalRule = dartRule as DartzeeDartRuleScore
        totalRule.score shouldBe 12
    }

    @Test
    fun `Should populate from a 'three darts' rule correctly`()
    {
        val rule = makeDartzeeRuleDto(makeScoreRule(13), makeColourRule(red = true, green = true), DartzeeDartRuleOdd(), inOrder = false)

        val dlg = DartzeeRuleCreationDialog()
        dlg.amendRule(rule)

        dlg.rdbtnAllDarts.isSelected shouldBe true
        dlg.cbInOrder.isSelected shouldBe false

        val scoreRule = dlg.dartOneSelector.getSelection() as DartzeeDartRuleScore
        scoreRule.score shouldBe 13

        val colourRule = dlg.dartTwoSelector.getSelection() as DartzeeDartRuleColour
        colourRule.red shouldBe true
        colourRule.green shouldBe true

        val thirdRule = dlg.dartThreeSelector.getSelection()
        thirdRule.shouldBeInstanceOf<DartzeeDartRuleOdd>()
    }

    @Test
    fun `Should populate an in order rule correctly`()
    {
        val rule = makeDartzeeRuleDto(DartzeeDartRuleEven(), DartzeeDartRuleOdd(), DartzeeDartRuleEven(), inOrder = true)

        val dlg = DartzeeRuleCreationDialog()
        dlg.amendRule(rule)

        dlg.rdbtnAllDarts.isSelected shouldBe true
        dlg.cbInOrder.isSelected shouldBe true
    }

    @Test
    fun `Should populate no total rule correctly`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.amendRule(makeDartzeeRuleDto())

        dlg.aggregateSelector.cbDesc.isSelected shouldBe false
    }

    @Test
    fun `Should populate a total rule correctly`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.amendRule(makeDartzeeRuleDto(aggregateRule = makeTotalScoreRule<DartzeeTotalRuleEqualTo>(48)))

        dlg.aggregateSelector.cbDesc.isSelected shouldBe true
        val totalRule = dlg.aggregateSelector.getSelection() as DartzeeTotalRuleEqualTo
        totalRule.target shouldBe 48
    }

    @Test
    fun `Should update the rule description`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.amendRule(makeDartzeeRuleDto(DartzeeDartRuleOuter()))

        flushEdt()

        dlg.tfDescription.text shouldBe "Score Outers"
    }

    @Test
    fun `Should populate allow misses correctly`()
    {
        val dlg = DartzeeRuleCreationDialog()
        val allowMisses = makeDartzeeRuleDto(allowMisses = true)
        val disallowMisses = makeDartzeeRuleDto(allowMisses = false)

        dlg.amendRule(allowMisses)
        dlg.cbAllowMisses.isSelected shouldBe true

        dlg.amendRule(disallowMisses)
        dlg.cbAllowMisses.isSelected shouldBe false
    }

    @Test
    fun `Should replace the darzeeRule with new values if Ok is pressed`()
    {
        val dlg = DartzeeRuleCreationDialog()

        val rule = makeDartzeeRuleDto()
        dlg.amendRule(rule)

        dlg.rdbtnAtLeastOne.doClick()
        dlg.targetSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleOdd>()
        dlg.clickOk()

        val updatedRule = dlg.dartzeeRule!!
        updatedRule shouldNotBe rule
        updatedRule.dart1Rule.shouldBeInstanceOf<DartzeeDartRuleOdd>()
    }

    @Test
    fun `Should leave the old dartzeeRule alone if cancelled`()
    {
        val dlg = DartzeeRuleCreationDialog()

        val rule = makeDartzeeRuleDto()
        dlg.amendRule(rule)

        dlg.rdbtnAtLeastOne.doClick()
        dlg.targetSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleOdd>()
        dlg.clickCancel()

        val updatedRule = dlg.dartzeeRule!!
        updatedRule shouldBe rule
    }
}

class TestDartzeeRuleCreationDialogValidation: AbstractTest()
{
    @Test
    fun `Should prevent an empty rule name`()
    {
        val dlg = showRuleCreationDialog()
        dlg.clickChild<JCheckBox>(text = "Custom rule name")
        dlg.clickOk()

        dialogFactory.errorsShown.shouldContainExactly("You cannot have an empty rule name.")
        dlg.dartzeeRule shouldBe null
        dlg.shouldBeVisible()
    }

    @Test
    fun `Should prevent a rule name that is too long`()
    {
        val ruleName = "a".repeat(1001)

        val dlg = showRuleCreationDialog()
        dlg.clickChild<JCheckBox>(text = "Custom rule name")
        dlg.tfRuleName.text = ruleName
        dlg.clickOk()

        dialogFactory.errorsShown.shouldContainExactly("Rule name cannot exceed 1000 characters.")
        dlg.dartzeeRule shouldBe null
        dlg.shouldBeVisible()
    }

    @Test
    fun `Should validate all three dart selectors for an all darts rule`()
    {
        val dlg = showRuleCreationDialog()

        SwingUtilities.invokeAndWait {
            dlg.dartOneSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleColour>()
            dlg.clickOk()
        }
        dialogFactory.errorsShown.shouldContainExactly("Dart 1: You must select at least one colour.")
        dlg.dartzeeRule shouldBe null
        dlg.shouldBeVisible()

        dialogFactory.errorsShown.clear()
        SwingUtilities.invokeAndWait {
            dlg.dartOneSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleAny>()
            dlg.dartTwoSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleColour>()
            dlg.clickOk()
        }
        dialogFactory.errorsShown.shouldContainExactly("Dart 2: You must select at least one colour.")
        dlg.dartzeeRule shouldBe null
        dlg.shouldBeVisible()

        dialogFactory.errorsShown.clear()
        SwingUtilities.invokeAndWait {
            dlg.dartTwoSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleAny>()
            dlg.dartThreeSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleColour>()
            dlg.clickOk()
        }
        dialogFactory.errorsShown.shouldContainExactly("Dart 3: You must select at least one colour.")
        dlg.dartzeeRule shouldBe null
        dlg.shouldBeVisible()
    }

    @Test
    fun `Should validate the target selector for an 'at least one' dart rule`()
    {
        val dlg = showRuleCreationDialog()

        SwingUtilities.invokeAndWait {
            dlg.rdbtnAtLeastOne.doClick()
            dlg.targetSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleColour>()
            dlg.clickOk()
        }

        dialogFactory.errorsShown.shouldContainExactly("Target: You must select at least one colour.")
        dlg.dartzeeRule shouldBe null
        dlg.shouldBeVisible()
    }

    @Test
    fun `Should detect impossible rules and not return a rule`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val dlg = showRuleCreationDialog()

        SwingUtilities.invokeAndWait {
            dlg.dartOneSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleEven>()
            dlg.dartTwoSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleEven>()
            dlg.dartThreeSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleEven>()
            dlg.aggregateSelector.cbDesc.doClick()
            dlg.aggregateSelector.comboBoxRuleType.selectByClass<DartzeeTotalRuleOdd>()
            dlg.clickOk()
        }

        dialogFactory.errorsShown.shouldContainExactly("This rule is impossible!")
        dlg.dartzeeRule shouldBe null
        dlg.shouldBeVisible()
    }

    @Test
    fun `Should dispose if valid`()
    {
        val dlg = showRuleCreationDialog()

        SwingUtilities.invokeAndWait {
            dlg.clickOk()
        }

        dialogFactory.errorsShown.shouldBeEmpty()
        dlg.shouldNotBeVisible()
    }

    private fun showRuleCreationDialog(): DartzeeRuleCreationDialog
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.isModal = false
        dlg.isVisible = true

        return dlg
    }
}

class TestDartzeeRuleCreationDialogDtoPopulation : AbstractTest()
{
    @Test
    fun `Should populate an 'at least one' rule correctly`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.rdbtnAtLeastOne.doClick()
        dlg.targetSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleOdd>()
        dlg.clickOk()

        val rule = dlg.dartzeeRule!!

        rule.dart1Rule!!.toDbString() shouldBe DartzeeDartRuleOdd().toDbString()
        rule.dart2Rule shouldBe null
        rule.dart3Rule shouldBe null
        rule.inOrder shouldBe false
    }

    @Test
    fun `Should populate an 'all darts' rule correctly`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.rdbtnAllDarts.isSelected = true
        dlg.cbInOrder.isSelected = false

        dlg.dartOneSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleInner>()
        dlg.dartTwoSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleOuter>()
        dlg.dartThreeSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleOdd>()
        dlg.clickOk()

        val rule = dlg.dartzeeRule!!

        rule.dart1Rule!!.toDbString() shouldBe DartzeeDartRuleInner().toDbString()
        rule.dart2Rule!!.toDbString() shouldBe DartzeeDartRuleOuter().toDbString()
        rule.dart3Rule!!.toDbString() shouldBe DartzeeDartRuleOdd().toDbString()
        rule.inOrder shouldBe false
    }

    @Test
    fun `Should populate in order correctly when checked`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.clickOk()

        val rule = dlg.dartzeeRule!!
        rule.inOrder shouldBe true
    }

    @Test
    fun `Should populate allowMisses correctly when checked`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.cbAllowMisses.isSelected = true

        dlg.clickOk()

        val rule = dlg.dartzeeRule!!
        rule.allowMisses shouldBe true
    }

    @Test
    fun `Should populate ruleName correctly when checked`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.clickChild<JCheckBox>(text = "Custom rule name")
        dlg.tfRuleName.text = "My Rule"
        dlg.clickOk()

        val rule = dlg.dartzeeRule!!
        rule.ruleName shouldBe "My Rule"
    }

    @Test
    fun `Should not populate ruleName when unchecked`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.clickChild<JCheckBox>(text = "Custom rule name")
        dlg.tfRuleName.text = "My Rule"
        dlg.clickChild<JCheckBox>(text = "Custom rule name")
        dlg.clickOk()

        val rule = dlg.dartzeeRule!!
        rule.ruleName shouldBe null
    }

    @Test
    fun `Should populate a 'no darts' rule correctly`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.rdbtnNoDarts.doClick()
        dlg.clickOk()

        val rule = dlg.dartzeeRule!!
        rule.dart1Rule shouldBe null
    }

    @Test
    fun `Should populate the total rule correctly`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.aggregateSelector.cbDesc.doClick()
        dlg.aggregateSelector.comboBoxRuleType.selectByClass<DartzeeTotalRulePrime>()
        dlg.clickOk()

        val rule = dlg.dartzeeRule!!
        rule.aggregateRule!!.toDbString() shouldBe DartzeeTotalRulePrime().toDbString()
    }
}

class TestDartzeeRuleCreationDialogInteraction : AbstractTest()
{
    @Test
    fun `Should not return a rule when cancelled`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.clickCancel()
        dlg.dartzeeRule shouldBe null
    }

    @Test
    fun `Should toggle rule name text field based on checkbox`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.tfRuleName.shouldBeDisabled()

        dlg.clickChild<JCheckBox>(text = "Custom rule name")
        dlg.tfRuleName.shouldBeEnabled()

        dlg.clickChild<JCheckBox>(text = "Custom rule name")
        dlg.tfRuleName.shouldBeDisabled()
    }

    @Test
    fun `Should toggle the rule selectors based on radio selection`()
    {
        val dlg = DartzeeRuleCreationDialog()

        var children = dlg.getAllChildComponentsForType<DartzeeDartRuleSelector>()
        children.shouldContainExactlyInAnyOrder(dlg.dartOneSelector, dlg.dartTwoSelector, dlg.dartThreeSelector)
        children.shouldNotContain(dlg.targetSelector)

        dlg.rdbtnAtLeastOne.doClick()
        children = dlg.getAllChildComponentsForType()
        children.shouldContainExactly(dlg.targetSelector)

        dlg.rdbtnAllDarts.doClick()
        children = dlg.getAllChildComponentsForType()
        children.shouldContainExactlyInAnyOrder(dlg.dartOneSelector, dlg.dartTwoSelector, dlg.dartThreeSelector)
        children.shouldNotContain(dlg.targetSelector)

        dlg.rdbtnNoDarts.doClick()
        children = dlg.getAllChildComponentsForType()
        children.shouldBeEmpty()
    }

    @Test
    fun `Should update the verification panel on initialisation`()
    {
        val verificationPanel = mockk<DartzeeRuleVerificationPanel>(relaxed = true)

        val dlg = DartzeeRuleCreationDialog(verificationPanel)
        flushEdt()

        verify { verificationPanel.updateRule(dlg.constructRuleFromComponents()) }
    }

    @Test
    fun `Should update the verification panel when things change`()
    {
        val verificationPanel = mockk<DartzeeRuleVerificationPanel>(relaxed = true)

        val dlg = DartzeeRuleCreationDialog(verificationPanel)
        flushEdt()
        clearAllMocks()

        dlg.dartOneSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleOuter>()
        flushEdt()

        verify { verificationPanel.updateRule(dlg.constructRuleFromComponents()) }
    }

    @Test
    fun `Should update the rule description when combo boxes change`()
    {
        val dlg = DartzeeRuleCreationDialog()
        dlg.dartOneSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleInner>()
        dlg.dartTwoSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleOuter>()
        dlg.dartThreeSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleOdd>()

        flushEdt()

        dlg.tfDescription.text shouldBe "Inner → Outer → Odd"

        dlg.aggregateSelector.cbDesc.doClick()
        dlg.aggregateSelector.comboBoxRuleType.selectByClass<DartzeeTotalRuleOdd>()

        flushEdt()

        dlg.tfDescription.text shouldBe "Inner → Outer → Odd, Total is odd"
    }

    @Test
    fun `Should update rule difficulty when the rule changes`()
    {
        //Need a real calculator for this to actually change
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val dlg = DartzeeRuleCreationDialog()
        flushEdt()
        dlg.lblDifficulty.text shouldBe "Very Easy"

        dlg.dartOneSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleCustom>()
        flushEdt()

        dlg.lblDifficulty.text shouldBe "Impossible"
    }

    @Test
    fun `Should update the rule description when score config changes`()
    {
        val dlg = DartzeeRuleCreationDialog()

        val scoreRule = dlg.dartOneSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleScore>()!!
        flushEdt()
        dlg.tfDescription.text shouldBe "20 → Any → Any"

        scoreRule.spinner.value = 15
        flushEdt()
        dlg.stateChanged(mockk())
        flushEdt()
        dlg.tfDescription.text shouldBe "15 → Any → Any"
    }

    @Test
    fun `Should update the rule description when custom config changes`()
    {
        val dlg = DartzeeRuleCreationDialog()

        val customRule = dlg.dartOneSelector.comboBoxRuleType.selectByClass<DartzeeDartRuleCustom>()!!
        flushEdt()

        dlg.tfDescription.text shouldBe "Custom → Any → Any"

        customRule.tfName.text = "Foo"
        customRule.actionPerformed(makeActionEvent(customRule.tfName))
        flushEdt()

        dlg.tfDescription.text shouldBe "Foo → Any → Any"
    }
}