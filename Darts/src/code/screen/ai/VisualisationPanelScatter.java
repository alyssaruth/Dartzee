package code.screen.ai;

import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import code.ai.AbstractDartsModel;
import code.object.ColourWrapper;
import code.utils.DartsColour;

public class VisualisationPanelScatter extends AbstractVisualisationPanel
{
	public VisualisationPanelScatter() 
	{
		
		JLabel label = new JLabel("20+");
		label.setBorder(new LineBorder(new Color(0, 0, 0)));
		label.setOpaque(true);
		label.setBackground(Color.YELLOW);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label.setBounds(16, 133, 64, 35);
		panel.add(label);
		
		JLabel label_1 = new JLabel("5 - 19");
		label_1.setOpaque(true);
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		label_1.setBackground(Color.ORANGE);
		label_1.setBounds(16, 233, 64, 35);
		panel.add(label_1);
		
		JLabel label_2 = new JLabel("1 - 4");
		label_2.setOpaque(true);
		label_2.setHorizontalAlignment(SwingConstants.CENTER);
		label_2.setFont(new Font("Tahoma", Font.PLAIN, 18));
		label_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		label_2.setBackground(Color.RED);
		label_2.setBounds(16, 333, 64, 35);
		panel.add(label_2);
	}
	
	
	@Override
	public ColourWrapper getColourWrapperForDartboard()
	{
		Color evenSingle = DartsColour.DARTBOARD_LIGHT_GREY;
		Color evenDouble = DartsColour.DARTBOARD_LIGHTER_GREY;
		Color evenTreble = DartsColour.DARTBOARD_LIGHTER_GREY;
		
		Color oddSingle = DartsColour.DARTBOARD_WHITE;
		Color oddDouble = DartsColour.DARTBOARD_LIGHTEST_GREY;
		Color oddTreble = DartsColour.DARTBOARD_LIGHTEST_GREY;
		
		ColourWrapper wrapper = new ColourWrapper(evenSingle, evenDouble, evenTreble,
				oddSingle, oddDouble, oddTreble, evenDouble, oddDouble);
		
		wrapper.setMissedBoardColour(Color.WHITE);
		wrapper.setOuterDartboardColour(Color.WHITE);
		
		return wrapper;
	}
	
	@Override
	public void showVisualisation(HashMap<Point, Integer> hmPointToCount, AbstractDartsModel model)
	{
		int width = overlayImg.getWidth();
		int height = overlayImg.getHeight();
		int[] pixels = new int[width * height];
		
		int i = 0;
		for (int y=0; y<height; y++)
		{
			for (int x=0; x<width; x++)
			{
				Point pt = new Point(x, y);
				Color colorToUse = DartsColour.TRANSPARENT;
				if (hmPointToCount.containsKey(pt))
				{
					int countInt = hmPointToCount.get(pt);
					colorToUse = getColourForNoOfHits(countInt);
				}
				
		    	pixels[i] = colorToUse.getRGB();
		    	i++;
			}
		}
		
		overlayImg.setRGB(0, 0, width, height, pixels, 0, width);
		
		repaint();
	}
	private Color getColourForNoOfHits(int count)
	{
		if (count >= 20)
		{
			return Color.yellow;
		}
		else if (count >= 5)
		{
			return Color.orange;
		}
		else
		{
			return Color.red;
		}
	}
	
	@Override
	public void paintKey()
	{
		//Do nothing
	}
	
	@Override
	public boolean dartboardOnTop()
	{
		return false;
	}
}
