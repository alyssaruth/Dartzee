package dartzee.screen.ai

import com.github.alexburlton.swingtest.clickChild
import com.github.alexburlton.swingtest.getChild
import com.github.alexburlton.swingtest.shouldBeDisabled
import com.github.alexburlton.swingtest.shouldBeEnabled
import dartzee.ai.DartsAiModel
import dartzee.helper.AbstractTest
import dartzee.helper.makeDartsModel
import io.kotlintest.shouldBe
import org.junit.Test
import javax.swing.JCheckBox

class TestAIConfigurationPanelNormalDistribution: AbstractTest()
{
    @Test
    fun `Should enable and disable doubles spinner`()
    {
        val panel = AIConfigurationPanelNormalDistribution()
        panel.initialiseFromModel(DartsAiModel.new())
        panel.nfStandardDeviationDoubles.shouldBeDisabled()

        panel.clickChild<JCheckBox>("Standard Deviation (Doubles)")
        panel.nfStandardDeviationDoubles.shouldBeEnabled()

        panel.clickChild<JCheckBox>("Standard Deviation (Doubles)")
        panel.nfStandardDeviationDoubles.shouldBeDisabled()
    }

    @Test
    fun `Should enable and disable central skew spinner`()
    {
        val panel = AIConfigurationPanelNormalDistribution()
        panel.initialiseFromModel(DartsAiModel.new())
        panel.nfCentralBias.shouldBeDisabled()

        panel.clickChild<JCheckBox>("Standard Deviation (skew towards center)")
        panel.nfCentralBias.shouldBeEnabled()

        panel.clickChild<JCheckBox>("Standard Deviation (skew towards center)")
        panel.nfCentralBias.shouldBeDisabled()
    }

    @Test
    fun `Should populate correctly from populated model`()
    {
        val model = makeDartsModel(standardDeviation = 100.0, standardDeviationDoubles = 17.5, standardDeviationCentral = 5.2)

        val panel = AIConfigurationPanelNormalDistribution()
        panel.initialiseFromModel(model)

        panel.nfStandardDeviation.value shouldBe 100.0
        panel.getChild<JCheckBox>("Standard Deviation (Doubles)").isSelected shouldBe true
        panel.nfStandardDeviationDoubles.value shouldBe 17.5
        panel.getChild<JCheckBox>("Standard Deviation (skew towards center)").isSelected shouldBe true
        panel.nfCentralBias.value shouldBe 5.2
    }

    @Test
    fun `Should populate from empty model`()
    {
        val model = makeDartsModel(standardDeviation = 100.0, standardDeviationDoubles = null, standardDeviationCentral = null)

        val panel = AIConfigurationPanelNormalDistribution()
        panel.initialiseFromModel(model)

        panel.nfStandardDeviation.value shouldBe 100.0
        panel.getChild<JCheckBox>("Standard Deviation (Doubles)").isSelected shouldBe false
        panel.nfStandardDeviationDoubles.value shouldBe 50.0
        panel.nfStandardDeviationDoubles.shouldBeDisabled()
        panel.getChild<JCheckBox>("Standard Deviation (skew towards center)").isSelected shouldBe false
        panel.nfCentralBias.value shouldBe 50.0
        panel.nfCentralBias.shouldBeDisabled()
    }

    @Test
    fun `Should create populated model`()
    {
        val panel = AIConfigurationPanelNormalDistribution()
        panel.nfStandardDeviation.value = 57.5

        panel.clickChild<JCheckBox>("Standard Deviation (Doubles)")
        panel.nfStandardDeviationDoubles.value = 100.0

        panel.clickChild<JCheckBox>("Standard Deviation (skew towards center)")
        panel.nfCentralBias.value = 2.5

        val model = panel.initialiseModel()
        model.standardDeviation shouldBe 57.5
        model.standardDeviationDoubles shouldBe 100.0
        model.standardDeviationCentral shouldBe 2.5
    }

    @Test
    fun `Should create a model with null fields if boxes left unchecked`()
    {
        val panel = AIConfigurationPanelNormalDistribution()
        panel.nfStandardDeviation.value = 57.5

        val model = panel.initialiseModel()
        model.standardDeviation shouldBe 57.5
        model.standardDeviationDoubles shouldBe null
        model.standardDeviationCentral shouldBe null
    }
}