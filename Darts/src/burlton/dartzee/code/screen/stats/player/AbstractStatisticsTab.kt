package burlton.dartzee.code.screen.stats.player

import burlton.desktopcore.code.util.Debug
import burlton.dartzee.code.stats.GameWrapper
import burlton.desktopcore.code.util.containsComponent
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.GridLayout
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.JPanel

abstract class AbstractStatisticsTab : JPanel(), PropertyChangeListener
{
    protected var filteredGames = listOf<GameWrapper>()
    protected var filteredGamesOther = listOf<GameWrapper>()

    init
    {
        preferredSize = Dimension(500, 150)
    }

    abstract fun populateStats()
    fun includeOtherComparison() = !filteredGamesOther.isEmpty()

    /**
     * For the tabs that are a simple grid layout showing two tables.
     */
    protected fun setOtherComponentVisibility(container: Container, otherComponent: Component)
    {
        if (container.layout !is GridLayout)
        {
            Debug.stackTrace("Calling method with inappropriate layout: $layout")
            return
        }

        if (!includeOtherComparison())
        {
            container.layout = GridLayout(0, 1, 0, 0)
            container.remove(otherComponent)
        }
        else if (!containsComponent(container, otherComponent))
        {
            container.layout = GridLayout(0, 2, 0, 0)
            container.add(otherComponent)
        }

        repaint()
    }

    /**
     * Helpers
     */
    fun getDistinctGameParams() = filteredGames.map{ it.gameParams }.distinct()

    /**
     * Gets / sets
     */
    fun setFilteredGames(filteredGames: List<GameWrapper>, filteredGamesOther: List<GameWrapper>)
    {
        this.filteredGames = filteredGames
        this.filteredGamesOther = filteredGamesOther
    }

    /**
     * PropertyChangeListener
     */
    override fun propertyChange(arg0: PropertyChangeEvent)
    {
        val propertyName = arg0.propertyName
        if (propertyName == "value")
        {
            populateStats()
        }
    }
}
