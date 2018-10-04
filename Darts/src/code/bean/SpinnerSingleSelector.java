package code.bean;

import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SpinnerSingleSelector extends JSpinner
								   implements ChangeListener
{
	public SpinnerSingleSelector()
	{
		setSize(50, 25);
		setPreferredSize(new Dimension(50, 25));
		setModel(new SpinnerNumberModel(20, 1, 25, 1));
		addChangeListener(this);
	}
	
	/**
	 * This looks a bit weird, but what we want is:
	 * 
	 *  - Upping from 20 -> 25
	 *  - Downing from 25 -> 20
	 *  - Should not be able to enter 21-24 manually.
	 */
	@Override
	public void stateChanged(ChangeEvent arg0)
	{
		int val = (int)getValue();
		if (val == 21
		  || val == 22)
		{
			setValue(25);
		}
		else if (val > 22
		  && val < 25)
		{
			setValue(20);
		}
	}
}
