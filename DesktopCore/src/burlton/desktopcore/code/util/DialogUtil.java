package burlton.desktopcore.code.util;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import burlton.desktopcore.code.screen.LoadingDialog;
import burlton.core.code.util.Debug;

public class DialogUtil 
{
	private static LoadingDialog loadingDialog = new LoadingDialog();
	private static boolean shownConnectionLost = false;
	
	public static void showInfo(String infoText)
	{
		JOptionPane.showMessageDialog(null, infoText, "Information", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void showError(String errorText)
	{
		//Always do this before showing any errors etc. Otherwise the error can pop up behind and it all fucks up.
		DialogUtil.dismissLoadingDialog();
		
		JOptionPane.showMessageDialog(null, errorText, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public static void showInfoLater(final String infoText)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				showInfo(infoText);
			}
		});
	}
	
	public static void invokeInfoLaterAndWait(final String infoText)
	{
		try
		{
			SwingUtilities.invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					showInfo(infoText);
				}
			});
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t, "Failed to invokeAndWait for info message: " + infoText);
		}
	}
	
	public static void showErrorLater(final String errorText)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				showError(errorText);
			}
		});
	}
	
	public static int showQuestion(String message, boolean allowCancel)
	{
		int option = JOptionPane.YES_NO_OPTION;
		if (allowCancel)
		{
			option = JOptionPane.YES_NO_CANCEL_OPTION;
		}
		
		return JOptionPane.showConfirmDialog(null, message, "Question", option, JOptionPane.QUESTION_MESSAGE);
	}
	
	public static void showConnectionLost()
	{
		if (!shownConnectionLost)
		{
			showErrorLater("The connection has been lost - EntropyOnline will now exit.");
			shownConnectionLost = true;
		}
	}
	
	public static void showLoadingDialog(String text)
	{
		loadingDialog.showDialog(text);
	}
	public static void dismissLoadingDialog()
	{
		loadingDialog.dismissDialog();
	}
	
	public static void showDemoDialog()
	{
		showInfo("DEMO VERSION - This functionality isn't written yet.");
	}
}
