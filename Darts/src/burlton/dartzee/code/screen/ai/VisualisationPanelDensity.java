package burlton.dartzee.code.screen.ai;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import burlton.dartzee.code.ai.AbstractDartsModel;
import burlton.dartzee.code.object.ColourWrapper;
import burlton.dartzee.code.utils.DartsColour;
import burlton.dartzee.code.utils.GeometryUtil;

public class VisualisationPanelDensity extends AbstractVisualisationPanel
{
	private static final int LABEL_WIDTH = 60;
	private static final int LABEL_HEIGHT = 30;
	
	private BufferedImage keyImg = null;
	
	public VisualisationPanelDensity()
	{
		super();
		
		panelKey.setBounds(0, 0, 100, 500);
		panel.add(panelKey);
		
		keyImg = new BufferedImage(panelKey.getWidth(), panelKey.getHeight(), BufferedImage.TYPE_INT_ARGB);
		panelKey.setIcon(new ImageIcon(keyImg));
	}
	
	private final JLabel panelKey = new JLabel();
	
	
	@Override
	public void showVisualisation(HashMap<Point, Integer> hmPointToCount, AbstractDartsModel model)
	{
		Point centerPt = model.getScoringPoint(dartboard);
		
		int width = overlay.getWidth();
		int height = overlay.getHeight();
		
		int[] pixels = new int[width * height];
		
		int i = 0;
		for (int y=0; y<height; y++)
		{
			for (int x=0; x<width; x++)
			{
		    	double radius = GeometryUtil.getDistance(new Point(x, y), centerPt);
		    	double probability = model.getProbabilityWithinRadius(radius);
		    	
		    	Color c = getColorForProbability(probability);
		    	pixels[i] = c.getRGB();
		    	i++;
			}
		}
		
		
		overlayImg.setRGB(0, 0, width, height, pixels, 0, width);
		repaint();
	}
	
	@Override
	public void paintKey()
	{
		int width = panelKey.getWidth();
		int height = panelKey.getHeight();
		
		int[] pixels = new int[width * height];
		
		//Label is 30 pixels wide. (100/2) - (50/2) = 25
		int lblXPosition = (panel.getWidth() / 2) - (LABEL_WIDTH/2);
		int i = 0;
		for (int y=0; y<height; y++)
		{
			for (int x=0; x<width; x++)
			{
		    	double probability = (float)y / (float)height;
		    	Color c = getColorForProbability(probability);
		    	
		    	pixels[i] = c.getRGB();
		    	i++;
		    	
		    	//Add the labels.
		    	if (x == lblXPosition
		    	  && (y % 50) == 0
		    	  && y > 0)
		    	{
		    		int probInt = 10 * y / 50;
		    		
		    		//Add a label
		    		JLabel label = new JLabel("-   " + probInt + "%   -");
		    		label.setBounds(lblXPosition, (y - (LABEL_HEIGHT/2)), LABEL_WIDTH, LABEL_HEIGHT);
		    		label.setHorizontalAlignment(SwingConstants.CENTER);
		    		panelKey.add(label);
		    	}
			}
		}
		
		keyImg.setRGB(0, 0, width, height, pixels, 0, width);
		repaint();
	}

	private Color getColorForProbability(double probability)
	{
		float hue = (float)(probability/1.2);
    	return Color.getHSBColor(hue, 1, 1);
	}
	
	@Override
	public ColourWrapper getColourWrapperForDartboard()
	{
		ColourWrapper wireframe = new ColourWrapper(DartsColour.TRANSPARENT);
		wireframe.setEdgeColour(Color.BLACK);
		return wireframe;
	}
	
	@Override
	public boolean dartboardOnTop()
	{
		//We overlay a wireframe dartboard over the shaded area
		return true;
	}

}
