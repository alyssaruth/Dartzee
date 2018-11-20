package code.screen.game;

import java.awt.BorderLayout;
import java.beans.Beans;

import javax.swing.JPanel;

import code.db.PlayerEntity;
import object.HandyArrayList;
import object.SuperHashMap;
import util.Debug;

/**
 * Represents a panel that has scorers on it, centralising the logic for laying them out and assigning players to them etc.
 */
public abstract class PanelWithScorers<S extends AbstractScorer> extends JPanel
{
	
	public PanelWithScorers()
	{
		setLayout(new BorderLayout(0, 0));
		
		add(scorerEastOuter, BorderLayout.EAST);
		add(scorerWestOuter, BorderLayout.WEST);
		add(innerPanel, BorderLayout.CENTER);
		innerPanel.setLayout(new BorderLayout(0, 0));
		innerPanel.add(scorerEast, BorderLayout.EAST);
		innerPanel.add(scorerWest, BorderLayout.WEST);
		
		panelCenter.setLayout(new BorderLayout(0, 0));
		innerPanel.add(panelCenter, BorderLayout.CENTER);
	}
	
	private final JPanel innerPanel = new JPanel();
	protected final S scorerWest = factoryScorer();
	protected final S scorerEast = factoryScorer();
	protected final S scorerEastOuter = factoryScorer();
	protected final S scorerWestOuter = factoryScorer();
	protected final JPanel panelCenter = new JPanel();
	
	protected final HandyArrayList<S> scorersOrdered = HandyArrayList.factoryAdd(scorerWestOuter, scorerWest, scorerEast, scorerEastOuter);
	
	/**
	 * Abstract methods
	 */
	public S factoryScorer()
	{
		if (!Beans.isDesignTime())
		{
			Debug.stackTrace("Shouldn't be calling this directly.");
		}
		
		return (S)new DartsScorerX01();
	}
	
	/**
	 * Instance methods
	 */
	public void initScorers(int totalPlayers)
	{
		//Reset scorers and set their visibility
		scorerWestOuter.reset();
		scorerWest.reset();
		scorerEast.reset();
		scorerEastOuter.reset();
		
		scorerEastOuter.setVisible(totalPlayers > 2);
		scorerWestOuter.setVisible(totalPlayers > 3);
	}
	public <K> S assignScorer(PlayerEntity player, SuperHashMap<K, S> hmKeyToScorer, K key, String gameParams)
	{
		for (S scorer : scorersOrdered)
		{
			if (!scorer.canBeAssigned())
			{
				continue;
			}
			
			hmKeyToScorer.put(key, scorer);
			scorer.init(player, gameParams);
			
			//Don't know why this was ever here. It was always the leftmost player!
			/*if (scorer == getFirstVisibleScorer())
			{
				scorer.assignAsterisk();
			}*/
			
			return scorer;
		}
		
		Debug.stackTrace("Unable to assign scorer for player " + player + " and key " + key);
		return null;
	}
	/*private S getFirstVisibleScorer()
	{
		for (S scorer : scorersOrdered)
		{
			if (!scorer.isVisible())
			{
				continue;
			}
			
			return scorer;
		}
		
		Debug.stackTrace("No scorers visible - this shouldn't happen");
		return null;
	}*/
}
