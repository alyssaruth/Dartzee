package code.utils;

import util.AbstractDesktopClient;
import util.Debug;
import util.MessageSender;
import util.MessageSenderParams;
import util.UpdateChecker;

public class DesktopDartsClient extends AbstractDesktopClient
{
	@Override
	public String getUsername()
	{
		Debug.stackTrace("Invalid method");
		return null;
	}

	@Override
	public boolean isOnline()
	{
		Debug.append("Calling isOnline() for Dartzee - this is odd, but possible if retrying CRC check.");
		return true;
	}

	@Override
	public void sendAsyncInSingleThread(MessageSenderParams message)
	{
		Debug.stackTrace("Invalid method");
	}

	@Override
	public String sendSyncOnDevice(MessageSender runnable)
	{
		return runnable.sendMessage();
	}

	@Override
	public void handleResponse(String message, String encryptedResponse)
			throws Throwable
	{
		Debug.stackTrace("Invalid method");
	}

	@Override
	public void checkForUpdates()
	{
		UpdateChecker.checkForUpdates(FILE_NAME_DARTS, SERVER_PORT_NUMBER_DOWNLOAD_DARTS);
	}

	@Override
	public boolean isCommunicatingWithServer()
	{
		return false;
	}

	@Override
	public void finishServerCommunication()
	{
		
	}

	@Override
	public void unableToConnect()
	{
		
	}

	@Override
	public void connectionLost()
	{
		
	}

	@Override
	public void goOffline()
	{
		
	}

}
