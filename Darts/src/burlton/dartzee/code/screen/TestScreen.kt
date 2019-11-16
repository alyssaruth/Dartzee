package burlton.dartzee.code.screen

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.bean.GameParamFilterPanelDartzee
import burlton.dartzee.code.db.DartzeeRoundResultEntity
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.listener.DartboardListener
import burlton.dartzee.code.screen.dartzee.DartboardRuleVerifier
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCarousel
import burlton.dartzee.code.screen.dartzee.IDartzeeTileListener
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel

class TestScreen: EmbeddedScreen(), DartboardListener, IDartzeeTileListener
{
    var carousel: DartzeeRuleCarousel = DartzeeRuleCarousel(listOf())
    val dartzeeSelector = GameParamFilterPanelDartzee()
    val panelNorth = JPanel()
    val panelCenter = JPanel()
    private val btnReset = JButton()
    private val btnConfirm = JButton()
    val dartboard = DartboardRuleVerifier()

    val dartsThrown = mutableListOf<Dart>()
    val ruleResults = mutableListOf<DartzeeRoundResultEntity>()

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
        btnPanel.add(btnConfirm)

        btnReset.preferredSize = Dimension(60, 60)
        btnReset.icon = ImageIcon(javaClass.getResource("/buttons/Reset.png"))
        btnReset.toolTipText = "Reset darts"
        btnConfirm.isEnabled = false
        btnConfirm.preferredSize = Dimension(60, 60)
        btnConfirm.icon = ImageIcon(javaClass.getResource("/buttons/Confirm.png"))
        btnConfirm.toolTipText = "Confirm round"

        btnReset.addActionListener(this)
        btnConfirm.addActionListener(this)

        dartboard.paintDartboard()

        dartboard.addDartboardListener(this)
        dartzeeSelector.addActionListener(this)
    }

    override fun actionPerformed(arg0: ActionEvent) {
        when (arg0.source)
        {
            btnNext, btnBack -> super.actionPerformed(arg0)
            btnReset -> clearDarts()
            btnConfirm -> confirmDarts()
            else -> updateCarousel()
        }

    }

    private fun updateCarousel()
    {
        val template = dartzeeSelector.getSelectedTemplate() ?: return

        val rules = DartzeeRuleEntity().retrieveForTemplate(template.rowId).map { it.toDto() }
        ruleResults.clear()

        carousel = DartzeeRuleCarousel(rules)
        panelNorth.removeAll()
        panelNorth.add(carousel, BorderLayout.CENTER)
        carousel.update(ruleResults, dartsThrown)

        if (dartsThrown.size < 3)
        {
            dartboard.refreshValidSegments(carousel.getValidSegments(dartsThrown))
        }

        ScreenCache.getMainScreen().pack()
        panelNorth.repaint()
        repaint()
    }

    override fun initialise() { }

    override fun getScreenName() = "Test Screen"

    private fun clearDarts()
    {
        dartsThrown.clear()

        btnConfirm.isEnabled = false
        carousel.update(ruleResults, dartsThrown)

        dartboard.clearDarts()
        dartboard.ensureListening()
        dartboard.refreshValidSegments(carousel.getValidSegments(dartsThrown))
    }

    private fun confirmDarts()
    {
        val result = carousel.getRoundResult()
        if (!result.userInputNeeded)
        {
            completeRound(result.ruleNumber, result.success)
        }
        else
        {
            btnConfirm.isEnabled = false
            btnReset.isEnabled = false
            carousel.addTileListener(this)
        }
    }

    private fun completeRound(ruleNumber: Int, success: Boolean)
    {
        val entity = DartzeeRoundResultEntity()
        entity.ruleNumber = ruleNumber
        entity.success = success

        ruleResults.add(entity)

        clearDarts()
    }

    override fun dartThrown(dart: Dart)
    {
        dartsThrown.add(dart)
        btnReset.isEnabled = true

        carousel.update(ruleResults, dartsThrown)

        val validSegments = carousel.getValidSegments(dartsThrown)
        if (validSegments.isEmpty() || dartsThrown.size == 3)
        {
            dartboard.stopListening()
            btnConfirm.isEnabled = true
        }

        dartboard.refreshValidSegments(validSegments)
    }

    override fun tilePressed(ruleNumber: Int, success: Boolean)
    {
        carousel.clearTileListener()

        completeRound(ruleNumber, success)
    }
}