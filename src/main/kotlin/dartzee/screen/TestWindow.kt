package dartzee.screen

import dartzee.core.util.doBadLuck
import dartzee.core.util.doChucklevision
import dartzee.dartzee.DartzeeRoundResult
import dartzee.db.DartzeeRuleEntity
import dartzee.db.DartzeeTemplateEntity
import dartzee.listener.DartboardListener
import dartzee.`object`.Dart
import dartzee.screen.game.SegmentStatuses
import dartzee.screen.game.dartzee.DartzeeRuleCarousel
import dartzee.screen.game.dartzee.IDartzeeCarouselListener
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

class TestWindow : JFrame(), ActionListener, DartboardListener, IDartzeeCarouselListener
{
    private val dartsThrown = mutableListOf<Dart>()
    private val template = DartzeeTemplateEntity().retrieveEntities().first()
    private val rules = DartzeeRuleEntity().retrieveForTemplate(template.rowId).map { it.toDto() }

    private val dartboard = GameplayDartboard()
    private val carousel = DartzeeRuleCarousel(rules)
    private val btnClear = JButton("Clear darts")
    private val btnRepaint = JButton("Repaint dartboard")
    private val btnChucklevision = JButton("Chucklevision")
    private val btnBadLuck = JButton("Bad luck")

    init
    {
        contentPane.layout = BorderLayout(0, 0)
        size = Dimension(1000, 800)
        preferredSize = Dimension(1000, 800)

        contentPane.add(carousel, BorderLayout.NORTH)
        carousel.update(emptyList(), emptyList(), 100)
        dartboard.refreshValidSegments(carousel.getSegmentStatus())

        contentPane.add(dartboard, BorderLayout.CENTER)

        val panelSouth = JPanel()
        contentPane.add(panelSouth, BorderLayout.SOUTH)

        panelSouth.add(btnClear)
        panelSouth.add(btnRepaint)
        panelSouth.add(btnChucklevision)
        panelSouth.add(btnBadLuck)

        dartboard.addDartboardListener(this)
        carousel.listener = this
        btnClear.addActionListener(this)
        btnRepaint.addActionListener(this)
        btnChucklevision.addActionListener(this)
        btnBadLuck.addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        when (e?.source) {
            btnClear -> clearDarts()
            btnRepaint -> dartboard.repaint()
            btnChucklevision -> dartboard.doChucklevision()
            btnBadLuck -> dartboard.doBadLuck()
        }
    }

    private fun clearDarts()
    {
        dartboard.clearDarts()
        dartsThrown.clear()
        carousel.update(emptyList(), dartsThrown, 100)
        dartboard.refreshValidSegments(carousel.getSegmentStatus())
    }

    override fun dartThrown(dart: Dart)
    {
        dartsThrown.add(dart)

        if (dartsThrown.size <= 3) {
            carousel.update(emptyList(), dartsThrown, 100)
            dartboard.refreshValidSegments(carousel.getSegmentStatus())
        }
    }

    override fun hoverChanged(segmentStatuses: SegmentStatuses)
    {
       dartboard.refreshValidSegments(segmentStatuses)
    }

    override fun tilePressed(dartzeeRoundResult: DartzeeRoundResult) {
        // do nothing
    }
}