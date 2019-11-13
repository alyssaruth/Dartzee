package burlton.dartzee.code.screen

import burlton.dartzee.code.bean.GameParamFilterPanelDartzee
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCarousel
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import javax.swing.JPanel

class TestScreen: EmbeddedScreen()
{
    val dartzeeSelector = GameParamFilterPanelDartzee()
    val panelNorth = JPanel()

    init
    {
        layout = BorderLayout(0, 0)

        add(dartzeeSelector, BorderLayout.CENTER)
        add(panelNorth, BorderLayout.NORTH)

        panelNorth.layout = BorderLayout(0, 0)

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

        ScreenCache.getMainScreen().pack()
        panelNorth.repaint()
        repaint()
    }

    override fun initialise() { }

    override fun getScreenName() = "Test Screen"

}