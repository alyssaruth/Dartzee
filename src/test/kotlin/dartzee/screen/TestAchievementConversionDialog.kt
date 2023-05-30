package dartzee.screen

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeDisabled
import com.github.alyssaburlton.swingtest.shouldBeEnabled
import com.github.alyssaburlton.swingtest.shouldBeVisible
import dartzee.achievements.AbstractAchievement
import dartzee.bean.PlayerSelector
import dartzee.bean.getAllPlayers
import dartzee.clickOk
import dartzee.helper.AbstractTest
import dartzee.helper.preparePlayers
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JComboBox
import javax.swing.JOptionPane
import javax.swing.JRadioButton

class TestAchievementConversionDialog : AbstractTest()
{
    @Test
    fun `Should populate the player selector from the db`()
    {
        val players = preparePlayers(5).map { it.name }
        val dlg = AchievementConversionDialog()
        dlg.getChild<PlayerSelector>().tablePlayersToSelectFrom.getAllPlayers().map { it.name } shouldBe players
    }

    @Test
    fun `Achievement dropdown should enable based on radio selection`()
    {
        val dlg = AchievementConversionDialog()
        val comboBox = dlg.getChild<JComboBox<AbstractAchievement>>()
        comboBox.shouldBeDisabled()

        dlg.clickChild<JRadioButton>(text = "Specific conversion")
        comboBox.shouldBeEnabled()

        dlg.clickChild<JRadioButton>(text = "All")
        comboBox.shouldBeDisabled()
    }

    @Test
    fun `Should validate and not dispose if cancelling running on all players`()
    {
        preparePlayers(2)
        dialogFactory.questionOption = JOptionPane.NO_OPTION

        val dlg = AchievementConversionDialog()
        dlg.isVisible = true
        dlg.clickOk()


        dlg.shouldBeVisible()
        dialogFactory.questionsShown.shouldContainExactly("This will run the conversion(s) for ALL players. Proceed?")
    }
}