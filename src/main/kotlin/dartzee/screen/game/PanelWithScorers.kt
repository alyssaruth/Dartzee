package dartzee.screen.game

import dartzee.core.util.ceilDiv
import dartzee.db.PlayerEntity
import dartzee.screen.game.scorer.AbstractScorer
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * Represents a panel that has scorers on it, centralising the logic for laying them out and assigning players to them etc.
 */
abstract class PanelWithScorers<S : AbstractScorer> : JPanel()
{
    private val innerPanel = JPanel()
    val panelEast = JPanel()
    val panelWest = JPanel()
    protected val panelCenter = JPanel()

    val scorersOrdered = mutableListOf<S>()

    init
    {
        layout = BorderLayout(0, 0)
        panelCenter.layout = BorderLayout(0, 0)

        add(panelCenter, BorderLayout.CENTER)
        add(panelEast, BorderLayout.EAST)
        add(panelWest, BorderLayout.WEST)
    }

    /**
     * Abstract methods
     */
    abstract fun factoryScorer(): S

    /**
     * Instance methods
     */
    fun initScorers(totalPlayers: Int)
    {
        scorersOrdered.clear()
        panelEast.removeAll()
        panelWest.removeAll()

        for (i in 0 until totalPlayers) { scorersOrdered.add(factoryScorer()) }

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
    }

    fun assignScorer(player: PlayerEntity): S
    {
        val scorer = scorersOrdered.find { it.canBeAssigned() } ?: throw Exception("Unable to assign scorer for player $player")
        scorer.init(player)
        return scorer
    }
}
