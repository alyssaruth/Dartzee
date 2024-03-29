package dartzee.screen.ai

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import dartzee.ai.DartzeePlayStyle
import dartzee.helper.AbstractTest
import dartzee.helper.makeDartsModel
import io.kotest.matchers.shouldBe
import javax.swing.JRadioButton
import org.junit.jupiter.api.Test

class TestAIConfigurationSubPanelDartzee : AbstractTest() {
    @Test
    fun `Should initialise from model correctly`() {
        val model = makeDartsModel(dartzeePlayStyle = DartzeePlayStyle.AGGRESSIVE)

        val panel = AIConfigurationSubPanelDartzee()
        panel.initialiseFromModel(model)
        panel.getChild<JRadioButton>(text = "Cautious").isSelected shouldBe false
        panel.getChild<JRadioButton>(text = "Aggressive").isSelected shouldBe true

        val cautiousModel = makeDartsModel(dartzeePlayStyle = DartzeePlayStyle.CAUTIOUS)
        panel.initialiseFromModel(cautiousModel)
        panel.getChild<JRadioButton>(text = "Cautious").isSelected shouldBe true
        panel.getChild<JRadioButton>(text = "Aggressive").isSelected shouldBe false
    }

    @Test
    fun `Should populate model correctly`() {
        val model = makeDartsModel()
        val panel = AIConfigurationSubPanelDartzee()

        panel.clickChild<JRadioButton>(text = "Cautious")
        panel.populateModel(model).dartzeePlayStyle shouldBe DartzeePlayStyle.CAUTIOUS

        panel.clickChild<JRadioButton>(text = "Aggressive")
        panel.populateModel(model).dartzeePlayStyle shouldBe DartzeePlayStyle.AGGRESSIVE
    }
}
