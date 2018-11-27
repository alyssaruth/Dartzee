package burlton.desktopcore.code.screen;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import burlton.core.code.util.Debug;

public abstract class SimpleDialog extends JDialog
						  		   implements ActionListener
{
	public SimpleDialog() 
	{
		getContentPane().add(panelOkCancel, BorderLayout.SOUTH);
		
		panelOkCancel.add(btnOk);
		panelOkCancel.add(btnCancel);
		
		btnCancel.setVisible(allowCancel());
		
		btnOk.addActionListener(this);
		btnCancel.addActionListener(this);
	}
	
	protected final JPanel panelOkCancel = new JPanel();
	private final JButton btnOk = new JButton("Ok");
	private final JButton btnCancel = new JButton("Cancel");
	
	/**
	 * Abstract methods
	 */
	public abstract void okPressed();
	
	/**
	 * Default methods
	 */
	public boolean allowCancel()
	{
		return true;
	}
	public void cancelPressed()
	{
		dispose();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		JButton src = (JButton)arg0.getSource();
		if (src == btnOk)
		{
			okPressed();
		}
		else if (src == btnCancel)
		{
			cancelPressed();
		}
		else
		{
			Debug.stackTrace("Unexpected button pressed: " + src.getText());
		}
	}
}
