package dartzee.screen.ai

import dartzee.ai.DartsAiModel
import dartzee.bean.PresentationDartboard
import dartzee.`object`.ColourWrapper
import dartzee.utils.DartsColour
import java.awt.Point
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel

abstract class AbstractVisualisationPanel : JPanel()
{
    //Cached stuff
    private var paintedKey = false

    val dartboard = PresentationDartboard(factoryColourWrapper())
    var overlayImg = BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB)

    protected val overlay = JLabel()
    protected val panel = JPanel()

    init
    {
        layout = null
        overlay.setBounds(0, 0, 500, 500)
        dartboard.setBounds(0, 0, 500, 500)
        panel.setBounds(500, 0, 100, 500)
        panel.layout = null

        add(panel)

        reset()
    }

    /**
     * Abstract fns
     */
    abstract fun showVisualisation(hmPointToCount: Map<Point, Int>, model: DartsAiModel)
    abstract fun paintKey()
    protected abstract fun factoryColourWrapper(): ColourWrapper

    fun reset()
    {
        overlayImg = BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB)
        overlay.icon = ImageIcon(overlayImg)
        overlay.background = DartsColour.TRANSPARENT
    }

    fun populate(hmPointToCount: Map<Point, Int>, model: DartsAiModel)
    {
        showVisualisation(hmPointToCount, model)

        if (!paintedKey)
        {
            paintKey()
            paintedKey = true
        }

        isEnabled = true
    }
}