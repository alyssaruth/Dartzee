package burlton.dartzee.code.screen.game

import burlton.core.code.util.Debug
import burlton.dartzee.code.db.PlayerEntity
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * Represents a panel that has scorers on it, centralising the logic for laying them out and assigning players to them etc.
 */
abstract class PanelWithScorers<S : AbstractScorer> : JPanel()
{
    private val innerPanel = JPanel()
    protected val scorerWest = factoryScorer()
    protected val scorerEast = factoryScorer()
    protected val scorerEastOuter = factoryScorer()
    protected val scorerWestOuter = factoryScorer()
    @JvmField protected val panelCenter = JPanel()

    @JvmField protected val scorersOrdered = mutableListOf(scorerWestOuter, scorerWest, scorerEast, scorerEastOuter)

    init
    {
        layout = BorderLayout(0, 0)

        add(scorerEastOuter, BorderLayout.EAST)
        add(scorerWestOuter, BorderLayout.WEST)
        add(innerPanel, BorderLayout.CENTER)
        innerPanel.layout = BorderLayout(0, 0)
        innerPanel.add(scorerEast, BorderLayout.EAST)
        innerPanel.add(scorerWest, BorderLayout.WEST)

        panelCenter.layout = BorderLayout(0, 0)
        innerPanel.add(panelCenter, BorderLayout.CENTER)
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
        //Reset scorers and set their visibility
        scorerWestOuter.reset()
        scorerWest.reset()
        scorerEast.reset()
        scorerEastOuter.reset()

        scorerWest.isVisible = totalPlayers > 1
        scorerEastOuter.isVisible = totalPlayers > 2
        scorerWestOuter.isVisible = totalPlayers > 3
    }

    fun <K> assignScorer(player: PlayerEntity, hmKeyToScorer: MutableMap<K, S>, key: K, gameParams: String): S?
    {
        scorersOrdered.forEach {
            if (it.canBeAssigned())
            {
                hmKeyToScorer[key] = it
                it.init(player, gameParams)
                return it
            }
        }

        Debug.stackTrace("Unable to assign scorer for player $player and key $key")
        return null
    }
}
