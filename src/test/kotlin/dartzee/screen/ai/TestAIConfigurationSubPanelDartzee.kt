package dartzee.screen.ai

import com.github.alexburlton.swingtest.clickChild
import com.github.alexburlton.swingtest.getChild
import dartzee.ai.DartzeePlayStyle
import dartzee.helper.AbstractTest
import dartzee.helper.makeDartsModel
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JRadioButton

class TestAIConfigurationSubPanelDartzee: AbstractTest()
{
    @Test
    fun `Should initialise from model correctly`()
    {
        val model = makeDartsModel(dartzeePlayStyle = DartzeePlayStyle.AGGRESSIVE)

        val panel = AIConfigurationSubPanelDartzee()
        panel.initialiseFromModel(model)
        panel.getChild<JRadioButton>("Cautious").isSelected shouldBe false
        panel.getChild<JRadioButton>("Aggressive").isSelected shouldBe true

        val cautiousModel = makeDartsModel(dartzeePlayStyle = DartzeePlayStyle.CAUTIOUS)
        panel.initialiseFromModel(cautiousModel)
        panel.getChild<JRadioButton>("Cautious").isSelected shouldBe true
        panel.getChild<JRadioButton>("Aggressive").isSelected shouldBe false
    }

    @Test
    fun `Should populate model correctly`()
    {
        val model = makeDartsModel()
        val panel = AIConfigurationSubPanelDartzee()

        panel.clickChild<JRadioButton>("Cautious")
        panel.populateModel(model).dartzeePlayStyle shouldBe DartzeePlayStyle.CAUTIOUS

        panel.clickChild<JRadioButton>("Aggressive")
        panel.populateModel(model).dartzeePlayStyle shouldBe DartzeePlayStyle.AGGRESSIVE
    }
}