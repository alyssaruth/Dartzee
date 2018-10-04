package code.screen.ai;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import code.ai.AbstractDartsModel;
import code.object.ColourWrapper;
import code.screen.Dartboard;
import code.utils.DartsColour;

public abstract class AbstractVisualisationPanel extends JPanel
{
	protected BufferedImage overlayImg = null;
	private boolean paintedKey = false;
	
	public AbstractVisualisationPanel()
	{
		setLayout(null);
		overlay.setBounds(0, 0, 500, 500);
		dartboard.setBounds(0, 0, 500, 500);
		panel.setBounds(500, 0, 100, 500);
		panel.setLayout(null);
		add(panel);
		
		
		//Add these in the right order
		if (dartboardOnTop())
		{
			add(dartboard);
			add(overlay);
		}
		else
		{
			add(overlay);
			add(dartboard);
		}
		
		
		init();
	}
	
	protected final Dartboard dartboard = new Dartboard(500, 500);
	protected final JLabel overlay = new JLabel();
	protected final JPanel panel = new JPanel();
	
	
	//Abstract methods
	public abstract void showVisualisation(HashMap<Point, Integer> hmPointToCount, AbstractDartsModel model);
	public abstract ColourWrapper getColourWrapperForDartboard();
	public abstract void paintKey();
	public abstract boolean dartboardOnTop();
	
	private void init()
	{
		ColourWrapper cw = getColourWrapperForDartboard();
		dartboard.paintDartboard(cw, false);
		
		reset();
	}
	
	public void reset()
	{
		overlayImg = dartboard.factoryOverlay();
		overlay.setIcon(new ImageIcon(overlayImg));
		overlay.setBackground(DartsColour.TRANSPARENT);
	}
	
	public void populate(HashMap<Point, Integer> hmPointToCount, AbstractDartsModel model)
	{
		showVisualisation(hmPointToCount, model);
		
		if (!paintedKey)
		{
			paintKey();
			paintedKey = true;
		}
		
		setEnabled(true);
	}
	
	/**
	 * Need to be able to access one of these to actually run the simulation.
	 */
	public Dartboard getDartboard()
	{
		return dartboard;
	}
}
