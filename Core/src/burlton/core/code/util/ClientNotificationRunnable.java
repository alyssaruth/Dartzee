package burlton.core.code.util;

import org.w3c.dom.Document;

public class ClientNotificationRunnable implements Runnable
{
	private static final int SO_TIMEOUT_MILLIS = 120000; //2 minutes
	
	private String socketType = null;
	private AbstractClient client = null;
	
	public ClientNotificationRunnable(AbstractClient client, String socketType)
	{
		this.client = client;
		this.socketType = socketType;
	}
	
	@Override
	public void run()
	{
		Throwable clientStackTrace = new Throwable();
		String messageStr = null;
		String response = null;
		
		while (client.isOnline())
		{
			try
			{
				String username = client.getUsername();
				Document notificationXml = XmlUtil.factorySimpleMessage(username, socketType);
				messageStr = XmlUtil.getStringFromDocument(notificationXml);
				
				//Send encrypted, with a 1 minute timeout
				response = client.sendSync(notificationXml, true, SO_TIMEOUT_MILLIS, true);
				
				//If the thread has stopped due to a d/c, we'll get a null response.
				if (response != null)
				{
					client.handleResponse(messageStr, response);
				}
			}
			catch (Throwable t)
			{
				MessageUtil.stackTraceAndDumpMessages(t, clientStackTrace, messageStr, response);
			}
		}
	}

}
