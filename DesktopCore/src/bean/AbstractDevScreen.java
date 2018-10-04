package bean;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.KeyStroke;

import util.Debug;

public abstract class AbstractDevScreen extends JFrame
{
	public AbstractDevScreen()
	{
		commandBar.setCheatListener(this);
	}
	
	protected final CheatBar commandBar = new CheatBar();
	
	/**
	 * Abstract methods
	 */
	public abstract boolean commandsEnabled();
	public abstract String processCommand(String cmd);
	
	/**
	 * Regular methods
	 */
	public void enableCheatBar(boolean enable)
	{
		commandBar.setEnabled(enable);
	}
	public KeyStroke getKeyStrokeForCommandBar()
	{
		return KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, InputEvent.CTRL_MASK);
	}
	public String processCommandWithTry(String cmd)
	{
		Debug.append("[Command Entered: " + cmd + "]", true);
		
		String result = "";
		try
		{
			result = processCommand(cmd);
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
		}
		
		return result;
	}
}
