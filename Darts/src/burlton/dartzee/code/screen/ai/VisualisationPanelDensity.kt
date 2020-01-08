package burlton.dartzee.code.screen.ai

import burlton.dartzee.code.`object`.ColourWrapper
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.utils.DartsColour
import burlton.dartzee.code.utils.getDistance
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
        val width = overlay.width
        val height = overlay.height
        val pixels = IntArray(width * height)
        var i = 0
        for (y in 0 until height)
        {
            for (x in 0 until width)
            {
                val radius = getDistance(Point(x, y), centerPt)
                val probability = model.getProbabilityWithinRadius(radius)
                val c = getColorForProbability(probability)
                pixels[i] = c.rgb
                i++
            }
        }

        overlayImg.setRGB(0, 0, width, height, pixels, 0, width)
        repaint()
    }

    override fun paintKey()
    {
        val width = panelKey.width
        val height = panelKey.height
        val pixels = IntArray(width * height)

        val lblXPosition = panel.width / 2 - LABEL_WIDTH / 2

        var i = 0
        for (y in 0 until height)
        {
            for (x in 0 until width)
            {
                val probability: Double = y.toDouble() / height.toDouble()
                val c = getColorForProbability(probability)
                pixels[i] = c.rgb
                i++

                //Add the labels.
                if (x == lblXPosition && y % 50 == 0 && y > 0)
                {
                    val probInt = 10 * y / 50
                    //Add a label
                    val label = JLabel("-   $probInt%   -")
                    label.setBounds(
                        lblXPosition,
                        y - LABEL_HEIGHT / 2,
                        LABEL_WIDTH,
                        LABEL_HEIGHT
                    )

                    label.horizontalAlignment = SwingConstants.CENTER
                    panelKey.add(label)
                }
            }
        }
        keyImg!!.setRGB(0, 0, width, height, pixels, 0, width)
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