package burlton.dartzee.code.screen.stats.player;

import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.util.Debug;
import burlton.dartzee.code.stats.GameWrapper;
import burlton.desktopcore.code.util.ComponentUtilKt;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;

public abstract class AbstractStatisticsTab extends JPanel
											implements PropertyChangeListener
{
	protected HandyArrayList<GameWrapper> filteredGames = new HandyArrayList<>();
	protected HandyArrayList<GameWrapper> filteredGamesOther = new HandyArrayList<>();
	
	public AbstractStatisticsTab()
	{
		setPreferredSize(new Dimension(500, 150));
	}
	
	public abstract void populateStats();

	/**
	 * Helpers
	 */
	public HandyArrayList<String> getDistinctGameParams()
	{
		HashSet<String> ret = new HashSet<>();
		for (GameWrapper game : filteredGames)
		{
			String startValue = game.getGameParams();
			ret.add(startValue);
		}
		
		return new HandyArrayList<>(ret);
	}
	public boolean includeOtherComparison()
	{
		return !filteredGamesOther.isEmpty();
	}
	
	/**
	 * For the tabs that are a simple grid layout showing two tables.
	 */
	protected void setOtherComponentVisibility(Container container, Component otherComponent)
	{
		if (!(container.getLayout() instanceof GridLayout))
		{
			Debug.stackTrace("Calling method with inappropriate layout: " + getLayout());
			return;
		}
		
		if (!includeOtherComparison())
		{
			container.setLayout(new GridLayout(0, 1, 0, 0));
			container.remove(otherComponent);
		}
		else if (!ComponentUtilKt.containsComponent(container, otherComponent))
		{
			container.setLayout(new GridLayout(0, 2, 0, 0));
			container.add(otherComponent);
		}
		
		repaint();
	}
	
	/**
	 * Gets / sets
	 */
	public void setFilteredGames(HandyArrayList<GameWrapper> filteredGames, HandyArrayList<GameWrapper> filteredGamesOther)
	{
		this.filteredGames = filteredGames;
		this.filteredGamesOther = filteredGamesOther;
	}
	
	/**
	 * PropertyChangeListener
	 */
	@Override
	public void propertyChange(PropertyChangeEvent arg0)
	{
		String propertyName = arg0.getPropertyName();
		if (propertyName.equals("value"))
		{
			populateStats();
		}
	}
}
