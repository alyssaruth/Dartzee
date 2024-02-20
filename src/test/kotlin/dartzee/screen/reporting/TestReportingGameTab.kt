package dartzee.screen.reporting

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeDisabled
import com.github.alyssaburlton.swingtest.shouldBeEnabled
import com.github.lgooddatepicker.components.DatePicker
import dartzee.bean.ComboBoxGameType
import dartzee.bean.GameParamFilterPanel
import dartzee.bean.GameParamFilterPanelRoundTheClock
import dartzee.bean.GameParamFilterPanelX01
import dartzee.bean.SpinnerX01
import dartzee.core.bean.DateFilterPanel
import dartzee.core.util.getAllChildComponentsForType
import dartzee.game.FinishType
import dartzee.game.GameType
import dartzee.game.X01Config
import dartzee.helper.AbstractTest
import dartzee.makeInvalid
import dartzee.reporting.MatchFilter
import dartzee.updateSelection
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.sql.Timestamp
import java.time.LocalDate
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JRadioButton
import org.junit.jupiter.api.Test

class TestReportingGameTab : AbstractTest() {
    @Test
    fun `Should have the correct initial state`() {
        val scrn = ReportingGameTab()
        val checkBoxes =
            scrn.getAllChildComponentsForType<JCheckBox>().filter {
                it.name?.startsWith("filter") ?: false
            }
        checkBoxes.forEach { it.isSelected shouldBe false }

        scrn.getAllChildComponentsForType<JRadioButton>().forEach { it.shouldBeDisabled() }
        scrn.getAllChildComponentsForType<JComboBox<*>>().forEach { it.shouldBeDisabled() }
        scrn.getAllChildComponentsForType<JLabel>().forEach { it.shouldBeDisabled() }
        scrn.getAllChildComponentsForType<DatePicker>().forEach { it.shouldBeDisabled() }
    }

    @Test
    fun `Should toggle game type combo box correctly`() {
        val scrn = ReportingGameTab()
        scrn.clickChild<JCheckBox>(text = "Game")
        scrn.getChild<ComboBoxGameType>().shouldBeEnabled()

        scrn.clickChild<JCheckBox>(text = "Game")
        scrn.getChild<ComboBoxGameType>().shouldBeDisabled()
    }

    @Test
    fun `Should swap in the right game param filter panel`() {
        val scrn = ReportingGameTab()
        scrn.getChild<GameParamFilterPanel>().shouldBeInstanceOf<GameParamFilterPanelX01>()

        scrn.clickChild<JCheckBox>(text = "Game")
        val comboBox = scrn.getChild<ComboBoxGameType>()
        comboBox.updateSelection(GameType.ROUND_THE_CLOCK)
        scrn
            .getChild<GameParamFilterPanel>()
            .shouldBeInstanceOf<GameParamFilterPanelRoundTheClock>()
    }

    @Test
    fun `Should toggle the game param filter panel correctly`() {
        val scrn = ReportingGameTab()
        scrn.clickChild<JCheckBox>(text = "Type")
        scrn.getChild<GameParamFilterPanel>().shouldBeEnabled()

        scrn.clickChild<JCheckBox>(text = "Type")
        scrn.getChild<GameParamFilterPanel>().shouldBeDisabled()
    }

    @Test
    fun `Should toggle the start date filter panel correctly`() {
        val scrn = ReportingGameTab()
        scrn.clickChild<JCheckBox>(text = "Start Date")
        scrn.getStartDateFilterPanel().cbDateFrom.shouldBeEnabled()

        scrn.clickChild<JCheckBox>(text = "Start Date")
        scrn.getStartDateFilterPanel().cbDateFrom.shouldBeDisabled()
    }

    @Test
    fun `Should toggle the finish date filter panel correctly`() {
        val scrn = ReportingGameTab()
        scrn.clickChild<JCheckBox>(text = "Finish Date")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeEnabled()
        scrn.getChild<JRadioButton>(text = "Finished:").shouldBeEnabled()
        scrn.getChild<JRadioButton>(text = "Unfinished").shouldBeEnabled()

        scrn.clickChild<JRadioButton>(text = "Unfinished")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeDisabled()

        scrn.clickChild<JRadioButton>(text = "Finished:")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeEnabled()

        scrn.clickChild<JCheckBox>(text = "Finish Date")
        scrn.getFinishDateFilterPanel().cbDateTo.shouldBeDisabled()
        scrn.getChild<JRadioButton>(text = "Finished:").shouldBeDisabled()
        scrn.getChild<JRadioButton>(text = "Unfinished").shouldBeDisabled()
    }

    @Test
    fun `Should toggle match radio buttons correctly`() {
        val tab = ReportingGameTab()
        tab.clickChild<JCheckBox>(text = "Part of Match")
        tab.getChild<JRadioButton>(text = "Yes").shouldBeEnabled()
        tab.getChild<JRadioButton>(text = "No").shouldBeEnabled()

        tab.clickChild<JCheckBox>(text = "Part of Match")
        tab.getChild<JRadioButton>(text = "Yes").shouldBeDisabled()
        tab.getChild<JRadioButton>(text = "No").shouldBeDisabled()
    }

    @Test
    fun `Should toggle the sync status radio buttons correctly`() {
        val tab = ReportingGameTab()
        tab.clickChild<JCheckBox>(text = "Sync Status")
        tab.getChild<JRadioButton>(text = "Pending changes").shouldBeEnabled()
        tab.getChild<JRadioButton>(text = "Synced").shouldBeEnabled()

        tab.clickChild<JCheckBox>(text = "Sync Status")
        tab.getChild<JRadioButton>(text = "Pending changes").shouldBeDisabled()
        tab.getChild<JRadioButton>(text = "Synced").shouldBeDisabled()
    }

