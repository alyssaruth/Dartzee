package dartzee.screen.reporting

import com.github.alexburlton.swingtest.clickChild
import com.github.alexburlton.swingtest.getChild
import com.github.alexburlton.swingtest.shouldBeDisabled
import com.github.alexburlton.swingtest.shouldBeEnabled
import com.github.lgooddatepicker.components.DatePicker
import dartzee.bean.*
import dartzee.core.bean.DateFilterPanel
import dartzee.core.util.getAllChildComponentsForType
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.makeInvalid
import dartzee.reporting.MatchFilter
import dartzee.reporting.ReportParameters
import dartzee.updateSelection
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
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
        scrn.clickChild<JCheckBox>("Game")
        scrn.getChild<ComboBoxGameType>().shouldBeEnabled()

        scrn.clickChild<JCheckBox>("Game")
        scrn.getChild<ComboBoxGameType>().shouldBeDisabled()
    }

    @Test
    fun `Should swap in the right game param filter panel`()
    {
        val scrn = ReportingGameTab()
        scrn.getChild<GameParamFilterPanel>().shouldBeInstanceOf<GameParamFilterPanelX01>()

        scrn.clickChild<JCheckBox>("Game")
        val comboBox = scrn.getChild<ComboBoxGameType>()
        comboBox.updateSelection(GameType.ROUND_THE_CLOCK)
        scrn.getChild<GameParamFilterPanel>().shouldBeInstanceOf<GameParamFilterPanelRoundTheClock>()
    }

    @Test
    fun `Should toggle the game param filter panel correctly`()
    {
        val scrn = ReportingGameTab()
        scrn.clickChild<JCheckBox>("Type")
        scrn.getChild<GameParamFilterPanel>().shouldBeEnabled()

        scrn.clickChild<JCheckBox>("Type")
        scrn.getChild<GameParamFilterPanel>().shouldBeDisabled()
    }

    @Test
    fun `Should toggle the start date filter panel correctly`()
    {
        val scrn = ReportingGameTab()
        scrn.clickChild<JCheckBox>("Start Date")
        scrn.getStartDateFilterPanel().cbDateFrom.shouldBeEnabled()

        scrn.clickChild<JCheckBox>("Start Date")
        scrn.getStartDateFilterPanel().cbDateFrom.shouldBeDisabled()
    }

    @Test
    fun `Should toggle the finish date filter panel correctly`()
    {
        val scrn = ReportingGameTab()
        scrn.clickChild<JCheckBox>("Finish Date")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeEnabled()
        scrn.getChild<JRadioButton>("Finished:").shouldBeEnabled()
        scrn.getChild<JRadioButton>("Unfinished").shouldBeEnabled()

        scrn.clickChild<JRadioButton>("Unfinished")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeDisabled()

        scrn.clickChild<JRadioButton>("Finished:")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeEnabled()

        scrn.clickChild<JCheckBox>("Finish Date")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeDisabled()
        scrn.getChild<JRadioButton>("Finished:").shouldBeDisabled()
        scrn.getChild<JRadioButton>("Unfinished").shouldBeDisabled()
    }

    @Test
    fun `Should toggle match radio buttons correctly`()
    {
        val tab = ReportingGameTab()
        tab.clickChild<JCheckBox>("Part of Match")
        tab.getChild<JRadioButton>("Yes").shouldBeEnabled()
        tab.getChild<JRadioButton>("No").shouldBeEnabled()

        tab.clickChild<JCheckBox>("Part of Match")
        tab.getChild<JRadioButton>("Yes").shouldBeDisabled()
        tab.getChild<JRadioButton>("No").shouldBeDisabled()
    }

    @Test
    fun `Should toggle the sync status radio buttons correctly`()
    {
        val tab = ReportingGameTab()
        tab.clickChild<JCheckBox>("Sync Status")
        tab.getChild<JRadioButton>("Pending changes").shouldBeEnabled()
        tab.getChild<JRadioButton>("Synced").shouldBeEnabled()

        tab.clickChild<JCheckBox>("Sync Status")
        tab.getChild<JRadioButton>("Pending changes").shouldBeDisabled()
        tab.getChild<JRadioButton>("Synced").shouldBeDisabled()
    }

    /**
     * Validation
     */
    @Test
    fun `Should validate against the start date filters`()
    {
        val tab = ReportingGameTab()
        tab.clickChild<JCheckBox>("Start Date")
        tab.getStartDateFilterPanel().makeInvalid()

        tab.valid() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("The 'date from' cannot be after the 'date to'")
    }

    @Test
    fun `Should validate against the finish date filters`()
    {
        val tab = ReportingGameTab()
        tab.clickChild<JCheckBox>("Finish Date")
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

        tab.clickChild<JCheckBox>("Game")
        tab.populateReportParameters(rp)
        rp.gameType shouldBe GameType.X01

        tab.getChild<ComboBoxGameType>().updateSelection(GameType.DARTZEE)
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

        tab.clickChild<JCheckBox>("Type")
        tab.populateReportParameters(rp)
        rp.gameParams shouldBe "501"

        tab.getChild<SpinnerX01>().value = 701
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

        tab.clickChild<JCheckBox>("Part of Match")
        tab.populateReportParameters(rp)
        rp.partOfMatch shouldBe MatchFilter.MATCHES_ONLY

        tab.clickChild<JRadioButton>("No")
        tab.populateReportParameters(rp)
        rp.partOfMatch shouldBe MatchFilter.GAMES_ONLY
    }

    @Test
    fun `Should populate sync status correctly`()
    {
        val rp = ReportParameters()
        val tab = ReportingGameTab()

        tab.populateReportParameters(rp)
        rp.pendingChanges shouldBe null

        tab.clickChild<JCheckBox>("Sync Status")
        tab.populateReportParameters(rp)
        rp.pendingChanges shouldBe true

        tab.clickChild<JRadioButton>("Synced")
        tab.populateReportParameters(rp)
        rp.pendingChanges shouldBe false
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
        tab.clickChild<JCheckBox>("Start Date")
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
        tab.clickChild<JCheckBox>("Finish Date")
        tab.getFinishDateFilterPanel().cbDateFrom.date = startDate
        tab.getFinishDateFilterPanel().cbDateTo.date = endDate
        tab.populateReportParameters(rp)
        rp.dtFinishFrom shouldBe Timestamp.valueOf(startDate.atTime(0, 0))
        rp.dtFinishTo shouldBe Timestamp.valueOf(endDate.atTime(0, 0))
        rp.unfinishedOnly shouldBe false

        rp = ReportParameters()
        tab.clickChild<JRadioButton>("Unfinished")
        tab.populateReportParameters(rp)
        rp.dtFinishFrom shouldBe null
        rp.dtFinishTo shouldBe null
        rp.unfinishedOnly shouldBe true
    }

    private fun ReportingGameTab.getStartDateFilterPanel() = getAllChildComponentsForType<DateFilterPanel>().first()
    private fun ReportingGameTab.getFinishDateFilterPanel() = getAllChildComponentsForType<DateFilterPanel>()[1]
}