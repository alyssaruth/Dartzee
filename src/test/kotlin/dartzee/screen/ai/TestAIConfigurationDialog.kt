package dartzee.screen.ai

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import dartzee.ai.DartsAiModel
import dartzee.ai.DartzeePlayStyle
import dartzee.bean.PlayerAvatar
import dartzee.core.bean.ComboBoxItem
import dartzee.core.bean.selectedItemTyped
import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.makeDartsModel
import dartzee.`object`.SegmentType
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JRadioButton

class TestAIConfigurationDialog: AbstractTest()
{
    @Test
    fun `Should populate correctly for a new player`()
    {
        val dlg = AIConfigurationDialog()

        dlg.getChild<PlayerAvatar>().readOnly shouldBe false
        dlg.textFieldName.isEditable shouldBe true

        val normalDistPanel = dlg.getChild<AIConfigurationPanelNormalDistribution>()
        normalDistPanel.initialiseModel() shouldBe DartsAiModel.new()

        val dartzeePanel = dlg.getChild<AIConfigurationSubPanelDartzee>()
        dartzeePanel.populateModel(DartsAiModel.new()) shouldBe DartsAiModel.new()

        val x01Panel = dlg.getChild<AIConfigurationSubPanelX01>()
        x01Panel.populateModel(DartsAiModel.new()) shouldBe DartsAiModel.new()

        val golfPanel = dlg.getChild<AIConfigurationSubPanelGolf>()
        golfPanel.populateModel(DartsAiModel.new()) shouldBe DartsAiModel.new()
    }

    @Test
    fun `Should populate correctly for an existing player`()
    {
        val strategy = makeDartsModel(
                standardDeviation = 75.0,
                dartzeePlayStyle = DartzeePlayStyle.AGGRESSIVE,
                mercyThreshold = 17,
                hmDartNoToSegmentType = mapOf(1 to SegmentType.INNER_SINGLE, 2 to SegmentType.INNER_SINGLE, 3 to SegmentType.INNER_SINGLE))

        val player = insertPlayer(name = "Robot", strategy = strategy.toJson())
        val dlg = AIConfigurationDialog(player)

        dlg.textFieldName.text shouldBe "Robot"
        dlg.getChild<PlayerAvatar>().readOnly shouldBe true

        val normalDistPanel = dlg.getChild<AIConfigurationPanelNormalDistribution>()
        normalDistPanel.nfStandardDeviation.value shouldBe 75.0

        val dartzeePanel = dlg.getChild<AIConfigurationSubPanelDartzee>()
        dartzeePanel.getChild<JRadioButton>("Aggressive").isSelected shouldBe true

        val x01Panel = dlg.getChild<AIConfigurationSubPanelX01>()
        x01Panel.spinnerMercyThreshold.value shouldBe 17

        val golfPanel = dlg.getChild<AIConfigurationSubPanelGolf>().getPanelForDartNo(1)
        golfPanel.getChild<JComboBox<ComboBoxItem<SegmentType>>>().selectedItemTyped().hiddenData shouldBe SegmentType.INNER_SINGLE
    }

    @Test
    fun `Should successfully save changes to an AI player`()
    {
        val player = insertPlayer(name = "Sid", strategy = DartsAiModel.new().toJson())

        val dlg = AIConfigurationDialog(player)
        dlg.textFieldName.text = "Brooke"
        val normalDistPanel = dlg.getChild<AIConfigurationPanelNormalDistribution>()
        normalDistPanel.nfStandardDeviation.value = 75.0

        dlg.clickChild<JButton>(text="Ok")

        val updatedPlayer = PlayerEntity().retrieveForId(player.rowId)!!
        updatedPlayer.name shouldBe "Brooke"
        val model = updatedPlayer.getModel()
        model shouldBe DartsAiModel.new().copy(standardDeviation = 75.0)
    }

    @Test
    fun `Should enforce name and avatar for a new player`()
    {
        insertPlayer(name = "Duplicate")

        val dlg = AIConfigurationDialog()
        dlg.clickChild<JButton>(text="Ok")
        dialogFactory.errorsShown.shouldContainExactly("You must enter a name for this player.")

        dialogFactory.errorsShown.clear()
        dlg.textFieldName.text = "Duplicate"
        dlg.clickChild<JButton>(text="Ok")
        dialogFactory.errorsShown.shouldContainExactly("A player with the name Duplicate already exists.")

        dialogFactory.errorsShown.clear()
        dlg.textFieldName.text = "Valid"
        dlg.clickChild<JButton>(text="Ok")
        dialogFactory.errorsShown.shouldContainExactly("You must select an avatar.")

        dialogFactory.errorsShown.clear()
        dlg.avatar.avatarId = "foo"
        dlg.clickChild<JButton>(text="Ok")
        dialogFactory.errorsShown.shouldBeEmpty()

        val player = PlayerEntity.retrieveForName("Valid")!!
        player.playerImageId shouldBe "foo"
    }

    @Test
    fun `Should calculate stats for the configured model`()
    {
        val dlg = AIConfigurationDialog()

        val normalDistPanel = dlg.getChild<AIConfigurationPanelNormalDistribution>()
        normalDistPanel.nfStandardDeviation.value = 0.1

        dlg.clickChild<JButton>(text="Calculate")
        dlg.textFieldAverageScore.text shouldBe "60.0"
        dlg.textFieldFinishPercent.text shouldBe "100.0"
        dlg.textFieldMissPercent.text shouldBe "0.0"
        dlg.textFieldTreblePercent.text shouldBe "100.0"

        dlg.getChild<AIConfigurationSubPanelX01>().spinnerScoringDart.value = 19
        dlg.clickChild<JButton>(text="Re-calculate")
        dlg.textFieldAverageScore.text shouldBe "57.0"
        dlg.textFieldFinishPercent.text shouldBe "100.0"
        dlg.textFieldMissPercent.text shouldBe "0.0"
        dlg.textFieldTreblePercent.text shouldBe "100.0"
    }
}