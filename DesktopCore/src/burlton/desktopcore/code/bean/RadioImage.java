package burlton.desktopcore.code.bean;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class RadioImage extends JPanel
						implements ChangeListener,
								   MouseListener
{
	public RadioImage(ImageIcon img)
	{
		setBorder(new EmptyBorder(1, 1, 1, 1));
		lblImg.setIcon(img);
		
		add(rdbtn);
		add(lblImg);
		
		rdbtn.addChangeListener(this);
		lblImg.addMouseListener(this);
	}
	
	private final JRadioButton rdbtn = new JRadioButton();
	private final JLabel lblImg = new JLabel();
	
	public void addToButtonGroup(ButtonGroup bg)
	{
		bg.add(rdbtn);
	}
	
	public boolean isSelected()
	{
		return rdbtn.isSelected();
	}

	@Override
	public void stateChanged(ChangeEvent arg0)
	{
		if (rdbtn.isSelected())
		{
			setBorder(new LineBorder(Color.BLACK));
		}
		else
		{
			setBorder(new EmptyBorder(1, 1, 1, 1));
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0)
	{
		rdbtn.setSelected(true);
	}
	@Override
	public void mouseEntered(MouseEvent arg0){}
	@Override
	public void mouseExited(MouseEvent arg0){}
	@Override
	public void mousePressed(MouseEvent arg0){}
	@Override
	public void mouseReleased(MouseEvent arg0){}
}
