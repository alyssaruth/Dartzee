package dartzee.screen.game.dartzee

import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.db.DartzeeRoundResultEntity
import dartzee.utils.getAllPossibleSegments
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class DartzeeRuleSummaryPanel(private val carousel: DartzeeRuleCarousel): JPanel()
{
    private val lblHighScore = JLabel()
    val panelHighScore = JPanel()

    init
    {
        layout = BorderLayout(0, 0)
        preferredSize = Dimension(150, 120)

        lblHighScore.icon = ImageIcon(javaClass.getResource("/icons/dartzeeScoringRound.png"))
        lblHighScore.background = Color.BLACK
        lblHighScore.horizontalAlignment = SwingConstants.CENTER
        lblHighScore.verticalAlignment = SwingConstants.CENTER

        panelHighScore.add(lblHighScore)
        add(panelHighScore)
    }

    fun update(results: List<DartzeeRoundResultEntity>, darts: List<Dart>, currentScore: Int, roundNumber: Int)
    {
        if (roundNumber == 1)
        {
            swapInComponentIfNecessary(panelHighScore)
        }
        else
        {
            swapInComponentIfNecessary(carousel)
            carousel.update(results, darts, currentScore)
        }
    }

    private fun swapInComponentIfNecessary(c: Component)
    {
        if (components.contains(c))
        {
            return
        }

        removeAll()

        add(c)
        validate()
        repaint()
    }

    fun getSegmentStatus(): SegmentStatus? =
        when
        {
            components.contains(panelHighScore) -> null
            else -> carousel.getSegmentStatus()
        }

    fun gameFinished()
    {
        swapInComponentIfNecessary(carousel)
        carousel.gameFinished()
    }

    fun setCarouselListener(listener: IDartzeeCarouselListener) {
        carousel.listener = listener
    }
}
