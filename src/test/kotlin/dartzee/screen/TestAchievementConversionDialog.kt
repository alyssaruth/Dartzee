package dartzee.screen

import com.github.alyssaburlton.swingtest.awaitCondition
import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeDisabled
import com.github.alyssaburlton.swingtest.shouldBeEnabled
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import dartzee.achievements.AbstractAchievement
import dartzee.achievements.getAllAchievements
import dartzee.bean.PlayerSelector
import dartzee.bean.getAllPlayers
import dartzee.clickOk
import dartzee.core.bean.selectedItemTyped
import dartzee.core.screen.ProgressDialog
import dartzee.findWindow
import dartzee.helper.AbstractTest
import dartzee.helper.preparePlayers
import dartzee.logging.CODE_ACHIEVEMENT_CONVERSION_FINISHED
import dartzee.logging.CODE_ACHIEVEMENT_CONVERSION_STARTED
import dartzee.logging.KEY_ACHIEVEMENT_TYPES
import dartzee.logging.KEY_PLAYER_IDS
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JButton
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

    @Test
    fun `Should run for all achievements and players`()
    {
        preparePlayers(3)
        dialogFactory.questionOption = JOptionPane.YES_OPTION

        val dlg = AchievementConversionDialog()
        dlg.isVisible = true
        dlg.clickOk()

        waitForConversionToFinish()

        dlg.shouldNotBeVisible()
        findWindow<ProgressDialog>()!!.shouldNotBeVisible()

        val log = findLog(CODE_ACHIEVEMENT_CONVERSION_STARTED)!!
        log.message shouldBe "Regenerating ${getAllAchievements().size} achievements for all players"
        log.keyValuePairs[KEY_ACHIEVEMENT_TYPES] shouldBe getAllAchievements().map { it.achievementType }
        log.keyValuePairs[KEY_PLAYER_IDS] shouldBe emptyList<String>()
    }

    @Test
    fun `Should run for a subset of players and achievements`()
    {
        preparePlayers(4)

        val dlg = AchievementConversionDialog()
        dlg.clickChild<JRadioButton>(text = "Specific conversion")
        val comboBox = dlg.getChild<JComboBox<AbstractAchievement>>()
        comboBox.selectedIndex = 3
        val selectedAchievement = comboBox.selectedItemTyped()

        val playerSelector = dlg.getChild<PlayerSelector>()
        val fromTable = playerSelector.tablePlayersToSelectFrom
        fromTable.selectFirstRow()
        playerSelector.clickChild<JButton>("Select")
        fromTable.selectFirstRow()
        playerSelector.clickChild<JButton>("Select")
        val players = playerSelector.getSelectedPlayers()

        dlg.clickOk()
        waitForConversionToFinish()

        val log = findLog(CODE_ACHIEVEMENT_CONVERSION_STARTED)!!
        log.message shouldBe "Regenerating 1 achievements for 2 players"
        log.keyValuePairs[KEY_ACHIEVEMENT_TYPES] shouldBe listOf(selectedAchievement.achievementType)
        log.keyValuePairs[KEY_PLAYER_IDS] shouldBe players.map { it.rowId }

    }

    private fun waitForConversionToFinish()
    {
        awaitCondition { findLog(CODE_ACHIEVEMENT_CONVERSION_FINISHED) != null }
        flushEdt()
    }
}