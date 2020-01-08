package burlton.dartzee.code.screen.ai

import burlton.dartzee.code.`object`.ColourWrapper
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.utils.DartsColour
import burlton.dartzee.code.utils.getDistance
import burlton.desktopcore.code.bean.paint
import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.SwingConstants

class VisualisationPanelDensity: AbstractVisualisationPanel()
{
    private var keyImg: BufferedImage? = null
    private val panelKey = JLabel()

    init
    {
        add(dartboard)
        add(overlay)

        val colourWrapper = ColourWrapper(DartsColour.TRANSPARENT).also { it.edgeColour = Color.BLACK }
        dartboard.paintDartboard(colourWrapper, false)

        panelKey.setBounds(0, 0, 100, 500)
        panel.add(panelKey)
        keyImg = BufferedImage(panelKey.width, panelKey.height, BufferedImage.TYPE_INT_ARGB)
        panelKey.icon = ImageIcon(keyImg)
    }

    override fun showVisualisation(hmPointToCount: Map<Point, Int>, model: AbstractDartsModel)
    {
        val centerPt = model.getScoringPoint(dartboard)

        overlayImg.paint {
            val radius = getDistance(it, centerPt)
            val probability = model.getProbabilityWithinRadius(radius)
            getColorForProbability(probability)
        }

        repaint()
    }

    override fun paintKey()
    {
        keyImg?.paint {
            val probability: Double = it.y.toDouble() / height.toDouble()
            getColorForProbability(probability)
        }

        //Add labels at 10% increments
        val lblXPosition = panel.width / 2 - LABEL_WIDTH / 2
        val yPositions = (1 until panel.height).filter { it % (height/10) == 0 }
        yPositions.forEach {
            val probInt = 10 * it / 50
            val label = JLabel("-   $probInt%   -")
            label.setBounds(lblXPosition, it - LABEL_HEIGHT / 2, LABEL_WIDTH, LABEL_HEIGHT)
            label.horizontalAlignment = SwingConstants.CENTER
            panelKey.add(label)
        }

        repaint()
    }

    private fun getColorForProbability(probability: Double): Color
    {
        val hue = (probability / 1.2).toFloat()
        return Color.getHSBColor(hue, 1f, 1f)
    }

    companion object
    {
        private const val LABEL_WIDTH = 60
        private const val LABEL_HEIGHT = 30
    }
}