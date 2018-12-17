package burlton.desktopcore.code.util;

import burlton.core.code.util.*;

import javax.swing.*;

public abstract class AbstractDesktopClient extends AbstractClient
											implements CoreRegistry
{
	@Override
	public void init()
	{
		EncryptionUtil.setBase64Interface(new Base64Desktop());
		MessageUtil.generatePublicKey();
		
		checkForUserName();
		
		ClientEmailer.tryToSendUnsentLogs();
		
		
	}
	
	private void checkForUserName()
	{
		String userName = instance.get(INSTANCE_STRING_USER_NAME, "");
		if (!userName.isEmpty())
		{
			return;
		}
		
		Debug.append("Username isn't specified - will prompt for one now");
		while (userName == null || userName.isEmpty())
		{
			userName = JOptionPane.showInputDialog(null, "Please enter your name (for debugging purposes).\nThis will only be asked for once.", "Enter your name");
		}
		
		instance.put(INSTANCE_STRING_USER_NAME, userName);
		
		try
		{
			ClientEmailer.sendClientEmail("Username notification", userName + " has set their username.", false);
		}
		catch (Throwable t)
		{
			//If there's no internet connection or Google does something dumb, just log a line
			Debug.append("Caught " + t + " trying to send username notification.");
		}
	}
}
