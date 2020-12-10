package dartzee.screen.reporting

import com.github.alexburlton.swingtest.clickChild
import dartzee.core.bean.ComboBoxNumberComparison
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.reporting.COMPARATOR_SCORE_UNSET
import dartzee.reporting.IncludedPlayerParameters
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JCheckBox

class TestPlayerParametersPanel: AbstractTest()
{
    @Test
    fun `Should support disabling all components`()
    {
        val panel = PlayerParametersPanel()
        panel.disableAll()

        panel.components.forEach { it.isEnabled shouldBe false }
    }

    @Test
    fun `Should having nothing selected by default, and generate empty parameters`()
    {
        val panel = PlayerParametersPanel()
        panel.chckbxPosition.isSelected shouldBe false
        panel.chckbxFinalScore.isSelected shouldBe false

        panel.generateParameters() shouldBe IncludedPlayerParameters()
    }

    @Test
    fun `Should enable and disable the position checkboxes`()
    {
        val panel = PlayerParametersPanel()
        panel.positionCheckboxes.forEach { it.isEnabled shouldBe false }
        panel.cbUndecided.isEnabled shouldBe false

        panel.chckbxPosition.doClick()
        panel.positionCheckboxes.forEach { it.isEnabled shouldBe true }
        panel.cbUndecided.isEnabled shouldBe true

        panel.chckbxPosition.doClick()
        panel.positionCheckboxes.forEach { it.isEnabled shouldBe false }
        panel.cbUndecided.isEnabled shouldBe false
    }

    @Test
    fun `Should enable and disable the score options`()
    {
        val panel = PlayerParametersPanel()
        panel.comboBox.isEnabled shouldBe false
        panel.spinner.isEnabled shouldBe false

        panel.chckbxFinalScore.doClick()
        panel.comboBox.isEnabled shouldBe true
        panel.spinner.isEnabled shouldBe true

        panel.chckbxFinalScore.doClick()
        panel.comboBox.isEnabled shouldBe false
        panel.spinner.isEnabled shouldBe false
    }

    @Test
    fun `Should enable and disable the spinner based on combo selection`()
    {
        val panel = PlayerParametersPanel()
        panel.chckbxFinalScore.doClick()
        panel.spinner.isEnabled shouldBe true

        panel.comboBox.selectedItem = COMPARATOR_SCORE_UNSET
        panel.spinner.isEnabled shouldBe false

        panel.comboBox.selectedIndex = 0
        panel.spinner.isEnabled shouldBe true
    }

    @Test
    fun `Should not be valid if position is selected but nothing ticked`()
    {
        val player = insertPlayer(name = "Gordon")

        val panel = PlayerParametersPanel()
        panel.valid(player) shouldBe true

        panel.clickChild<JCheckBox>("Position")
        panel.valid(player) shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("You must select at least one finishing position for player Gordon")
        dialogFactory.errorsShown.clear()

        panel.clickChild<JCheckBox>("Undecided")
        panel.valid(player) shouldBe true
        dialogFactory.errorsShown.shouldBeEmpty()

        panel.cbUndecided.doClick()
        panel.clickChild<JCheckBox>("1st")
        panel.valid(player) shouldBe true
        dialogFactory.errorsShown.shouldBeEmpty()
    }

    @Test
    fun `Should generate the correct parameters for final score`()
    {
        val panel = PlayerParametersPanel()
        panel.clickChild<JCheckBox>("Game Score")
        panel.spinner.value = 20
        panel.comboBox.selectedItem = ComboBoxNumberComparison.FILTER_MODE_GREATER_THAN

        val params = panel.generateParameters()
        params.finalScore shouldBe 20
        params.finalScoreComparator shouldBe ComboBoxNumberComparison.FILTER_MODE_GREATER_THAN
    }

    @Test
    fun `Should generate the correct parameters for position`()
    {
        val panel = PlayerParametersPanel()
        panel.clickChild<JCheckBox>("Position")
        panel.clickChild<JCheckBox>("1st")
        panel.clickChild<JCheckBox>("5th")
        panel.clickChild<JCheckBox>("Undecided")


        val params = panel.generateParameters()
        params.finishingPositions.shouldContainExactly(1, 5, -1)
    }
}