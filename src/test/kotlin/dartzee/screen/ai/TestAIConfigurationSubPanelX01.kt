package dartzee.screen.ai

import dartzee.`object`.Dart
import dartzee.ai.DartsModelNormalDistribution
import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestAIConfigurationSubPanelX01: AbstractTest()
{
    @Test
    fun `Should populate the scoring dart onto the model`()
    {
        val panel = AIConfigurationSubPanelX01()
        panel.spinnerScoringDart.value = 18

        val model = DartsModelNormalDistribution()
        panel.populateModel(model)

        model.scoringDart shouldBe 18
    }

    @Test
    fun `Should populate the mercy threshold onto the model`()
    {
        val panel = AIConfigurationSubPanelX01()
        panel.chckbxMercyRule.isSelected = true
        panel.spinnerMercyThreshold.value = 13

        val model = DartsModelNormalDistribution()
        panel.populateModel(model)

        model.mercyThreshold shouldBe 13
    }

    @Test
    fun `Should not populate the mercy threshold if the box is unticked`()
    {
        val panel = AIConfigurationSubPanelX01()
        panel.chckbxMercyRule.isSelected = false
        panel.spinnerMercyThreshold.value = 13

        val model = DartsModelNormalDistribution()
        panel.populateModel(model)

        model.mercyThreshold shouldBe -1
    }

    @Test
    fun `Should populate the model with the setup darts`()
    {
        val panel = AIConfigurationSubPanelX01()
        panel.hmScoreToDart[20] = Dart(10, 2)

        val model = DartsModelNormalDistribution()
        panel.populateModel(model)

        model.hmScoreToDart[20] shouldBe Dart(10, 2)
    }

    @Test
    fun `Should initialise the scoringDart from the model`()
    {
        val model = DartsModelNormalDistribution()
        model.scoringDart = 15

        val panel = AIConfigurationSubPanelX01()
        panel.initialiseFromModel(model)

        panel.spinnerScoringDart.value shouldBe 15
    }

    @Test
    fun `Should initialise the mercy threshold from the model`()
    {
        val model = DartsModelNormalDistribution()
        model.mercyThreshold = 11

        val panel = AIConfigurationSubPanelX01()
        panel.initialiseFromModel(model)

        panel.chckbxMercyRule.isSelected shouldBe true
        panel.spinnerMercyThreshold.value shouldBe 11
        panel.lblWhenScoreLess.isEnabled shouldBe true
    }

    @Test
    fun `Should initialise an unset mercy threshold from the model`()
    {
        val model = DartsModelNormalDistribution()
        model.mercyThreshold = -1

        val panel = AIConfigurationSubPanelX01()
        panel.initialiseFromModel(model)

        panel.chckbxMercyRule.isSelected shouldBe false
        panel.spinnerMercyThreshold.value shouldBe 10
        panel.lblWhenScoreLess.isEnabled shouldBe false
    }

    @Test
    fun `Mercy rule checkbox should toggle other components`()
    {
        val panel = AIConfigurationSubPanelX01()
        panel.chckbxMercyRule.doClick()
        panel.spinnerMercyThreshold.isEnabled shouldBe true
        panel.lblWhenScoreLess.isEnabled shouldBe true

        panel.chckbxMercyRule.doClick()
        panel.spinnerMercyThreshold.isEnabled shouldBe false
        panel.lblWhenScoreLess.isEnabled shouldBe false
    }
}