package burlton.dartzee.code.screen.game

import burlton.core.code.util.ceilDiv
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.game.scorer.AbstractScorer
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JPanel

/**
 * Represents a panel that has scorers on it, centralising the logic for laying them out and assigning players to them etc.
 */
abstract class PanelWithScorers<S : AbstractScorer> : JPanel()
{
    private val innerPanel = JPanel()
    val panelEast = JPanel()
    val panelWest = JPanel()

    @JvmField protected val panelCenter = JPanel()

    @JvmField protected val scorersOrdered = mutableListOf<S>()

    init
    {
        layout = BorderLayout(0, 0)
        panelCenter.layout = BorderLayout(0, 0)

        panelEast.layout = makeFlowLayout()
        panelWest.layout = makeFlowLayout()

        add(panelCenter, BorderLayout.CENTER)
        add(panelEast, BorderLayout.EAST)
        add(panelWest, BorderLayout.WEST)
    }

    private fun makeFlowLayout() = FlowLayout().also { it.hgap = 0 }

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
        val eastScorers = eastAndWestScorers[1]
        val westScorers = eastAndWestScorers[0]

        eastScorers.forEach { panelEast.add(it) }
        westScorers.forEach { panelWest.add(it) }
    }

    fun <K> assignScorer(player: PlayerEntity, hmKeyToScorer: MutableMap<K, S>, key: K, gameParams: String): S
    {
        val scorer = scorersOrdered.find { it.canBeAssigned() } ?: throw Exception("Unable to assign scorer for player $player and key $key")

        hmKeyToScorer[key] = scorer
        scorer.init(player, gameParams)
        return scorer
    }
}
