package burlton.dartzee.code.screen

import burlton.dartzee.code.bean.GameParamFilterPanelDartzee
import burlton.dartzee.code.db.DartzeeRoundResult
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.screen.dartzee.DartboardRuleVerifier
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCarousel
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.JPanel

class TestScreen: EmbeddedScreen()
{
    val dartzeeSelector = GameParamFilterPanelDartzee()
    val panelNorth = JPanel()
    val dartboard = DartboardRuleVerifier()

    init
    {
        layout = BorderLayout(0, 0)

        add(dartzeeSelector, BorderLayout.SOUTH)
        add(dartboard, BorderLayout.CENTER)
        add(panelNorth, BorderLayout.NORTH)

        panelNorth.layout = BorderLayout(0, 0)

        dartboard.paintDartboard()

        dartzeeSelector.addActionListener(this)
    }

    override fun actionPerformed(arg0: ActionEvent) {
        when (arg0.source)
        {
            btnNext, btnBack -> super.actionPerformed(arg0)
            else -> updateCarousel()
        }

    }

    private fun updateCarousel()
    {
        val template = dartzeeSelector.getSelectedTemplate() ?: return

        val rules = DartzeeRuleEntity().retrieveForTemplate(template.rowId).map { it.toDto() }

        val carousel = DartzeeRuleCarousel(rules)
        panelNorth.removeAll()
        panelNorth.add(carousel, BorderLayout.CENTER)
        carousel.update(makeResults())

        dartboard.refreshValidSegments(carousel.getValidSegments())

        ScreenCache.getMainScreen().pack()
        panelNorth.repaint()
        repaint()
    }

    private fun makeResults(): List<DartzeeRoundResult>
    {
        val result1 = DartzeeRoundResult()
        result1.ruleNumber = 1
        result1.success = false

        val result2 = DartzeeRoundResult()
        result2.ruleNumber = 3
        result2.success = true

        return listOf(result1, result2)
    }

    override fun initialise() { }

    override fun getScreenName() = "Test Screen"

}