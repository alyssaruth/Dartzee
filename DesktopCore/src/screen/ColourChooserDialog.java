package screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.colorchooser.DefaultColorSelectionModel;

import util.Debug;

public class ColourChooserDialog extends JDialog
								 implements ActionListener
{
	private Color initialColour = null;
	private Color selectedColour = null;
	
	public ColourChooserDialog() 
	{
		setTitle("Choose Colour");
		setSize(660, 450);
		getContentPane().add(colourChooser);
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.add(btnOk);
		panel.add(btnCancel);
		
		btnOk.addActionListener(this);
		btnCancel.addActionListener(this);
	}
	
	private final JColorChooser colourChooser = new JColorChooser(new DefaultColorSelectionModel());
	private final JButton btnOk = new JButton("Ok");
	private final JButton btnCancel = new JButton("Cancel");

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		Component source = (Component)e.getSource();
		if (source == btnOk)
		{
			selectedColour = colourChooser.getColor();
			dispose();
		}
		else if (source == btnCancel)
		{
			selectedColour = initialColour;
			dispose();
		}
		else
		{
			Debug.stackTrace("Unexpected actionPerformed");
		}
	}
	
	public Color getSelectedColour()
	{
		return selectedColour;
	}
	
	public void setInitialColour(Color initialColour)
	{
		this.initialColour = initialColour;
		colourChooser.setColor(initialColour);
	}
}
