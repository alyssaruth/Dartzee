package code.bean;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SpinnerX01 extends JSpinner
						implements ChangeListener
{
	private ActionListener listener = null;
	
	public SpinnerX01()
	{
		setModel(new SpinnerNumberModel(501, 101, 701, 100));
		addChangeListener(this);
	}
	
	@Override
	public void stateChanged(ChangeEvent arg0)
	{
		int value = (int)getValue();
		if (!(value % 100 == 1))
		{
			setValue(501);
		}	
		
		if (listener != null)
		{
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
		}
	}
	
	public void addActionListener(ActionListener listener)
	{
		this.listener = listener;
	}
}
