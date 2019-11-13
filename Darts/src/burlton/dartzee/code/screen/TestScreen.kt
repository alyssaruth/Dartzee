package burlton.dartzee.code.screen

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.bean.GameParamFilterPanelDartzee
import burlton.dartzee.code.db.DartzeeRoundResult
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.listener.DartboardListener
import burlton.dartzee.code.screen.dartzee.DartboardRuleVerifier
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCarousel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel

class TestScreen: EmbeddedScreen(), DartboardListener
{
    var carousel: DartzeeRuleCarousel = DartzeeRuleCarousel(listOf())
    val dartzeeSelector = GameParamFilterPanelDartzee()
    val panelNorth = JPanel()
    val panelCenter = JPanel()
    val btnReset = JButton()
    val dartboard = DartboardRuleVerifier()

    val dartsThrown = mutableListOf<Dart>()

    init
    {
        layout = BorderLayout(0, 0)

        add(dartzeeSelector, BorderLayout.SOUTH)
        add(panelCenter, BorderLayout.CENTER)
        add(panelNorth, BorderLayout.NORTH)

        panelNorth.layout = BorderLayout(0, 0)

        panelCenter.layout = BorderLayout(0, 0)
        panelCenter.add(dartboard, BorderLayout.CENTER)

        val btnPanel = JPanel()
        panelCenter.add(btnPanel, BorderLayout.SOUTH)
        btnPanel.add(btnReset)

        btnReset.preferredSize = Dimension(60, 60)
        btnReset.icon = ImageIcon(javaClass.getResource("/buttons/Reset.png"))
        btnReset.toolTipText = "Reset darts"

        btnReset.addActionListener(this)

        dartboard.paintDartboard()

        dartboard.addDartboardListener(this)
        dartzeeSelector.addActionListener(this)
    }

    override fun actionPerformed(arg0: ActionEvent) {
        when (arg0.source)
        {
            btnNext, btnBack -> super.actionPerformed(arg0)
            btnReset -> clearDarts()
            else -> updateCarousel()
        }

    }

    private fun updateCarousel()
    {
        val template = dartzeeSelector.getSelectedTemplate() ?: return

        val rules = DartzeeRuleEntity().retrieveForTemplate(template.rowId).map { it.toDto() }

        carousel = DartzeeRuleCarousel(rules)
        panelNorth.removeAll()
        panelNorth.add(carousel, BorderLayout.CENTER)
        carousel.update(makeResults(), dartboard, dartsThrown)

        dartboard.refreshValidSegments(carousel.getValidSegments(dartboard, dartsThrown))

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

    private fun clearDarts()
    {
        dartsThrown.clear()

        carousel.update(makeResults(), dartboard, dartsThrown)

        dartboard.clearDarts()
        dartboard.ensureListening()
        dartboard.refreshValidSegments(carousel.getValidSegments(dartboard, dartsThrown))
    }

    override fun dartThrown(dart: Dart)
    {
        dartsThrown.add(dart)

        if (dartsThrown.size == 3)
        {
            dartboard.stopListening()
        }

        carousel.update(makeResults(), dartboard, dartsThrown)

        dartboard.refreshValidSegments(carousel.getValidSegments(dartboard, dartsThrown))
    }

}