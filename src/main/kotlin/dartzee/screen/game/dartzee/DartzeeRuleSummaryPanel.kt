package dartzee.screen.game.dartzee

import dartzee.ai.DartsAiModel
import dartzee.db.DartzeeRoundResultEntity
import dartzee.`object`.Dart
import dartzee.screen.game.SegmentStatuses
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

    fun getSegmentStatus(): SegmentStatuses =
        when
        {
            components.contains(panelHighScore) -> SegmentStatuses(getAllPossibleSegments(), getAllPossibleSegments())
            else -> carousel.getSegmentStatus()
        }

    fun ensureReady()
    {
        while (!carousel.initialised)
        {
            Thread.sleep(200)
        }
    }

    fun selectRule(model: DartsAiModel)
    {
        carousel.selectRule(model)
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
