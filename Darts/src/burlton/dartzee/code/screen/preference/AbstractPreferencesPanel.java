package burlton.dartzee.code.screen.preference;

import javax.swing.*;

public abstract class AbstractPreferencesPanel extends JPanel
{
	/**
	 * Refresh this panel
	 * 
	 * @param useDefaults: If true, the panel will refresh using the default values of its preferences.
	 */
	public abstract void refresh(boolean useDefaults);
	
	/**
	 * Validate and save the data in this panel
	 */
	public abstract boolean valid();
	public abstract void save();
}
