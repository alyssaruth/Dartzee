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

class VisualisationPanelScatter : AbstractVisualisationPanel() {
    init {
        add(overlay)
        add(dartboard)

        val lblYellow = JLabel("20+")
        lblYellow.border = LineBorder(Color(0, 0, 0))
        lblYellow.isOpaque = true
        lblYellow.background = Color.YELLOW
        lblYellow.horizontalAlignment = SwingConstants.CENTER
        lblYellow.font = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 18f)
        lblYellow.setBounds(16, 133, 64, 35)
        panel.add(lblYellow)
        val lblOrange = JLabel("5 - 19")
        lblOrange.isOpaque = true
        lblOrange.horizontalAlignment = SwingConstants.CENTER
        lblOrange.font = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 18f)
        lblOrange.border = LineBorder(Color(0, 0, 0))
        lblOrange.background = Color.ORANGE
        lblOrange.setBounds(16, 233, 64, 35)
        panel.add(lblOrange)
        val lblRed = JLabel("1 - 4")
        lblRed.isOpaque = true
        lblRed.horizontalAlignment = SwingConstants.CENTER
        lblRed.font = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 18f)
        lblRed.border = LineBorder(Color(0, 0, 0))
        lblRed.background = Color.RED
        lblRed.setBounds(16, 333, 64, 35)
        panel.add(lblRed)
    }

    override fun factoryColourWrapper(): ColourWrapper {
        val evenSingle = DartsColour.DARTBOARD_LIGHT_GREY
        val evenDouble = DartsColour.DARTBOARD_LIGHTER_GREY
        val evenTreble = DartsColour.DARTBOARD_LIGHTER_GREY
        val oddSingle = DartsColour.DARTBOARD_WHITE
        val oddDouble = DartsColour.DARTBOARD_LIGHTEST_GREY
        val oddTreble = DartsColour.DARTBOARD_LIGHTEST_GREY
        val wrapper =
            ColourWrapper(
                evenSingle,
                evenDouble,
                evenTreble,
                oddSingle,
                oddDouble,
                oddTreble,
                evenDouble,
                oddDouble,
            )
        wrapper.missedBoardColour = Color.WHITE
        wrapper.outerDartboardColour = Color.WHITE
        return wrapper
    }

    override fun showVisualisation(hmPointToCount: Map<Point, Int>, model: DartsAiModel) {
        overlayImg.paint { getColorForPoint(it, hmPointToCount) }
        repaint()
    }

    private fun getColorForPoint(pt: Point, hmPointToCount: Map<Point, Int>): Color {
        val count = hmPointToCount[pt] ?: return DartsColour.TRANSPARENT
        return getColourForNoOfHits(count)
    }

    private fun getColourForNoOfHits(count: Int): Color {
        return when {
            count >= 20 -> Color.yellow
            count >= 5 -> Color.orange
            else -> Color.red
        }
    }

    override fun paintKey() {}
}
