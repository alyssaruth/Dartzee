package dartzee.screen.reporting

import com.github.lgooddatepicker.components.DatePicker
import dartzee.*
import dartzee.bean.ComboBoxGameType
import dartzee.bean.GameParamFilterPanel
import dartzee.bean.GameParamFilterPanelRoundTheClock
import dartzee.bean.GameParamFilterPanelX01
import dartzee.core.bean.DateFilterPanel
import dartzee.core.util.getAllChildComponentsForType
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.Test
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JRadioButton

class TestReportingGameTab: AbstractTest()
{
    @Test
    fun `Should have the correct initial state`()
    {
        val scrn = ReportingGameTab()
        val checkBoxes = scrn.getAllChildComponentsForType<JCheckBox>()
        checkBoxes.forEach { it.isSelected shouldBe false }

        scrn.getAllChildComponentsForType<JRadioButton>().forEach { it.shouldBeDisabled() }
        scrn.getAllChildComponentsForType<JComboBox<*>>().forEach { it.shouldBeDisabled() }
        scrn.getAllChildComponentsForType<JLabel>().forEach { it.shouldBeDisabled() }
        scrn.getAllChildComponentsForType<DatePicker>().forEach { it.shouldBeDisabled() }
    }

    @Test
    fun `Should toggle game type combo box correctly`()
    {
        val scrn = ReportingGameTab()
        scrn.clickComponent<JCheckBox>("Game")
        scrn.findComponent<ComboBoxGameType>().shouldBeEnabled()

        scrn.clickComponent<JCheckBox>("Game")
        scrn.findComponent<ComboBoxGameType>().shouldBeDisabled()
    }

    @Test
    fun `Should swap in the right game param filter panel`()
    {
        val scrn = ReportingGameTab()
        scrn.findComponent<GameParamFilterPanel>().shouldBeInstanceOf<GameParamFilterPanelX01>()

        scrn.clickComponent<JCheckBox>("Game")
        val comboBox = scrn.findComponent<ComboBoxGameType>()
        comboBox.updateSelection(GameType.ROUND_THE_CLOCK)
        scrn.findComponent<GameParamFilterPanel>().shouldBeInstanceOf<GameParamFilterPanelRoundTheClock>()
    }

    @Test
    fun `Should toggle the game param filter panel correctly`()
    {
        val scrn = ReportingGameTab()
        scrn.clickComponent<JCheckBox>("Type")
        scrn.findComponent<GameParamFilterPanel>().shouldBeEnabled()

        scrn.clickComponent<JCheckBox>("Type")
        scrn.findComponent<GameParamFilterPanel>().shouldBeDisabled()
    }

    @Test
    fun `Should toggle the start date filter panel correctly`()
    {
        val scrn = ReportingGameTab()
        scrn.clickComponent<JCheckBox>("Start Date")
        scrn.getStartDateFilterPanel().cbDateFrom.shouldBeEnabled()

        scrn.clickComponent<JCheckBox>("Start Date")
        scrn.getStartDateFilterPanel().cbDateFrom.shouldBeDisabled()
    }

    @Test
    fun `Should toggle the finish date filter panel correctly`()
    {
        val scrn = ReportingGameTab()
        scrn.clickComponent<JCheckBox>("Finish Date")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeEnabled()
        scrn.findComponent<JRadioButton>("Finished:").shouldBeEnabled()
        scrn.findComponent<JRadioButton>("Unfinished").shouldBeEnabled()

        scrn.clickComponent<JRadioButton>("Unfinished")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeDisabled()

        scrn.clickComponent<JRadioButton>("Finished:")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeEnabled()

        scrn.clickComponent<JCheckBox>("Finish Date")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeDisabled()
        scrn.findComponent<JRadioButton>("Finished:").shouldBeDisabled()
        scrn.findComponent<JRadioButton>("Unfinished").shouldBeDisabled()
    }

    @Test
    fun `Should toggle match radio buttons correctly`()
    {
        val tab = ReportingGameTab()
        tab.clickComponent<JCheckBox>("Part of Match")
        tab.findComponent<JRadioButton>("Yes").shouldBeEnabled()
        tab.findComponent<JRadioButton>("No").shouldBeEnabled()

        tab.clickComponent<JCheckBox>("Part of Match")
        tab.findComponent<JRadioButton>("Yes").shouldBeDisabled()
        tab.findComponent<JRadioButton>("No").shouldBeDisabled()
    }

    private fun ReportingGameTab.getStartDateFilterPanel() = getAllChildComponentsForType<DateFilterPanel>().first()
    private fun ReportingGameTab.getFinishDateFilterPanel() = getAllChildComponentsForType<DateFilterPanel>()[1]
}