    /** Validation */
    @Test
    fun `Should validate against the start date filters`() {
        val tab = ReportingGameTab()
        tab.clickChild<JCheckBox>(text = "Start Date")
        tab.getStartDateFilterPanel().makeInvalid()

        tab.valid() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly(
            "The 'date from' cannot be after the 'date to'"
        )
    }

    @Test
    fun `Should validate against the finish date filters`() {
        val tab = ReportingGameTab()
        tab.clickChild<JCheckBox>(text = "Finish Date")
        tab.getFinishDateFilterPanel().makeInvalid()

        tab.valid() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly(
            "The 'date from' cannot be after the 'date to'"
        )
    }

    @Test
    fun `Should be valid by default`() {
        val tab = ReportingGameTab()
        tab.valid() shouldBe true
    }

    /** Population */
    @Test
    fun `Should populate game type correctly`() {
        val tab = ReportingGameTab()
        tab.generateReportParameters().gameType shouldBe null

        tab.clickChild<JCheckBox>(text = "Game")
        tab.generateReportParameters().gameType shouldBe GameType.X01

        tab.getChild<ComboBoxGameType>().updateSelection(GameType.DARTZEE)
        tab.generateReportParameters().gameType shouldBe GameType.DARTZEE
    }

    @Test
    fun `Should populate gameParams correctly`() {
        val tab = ReportingGameTab()
        tab.generateReportParameters().gameParams shouldBe ""

        tab.clickChild<JCheckBox>(text = "Type")
        tab.generateReportParameters().gameParams shouldBe X01Config(501, FinishType.Doubles).toJson()

        tab.getChild<SpinnerX01>().value = 701
        tab.clickChild<JCheckBox>(text = "Finish on double")
        tab.generateReportParameters().gameParams shouldBe X01Config(701, FinishType.Any).toJson()
    }

    @Test
    fun `Should populate part of match correctly`() {
        val tab = ReportingGameTab()
        tab.generateReportParameters().partOfMatch shouldBe MatchFilter.BOTH

        tab.clickChild<JCheckBox>(text = "Part of Match")
        tab.generateReportParameters().partOfMatch shouldBe MatchFilter.MATCHES_ONLY

        tab.clickChild<JRadioButton>(text = "No")
        tab.generateReportParameters().partOfMatch shouldBe MatchFilter.GAMES_ONLY
    }

    @Test
    fun `Should populate sync status correctly`() {
        val tab = ReportingGameTab()
        tab.generateReportParameters().pendingChanges shouldBe null

        tab.clickChild<JCheckBox>(text = "Sync Status")
        tab.generateReportParameters().pendingChanges shouldBe true

        tab.clickChild<JRadioButton>(text = "Synced")
        tab.generateReportParameters().pendingChanges shouldBe false
    }

    @Test
    fun `Should populate start date correctly`() {
        val tab = ReportingGameTab()
        tab.generateReportParameters().dtStartFrom shouldBe null
        tab.generateReportParameters().dtStartTo shouldBe null

        val startDate = LocalDate.ofYearDay(2020, 20)
        val endDate = LocalDate.ofYearDay(2020, 30)
        tab.clickChild<JCheckBox>(text = "Start Date")
        tab.getStartDateFilterPanel().cbDateFrom.date = startDate
        tab.getStartDateFilterPanel().cbDateTo.date = endDate
        tab.generateReportParameters().dtStartFrom shouldBe
            Timestamp.valueOf(startDate.atTime(0, 0))
        tab.generateReportParameters().dtStartTo shouldBe Timestamp.valueOf(endDate.atTime(0, 0))
    }

    @Test
    fun `Should populate finish date correctly`() {
        val tab = ReportingGameTab()

        val rpDefault = tab.generateReportParameters()
        rpDefault.dtFinishFrom shouldBe null
        rpDefault.dtFinishTo shouldBe null
        rpDefault.unfinishedOnly shouldBe false

        val startDate = LocalDate.ofYearDay(2020, 20)
        val endDate = LocalDate.ofYearDay(2020, 30)
        tab.clickChild<JCheckBox>(text = "Finish Date")
        tab.getFinishDateFilterPanel().cbDateFrom.date = startDate
        tab.getFinishDateFilterPanel().cbDateTo.date = endDate
        val rpBetweenDates = tab.generateReportParameters()
        rpBetweenDates.dtFinishFrom shouldBe Timestamp.valueOf(startDate.atTime(0, 0))
        rpBetweenDates.dtFinishTo shouldBe Timestamp.valueOf(endDate.atTime(0, 0))
        rpBetweenDates.unfinishedOnly shouldBe false

        tab.clickChild<JRadioButton>(text = "Unfinished")
        val rpUnfinished = tab.generateReportParameters()
        rpUnfinished.dtFinishFrom shouldBe null
        rpUnfinished.dtFinishTo shouldBe null
        rpUnfinished.unfinishedOnly shouldBe true
    }

    private fun ReportingGameTab.getStartDateFilterPanel() =
        getAllChildComponentsForType<DateFilterPanel>().first()

    private fun ReportingGameTab.getFinishDateFilterPanel() =
        getAllChildComponentsForType<DateFilterPanel>()[1]
}
