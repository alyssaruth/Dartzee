package dartzee.screen.ai

import dartzee.`object`.Dart
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.maps.shouldContain
import io.kotlintest.shouldBe
import net.miginfocom.swing.MigLayout
import org.junit.Test

class TestNewSetupRuleDialog: AbstractTest()
{
    @Test
    fun `Should move the spinner to the correct cell when radio selection changes`()
    {
        val dlg = NewSetupRuleDialog(mutableMapOf())

        val layout = dlg.panel.layout as MigLayout

        dlg.rdbtnSingle.doClick()
        layout.getComponentConstraints(dlg.spinner) shouldBe "cell 2 1"

        dlg.rdbtnDouble.doClick()
        layout.getComponentConstraints(dlg.spinner) shouldBe "cell 2 2"

        dlg.rdbtnTreble.doClick()
        layout.getComponentConstraints(dlg.spinner) shouldBe "cell 2 3"
    }

    @Test
    fun `Should not allow the score field to be left blank`()
    {
        val dlg = NewSetupRuleDialog(mutableMapOf())

        dlg.valid() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("You must enter a score for this rule to apply to.")
    }

    @Test
    fun `Should not allow treble 25 to be entered as a target`()
    {
        val dlg = NewSetupRuleDialog(mutableMapOf())

        dlg.nfScore.value = 50
        dlg.spinner.value = 25
        dlg.rdbtnTreble.doClick()

        dlg.valid() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("Treble 25 is not a valid dart!")
    }

    @Test
    fun `Should not allow a target that would bust the player`()
    {
        val dlg = NewSetupRuleDialog(mutableMapOf())

        dlg.nfScore.value = 50

        dlg.spinner.value = 20
        dlg.rdbtnTreble.doClick()

        dlg.valid() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("This target would bust the player")
    }

    @Test
    fun `Should not allow a rule that overlaps with the existing default`()
    {
        val dlg = NewSetupRuleDialog(mutableMapOf())

        dlg.nfScore.value = 60
        dlg.spinner.value = 20
        dlg.rdbtnSingle.doClick()

        dlg.valid() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("The selected dart is already the default for this starting score.")
    }

    @Test
    fun `Should add a valid rule to the HashMap`()
    {
        val hm = mutableMapOf<Int, Dart>()
        val dlg = NewSetupRuleDialog(hm)

        dlg.nfScore.value = 50
        dlg.spinner.value = 25
        dlg.rdbtnDouble.doClick()

        dlg.btnOk.doClick()
        dialogFactory.errorsShown.shouldBeEmpty()
        hm.shouldContain(50, Dart(25, 2))
        hm.size shouldBe 1
    }

    @Test
    fun `Should not add to the HashMap when cancelled`()
    {
        val hm = mutableMapOf<Int, Dart>()
        val dlg = NewSetupRuleDialog(hm)

        dlg.nfScore.value = 50
        dlg.spinner.value = 25
        dlg.rdbtnDouble.doClick()

        dlg.btnCancel.doClick()
        dialogFactory.errorsShown.shouldBeEmpty()
        hm.size shouldBe 0
    }

    @Test
    fun `Should set the correct score and multiplier on the target dart`()
    {
        verifyDart("Single", 1)
        verifyDart("Double", 2)
        verifyDart("Treble", 3)
    }
    private fun verifyDart(rdbtn: String, multiplier: Int)
    {
        val hm = mutableMapOf<Int, Dart>()
        val dlg = NewSetupRuleDialog(hm)

        dlg.nfScore.value = 60
        dlg.spinner.value = 10
        dlg.panel.setSelection(rdbtn)

        dlg.btnOk.doClick()
        dialogFactory.errorsShown.shouldBeEmpty()
        hm.shouldContain(60, Dart(10, multiplier))
        hm.size shouldBe 1
    }
}
