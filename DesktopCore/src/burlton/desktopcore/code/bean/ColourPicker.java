package burlton.desktopcore.code.bean;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

import burlton.desktopcore.code.screen.ColourChooserDialog;

public class ColourPicker extends JLabel
						  implements MouseListener
{
	//Static so that 'recent colours' get remembered across different ColourPicker beans
	private static final ColourChooserDialog dlg = new ColourChooserDialog();
	
	private ColourSelectionListener listener = null;
	private Color selectedColour = null;
	private BufferedImage img = null;
	
	public ColourPicker()
	{
		super();
		setBorder(new LineBorder(new Color(0, 0, 0)));
		setSize(30, 20);
		setOpaque(true);
		
		addMouseListener(this);
	}
	
	public void addColourSelectionListener(ColourSelectionListener listener)
	{
		this.listener = listener;
	}
	
	public Color getSelectedColor()
	{
		return selectedColour;
	}
	public void setSelectedColor(Color colour)
	{
		if (colour == null
		  || colour.equals(selectedColour))
		{
			return;
		}
		
		selectedColour = colour;
		
		int width = getWidth();
		int height = getHeight();
		
		if (img == null)
		{
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}
		
		for (int x=0; x<width; x++) 
		{
		    for (int y=0; y<height; y++) 
		    {
		    	img.setRGB(x, y, colour.getRGB());
		    }
		}
		
		setIcon(new ImageIcon(img));
		repaint();
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) 
	{
		dlg.setInitialColour(selectedColour);
		dlg.setLocationRelativeTo(null);
		dlg.setModal(true);
		dlg.setVisible(true);
		
		Color colour = dlg.getSelectedColour();
		setSelectedColor(colour);
		
		if (listener != null)
		{
			listener.colourSelected(colour);
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}
	@Override
	public void mousePressed(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent arg0) {}

}
