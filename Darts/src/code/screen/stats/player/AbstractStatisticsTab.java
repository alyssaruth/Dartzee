package code.screen.stats.player;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;

import javax.swing.JPanel;

import code.stats.GameWrapper;
import object.HandyArrayList;
import util.ComponentUtil;
import util.Debug;

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
		else if (!ComponentUtil.containsComponent(container, otherComponent))
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
