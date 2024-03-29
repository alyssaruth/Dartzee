package dartzee.screen.ai

import com.github.alyssaburlton.swingtest.clickCancel
import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import dartzee.ai.AimDart
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import net.miginfocom.swing.MigLayout
import org.junit.jupiter.api.Test

class TestNewSetupRuleDialog : AbstractTest() {
    @Test
    fun `Should move the spinner to the correct cell when radio selection changes`() {
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
    fun `Should not allow the score field to be left blank`() {
        val dlg = NewSetupRuleDialog(mutableMapOf())
        dlg.isModal = false
        dlg.isVisible = true
        dlg.clickOk(async = true)

        getErrorDialog().getDialogMessage() shouldBe
            "You must enter a score for this rule to apply to."

        dlg.shouldBeVisible()
    }

    @Test
    fun `Should not allow treble 25 to be entered as a target`() {
        val dlg = NewSetupRuleDialog(mutableMapOf())

        dlg.nfScore.value = 50
        dlg.spinner.value = 25
        dlg.rdbtnTreble.doClick()

        dlg.clickOk(async = true)
        getErrorDialog().getDialogMessage() shouldBe "Treble 25 is not a valid dart!"
    }

    @Test
    fun `Should not allow a target that would bust the player`() {
        val dlg = NewSetupRuleDialog(mutableMapOf())

        dlg.nfScore.value = 50

        dlg.spinner.value = 20
        dlg.rdbtnTreble.doClick()

        dlg.clickOk(async = true)
        getErrorDialog().getDialogMessage() shouldBe "This target would bust the player"
    }

    @Test
    fun `Should not allow a rule that overlaps with the existing default`() {
        val dlg = NewSetupRuleDialog(mutableMapOf())

        dlg.nfScore.value = 60
        dlg.spinner.value = 20
        dlg.rdbtnSingle.doClick()

        dlg.clickOk(async = true)
        getErrorDialog().getDialogMessage() shouldBe
            "The selected dart is already the default for this starting score."
    }

    @Test
    fun `Should add a valid rule to the HashMap`() {
        val hm = mutableMapOf<Int, AimDart>()
        val dlg = NewSetupRuleDialog(hm)

        dlg.nfScore.value = 50
        dlg.spinner.value = 25
        dlg.rdbtnDouble.doClick()

        dlg.clickOk()
        dlg.shouldNotBeVisible()
        hm.shouldContain(50, AimDart(25, 2))
        hm.size shouldBe 1
    }

    @Test
    fun `Should not add to the HashMap when cancelled`() {
        val hm = mutableMapOf<Int, AimDart>()
        val dlg = NewSetupRuleDialog(hm)

        dlg.nfScore.value = 50
        dlg.spinner.value = 25
        dlg.rdbtnDouble.doClick()

        dlg.clickCancel()
        hm.size shouldBe 0
    }

    @Test
    fun `Should set the correct score and multiplier on the target dart`() {
        verifyDart("Single", 1)
        verifyDart("Double", 2)
        verifyDart("Treble", 3)
    }

    private fun verifyDart(rdbtn: String, multiplier: Int) {
        val hm = mutableMapOf<Int, AimDart>()
        val dlg = NewSetupRuleDialog(hm)

        dlg.nfScore.value = 60
        dlg.spinner.value = 10
        dlg.panel.setSelection(rdbtn)

        dlg.clickOk()
        dialogFactory.errorsShown.shouldBeEmpty()
        hm.shouldContain(60, AimDart(10, multiplier))
        hm.size shouldBe 1
    }
}
