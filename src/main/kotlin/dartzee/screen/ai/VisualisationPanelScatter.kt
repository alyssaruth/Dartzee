package dartzee.screen.ai

import dartzee.ai.DartsAiModel
import dartzee.core.bean.paint
import dartzee.`object`.ColourWrapper
import dartzee.utils.DartsColour
import dartzee.utils.ResourceCache
import java.awt.Color
import java.awt.Font
import java.awt.Point
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.border.LineBorder

class VisualisationPanelScatter : AbstractVisualisationPanel()
{
    init
    {
        add(overlay)
        add(dartboard)

        val label = JLabel("20+")
        label.border = LineBorder(Color(0, 0, 0))
        label.isOpaque = true
        label.background = Color.YELLOW
        label.horizontalAlignment = SwingConstants.CENTER
        label.font = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 18f)
        label.setBounds(16, 133, 64, 35)
        panel.add(label)
        val label_1 = JLabel("5 - 19")
        label_1.isOpaque = true
        label_1.horizontalAlignment = SwingConstants.CENTER
        label_1.font = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 18f)
        label_1.border = LineBorder(Color(0, 0, 0))
        label_1.background = Color.ORANGE
        label_1.setBounds(16, 233, 64, 35)
        panel.add(label_1)
        val label_2 = JLabel("1 - 4")
        label_2.isOpaque = true
        label_2.horizontalAlignment = SwingConstants.CENTER
        label_2.font = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 18f)
        label_2.border = LineBorder(Color(0, 0, 0))
        label_2.background = Color.RED
        label_2.setBounds(16, 333, 64, 35)
        panel.add(label_2)
    }

    override fun factoryColourWrapper(): ColourWrapper
    {
        val evenSingle = DartsColour.DARTBOARD_LIGHT_GREY
        val evenDouble = DartsColour.DARTBOARD_LIGHTER_GREY
        val evenTreble = DartsColour.DARTBOARD_LIGHTER_GREY
        val oddSingle = DartsColour.DARTBOARD_WHITE
        val oddDouble = DartsColour.DARTBOARD_LIGHTEST_GREY
        val oddTreble = DartsColour.DARTBOARD_LIGHTEST_GREY
        val wrapper = ColourWrapper(
            evenSingle, evenDouble, evenTreble,
            oddSingle, oddDouble, oddTreble, evenDouble, oddDouble
        )
        wrapper.missedBoardColour = Color.WHITE
        wrapper.outerDartboardColour = Color.WHITE
        return wrapper
    }

    override fun showVisualisation(hmPointToCount: Map<Point, Int>, model: DartsAiModel)
    {
        overlayImg.paint { getColorForPoint(it, hmPointToCount) }
        repaint()
    }
    private fun getColorForPoint(pt: Point, hmPointToCount: Map<Point, Int>): Color
    {
        val count = hmPointToCount[pt] ?: return DartsColour.TRANSPARENT
        return getColourForNoOfHits(count)
    }

    private fun getColourForNoOfHits(count: Int): Color
    {
        return when
        {
            count >= 20 -> Color.yellow
            count >= 5 -> Color.orange
            else -> Color.red
        }
    }

    override fun paintKey() {}
}