package dartzee.screen.ai

import dartzee.`object`.ColourWrapper
import dartzee.ai.DartsAiModel
import dartzee.core.bean.paint
import dartzee.utils.DartsColour
import dartzee.utils.ResourceCache
import dartzee.utils.getDistance
import java.awt.*
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.SwingConstants

class VisualisationPanelDensity: AbstractVisualisationPanel()
{
    private val keyImg = BufferedImage(100, 500, BufferedImage.TYPE_INT_ARGB)
    val panelKey = JLabel()

    init
    {
        add(dartboard)
        add(overlay)

        val colourWrapper = ColourWrapper(DartsColour.TRANSPARENT).also { it.edgeColour = Color.BLACK }
        dartboard.paintDartboard(colourWrapper, false)

        panelKey.setBounds(0, 0, 100, 500)
        panel.add(panelKey)
        panelKey.icon = ImageIcon(keyImg)
    }

    override fun showVisualisation(hmPointToCount: Map<Point, Int>, model: DartsAiModel)
    {
        val centerPt = model.getScoringPoint(dartboard)

        val divisor = model.getProbabilityDensityDivisor()

        overlayImg.paint {
            val radius = getDistance(it, centerPt)
            val probability = model.getProbabilityWithinRadius(radius)
            getColorForProbability(probability?.div(divisor))
        }

        repaint()
    }

    override fun paintKey()
    {
        keyImg.paint {
            val probability: Double = it.y.toDouble() / height.toDouble()
            getColorForProbability(probability)
        }

        //Add labels at 10% increments
        val lblXPosition = panel.width / 2 - LABEL_WIDTH / 2
        val yPositions = (1 until 500).filter { it % (500/10) == 0 }
        yPositions.forEach {
            val probInt = 10 * it / 50
            val label = JLabel("-   $probInt%   -")
            label.font = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 14f)
            label.setSize(LABEL_WIDTH, LABEL_HEIGHT)
            label.horizontalAlignment = SwingConstants.CENTER

            val g = keyImg.graphics as Graphics2D
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.translate(lblXPosition, it - LABEL_HEIGHT / 2)
            label.paint(g)
        }

        repaint()
    }

    private fun getColorForProbability(probability: Double?): Color
    {
        probability ?: return Color.BLACK

        val hue = (probability / 1.2).toFloat()
        return Color.getHSBColor(hue, 1f, 1f)
    }

    companion object
    {
        private const val LABEL_WIDTH = 60
        private const val LABEL_HEIGHT = 40
    }
}