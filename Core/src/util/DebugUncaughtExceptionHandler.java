package util;

import java.lang.Thread.UncaughtExceptionHandler;

public class DebugUncaughtExceptionHandler implements UncaughtExceptionHandler
{
	@Override
	public void uncaughtException(Thread arg0, Throwable arg1)
	{
		Debug.append("UNCAUGHT EXCEPTION in thread " + arg0);
		
		if (isSuppressed(arg1))
		{
			//Still stack trace, but don't show an error message or email a log
			Debug.stackTraceSilently(arg1);
		}
		else
		{
			Debug.stackTrace(arg1);
		}
	}
	
	/**
	 * Some exceptions just can't be prevented, for example some Nimbus L&F exceptions that aren't caused by threading
	 * issues (I can see it's in the AWT thread)
	 */
	private boolean isSuppressed(Throwable t)
	{
		String message = t.getMessage();
		if (message == null)
		{
			return false;
		}
		
		if (message.equals("javax.swing.plaf.FontUIResource cannot be cast to javax.swing.Painter")
		  || message.equals("javax.swing.plaf.nimbus.DerivedColor$UIResource cannot be cast to javax.swing.Painter")
		  || message.equals("javax.swing.plaf.DimensionUIResource cannot be cast to java.awt.Color"))
		{
			return true;
		}
		
		return false;
	}
}
