package dartzee.screen.game

import dartzee.core.util.ceilDiv
import dartzee.game.state.IWrappedParticipant
import dartzee.screen.game.scorer.AbstractScorer
import java.awt.BorderLayout
import javax.swing.JPanel
import net.miginfocom.swing.MigLayout

/**
 * Represents a panel that has scorers on it, centralising the logic for laying them out and
 * assigning players to them etc.
 */
abstract class PanelWithScorers<S : AbstractScorer> : JPanel() {
    val panelEast = JPanel()
    val panelWest = JPanel()
    protected val panelCenter = JPanel()

    val scorersOrdered = mutableListOf<S>()

    init {
        layout = BorderLayout(0, 0)
        panelCenter.layout = BorderLayout(0, 0)

        add(panelCenter, BorderLayout.CENTER)
        add(panelEast, BorderLayout.EAST)
        add(panelWest, BorderLayout.WEST)
    }

    /** Abstract methods */
    protected abstract fun factoryScorer(participant: IWrappedParticipant): S

    fun finaliseScorers(parentWindow: AbstractDartsGameScreen) {
        panelEast.removeAll()
        panelWest.removeAll()

        val chunkSize = scorersOrdered.size.ceilDiv(2)
        val eastAndWestScorers = scorersOrdered.chunked(chunkSize)
        val westScorers = eastAndWestScorers[0]
        val eastScorers = eastAndWestScorers.getOrNull(1)

        panelEast.border = null
        panelWest.border = null
        panelEast.layout = MigLayout("insets 0, gapx 0", "[]", "[grow]")
        panelWest.layout = MigLayout("insets 0, gapx 0", "[]", "[grow]")

        eastScorers?.forEach { panelEast.add(it, "growy") }
        westScorers.forEach { panelWest.add(it, "growy") }

        parentWindow.packIfNecessary()
    }

    fun assignScorer(participant: IWrappedParticipant): S {
        val scorer = factoryScorer(participant)
        scorer.init()
        scorersOrdered.add(scorer)
        return scorer
    }
}
