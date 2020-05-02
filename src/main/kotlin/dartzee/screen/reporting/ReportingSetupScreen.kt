package dartzee.screen.reporting

import dartzee.reporting.ReportParameters
import dartzee.screen.EmbeddedScreen
import dartzee.screen.ScreenCache
import java.awt.BorderLayout
import javax.swing.JTabbedPane
import javax.swing.SwingConstants

class ReportingSetupScreen: EmbeddedScreen()
{
    private val tabbedPane = JTabbedPane(SwingConstants.TOP)
    private val gameTab = ReportingGameTab()
    private val playerTab = ReportingPlayersTab()

    init
    {
        add(tabbedPane, BorderLayout.CENTER)

        tabbedPane.addTab("Game", null, gameTab, null)
    }

    override fun getScreenName() = "Report Setup"
    override fun initialise() {}

    private fun valid() = gameTab.valid() && playerTab.valid()

    override fun showNextButton() = true
    override fun nextPressed()
    {
        if (!valid())
        {
            return
        }

        val scrn = ScreenCache.get<ReportingResultsScreen>()

        val rp = generateReportParams()
        scrn.setReportParameters(rp)

        ScreenCache.switch(scrn)
    }

    private fun generateReportParams(): ReportParameters
    {
        val rp = ReportParameters()
        gameTab.populateReportParameters(rp)
        playerTab.populateReportParameters(rp)
        return rp
    }
}
