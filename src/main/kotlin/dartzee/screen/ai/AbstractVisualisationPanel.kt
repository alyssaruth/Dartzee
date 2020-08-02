package dartzee.screen.ai

import dartzee.ai.DartsAiModelMk2
import dartzee.screen.Dartboard
import dartzee.utils.DartsColour
import java.awt.Point
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel

abstract class AbstractVisualisationPanel : JPanel()
{
    //Cached stuff
    private var paintedKey = false

    val dartboard = Dartboard(500, 500)
    var overlayImg = dartboard.factoryOverlay()

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
    abstract fun showVisualisation(hmPointToCount: Map<Point, Int>, model: DartsAiModelMk2)
    abstract fun paintKey()

    fun reset()
    {
        overlayImg = dartboard.factoryOverlay()
        overlay.icon = ImageIcon(overlayImg)
        overlay.background = DartsColour.TRANSPARENT
    }

    fun populate(hmPointToCount: Map<Point, Int>, model: DartsAiModelMk2)
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