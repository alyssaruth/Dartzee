package dartzee.screen.reporting

import com.github.lgooddatepicker.components.DatePicker
import dartzee.*
import dartzee.bean.*
import dartzee.core.bean.DateFilterPanel
import dartzee.core.util.getAllChildComponentsForType
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.reporting.MatchFilter
import dartzee.reporting.ReportParameters
import find
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.Test
import shouldBeDisabled
import shouldBeEnabled
import java.sql.Timestamp
import java.time.LocalDate
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
        scrn.find<ComboBoxGameType>()!!.shouldBeEnabled()

        scrn.clickComponent<JCheckBox>("Game")
        scrn.find<ComboBoxGameType>()!!.shouldBeDisabled()
    }

    @Test
    fun `Should swap in the right game param filter panel`()
    {
        val scrn = ReportingGameTab()
        scrn.find<GameParamFilterPanel>().shouldBeInstanceOf<GameParamFilterPanelX01>()

        scrn.clickComponent<JCheckBox>("Game")
        val comboBox = scrn.find<ComboBoxGameType>()!!
        comboBox.updateSelection(GameType.ROUND_THE_CLOCK)
        scrn.find<GameParamFilterPanel>().shouldBeInstanceOf<GameParamFilterPanelRoundTheClock>()
    }

    @Test
    fun `Should toggle the game param filter panel correctly`()
    {
        val scrn = ReportingGameTab()
        scrn.clickComponent<JCheckBox>("Type")
        scrn.find<GameParamFilterPanel>()!!.shouldBeEnabled()

        scrn.clickComponent<JCheckBox>("Type")
        scrn.find<GameParamFilterPanel>()!!.shouldBeDisabled()
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
        scrn.find<JRadioButton>("Finished:")!!.shouldBeEnabled()
        scrn.find<JRadioButton>("Unfinished")!!.shouldBeEnabled()

        scrn.clickComponent<JRadioButton>("Unfinished")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeDisabled()

        scrn.clickComponent<JRadioButton>("Finished:")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeEnabled()

        scrn.clickComponent<JCheckBox>("Finish Date")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeDisabled()
        scrn.find<JRadioButton>("Finished:")!!.shouldBeDisabled()
        scrn.find<JRadioButton>("Unfinished")!!.shouldBeDisabled()
    }

    @Test
    fun `Should toggle match radio buttons correctly`()
    {
        val tab = ReportingGameTab()
        tab.clickComponent<JCheckBox>("Part of Match")
        tab.find<JRadioButton>("Yes")!!.shouldBeEnabled()
        tab.find<JRadioButton>("No")!!.shouldBeEnabled()

        tab.clickComponent<JCheckBox>("Part of Match")
        tab.find<JRadioButton>("Yes")!!.shouldBeDisabled()
        tab.find<JRadioButton>("No")!!.shouldBeDisabled()
    }

    /**
     * Validation
     */
    @Test
    fun `Should validate against the start date filters`()
    {
        val tab = ReportingGameTab()
        tab.clickComponent<JCheckBox>("Start Date")
        tab.getStartDateFilterPanel().makeInvalid()

        tab.valid() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("The 'date from' cannot be after the 'date to'")
    }

    @Test
    fun `Should validate against the finish date filters`()
    {
        val tab = ReportingGameTab()
        tab.clickComponent<JCheckBox>("Finish Date")
        tab.getFinishDateFilterPanel().makeInvalid()

        tab.valid() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("The 'date from' cannot be after the 'date to'")
    }

    @Test
    fun `Should be valid by default`()
    {
        val tab = ReportingGameTab()
        tab.valid() shouldBe true
    }

    /**
     * Population
     */
    @Test
    fun `Should populate game type correctly`()
    {
        val rp = ReportParameters()
        val tab = ReportingGameTab()

        tab.populateReportParameters(rp)
        rp.gameType shouldBe null

        tab.clickComponent<JCheckBox>("Game")
        tab.populateReportParameters(rp)
        rp.gameType shouldBe GameType.X01

        tab.find<ComboBoxGameType>()!!.updateSelection(GameType.DARTZEE)
        tab.populateReportParameters(rp)
        rp.gameType shouldBe GameType.DARTZEE
    }

    @Test
    fun `Should populate gameParams correctly`()
    {
        val rp = ReportParameters()
        val tab = ReportingGameTab()

        tab.populateReportParameters(rp)
        rp.gameParams shouldBe ""

        tab.clickComponent<JCheckBox>("Type")
        tab.populateReportParameters(rp)
        rp.gameParams shouldBe "501"

        tab.find<SpinnerX01>()!!.value = 701
        tab.populateReportParameters(rp)
        rp.gameParams shouldBe "701"
    }

    @Test
    fun `Should populate part of match correctly`()
    {
        val rp = ReportParameters()
        val tab = ReportingGameTab()

        tab.populateReportParameters(rp)
        rp.partOfMatch shouldBe MatchFilter.BOTH

        tab.clickComponent<JCheckBox>("Part of Match")
        tab.populateReportParameters(rp)
        rp.partOfMatch shouldBe MatchFilter.MATCHES_ONLY

        tab.clickComponent<JRadioButton>("No")
        tab.populateReportParameters(rp)
        rp.partOfMatch shouldBe MatchFilter.GAMES_ONLY
    }

    @Test
    fun `Should populate start date correctly`()
    {
        val rp = ReportParameters()
        val tab = ReportingGameTab()

        tab.populateReportParameters(rp)
        rp.dtStartFrom shouldBe null
        rp.dtStartTo shouldBe null

        val startDate = LocalDate.ofYearDay(2020, 20)
        val endDate = LocalDate.ofYearDay(2020, 30)
        tab.clickComponent<JCheckBox>("Start Date")
        tab.getStartDateFilterPanel().cbDateFrom.date = startDate
        tab.getStartDateFilterPanel().cbDateTo.date = endDate
        tab.populateReportParameters(rp)
        rp.dtStartFrom shouldBe Timestamp.valueOf(startDate.atTime(0, 0))
        rp.dtStartTo shouldBe Timestamp.valueOf(endDate.atTime(0, 0))
    }

    @Test
    fun `Should populate finish date correctly`()
    {
        var rp = ReportParameters()
        val tab = ReportingGameTab()

        tab.populateReportParameters(rp)
        rp.dtFinishFrom shouldBe null
        rp.dtFinishTo shouldBe null
        rp.unfinishedOnly shouldBe false

        val startDate = LocalDate.ofYearDay(2020, 20)
        val endDate = LocalDate.ofYearDay(2020, 30)
        tab.clickComponent<JCheckBox>("Finish Date")
        tab.getFinishDateFilterPanel().cbDateFrom.date = startDate
        tab.getFinishDateFilterPanel().cbDateTo.date = endDate
        tab.populateReportParameters(rp)
        rp.dtFinishFrom shouldBe Timestamp.valueOf(startDate.atTime(0, 0))
        rp.dtFinishTo shouldBe Timestamp.valueOf(endDate.atTime(0, 0))
        rp.unfinishedOnly shouldBe false

        rp = ReportParameters()
        tab.clickComponent<JRadioButton>("Unfinished")
        tab.populateReportParameters(rp)
        rp.dtFinishFrom shouldBe null
        rp.dtFinishTo shouldBe null
        rp.unfinishedOnly shouldBe true
    }

    private fun ReportingGameTab.getStartDateFilterPanel() = getAllChildComponentsForType<DateFilterPanel>().first()
    private fun ReportingGameTab.getFinishDateFilterPanel() = getAllChildComponentsForType<DateFilterPanel>()[1]
}