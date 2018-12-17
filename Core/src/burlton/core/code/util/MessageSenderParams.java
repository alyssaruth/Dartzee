package burlton.core.code.util;

import org.w3c.dom.Document;

public class MessageSenderParams
{
	public static final int SO_TIMEOUT_MILLIS = 20000;
	
	private String messageString = null;
	private String encryptedMessageString = null;
	private int millis = 0;
	private int retries = 0;
	private boolean expectResponse = true;
	private boolean ignoreResponse = false;
	private boolean alwaysRetryOnSoTimeout = false;
	private int readTimeOut = SO_TIMEOUT_MILLIS;
	private Throwable creationStack = new Throwable();
	
	public MessageSenderParams(String messageString, int millis, int retries)
	{
		this.messageString = messageString;
		this.millis = millis;
		this.retries = retries;
	}

	public String getMessageString()
	{
		return messageString;
	}
	public void setMessageString(String messageString)
	{
		this.messageString = messageString;
	}
	public String getEncryptedMessageString()
	{
		return encryptedMessageString;
	}
	public void setEncryptedMessageString(String encryptedMessageString)
	{
		this.encryptedMessageString = encryptedMessageString;
	}
	public int getMillis()
	{
		return millis;
	}
	public void setMillis(int millis)
	{
		this.millis = millis;
	}
	public int getRetries()
	{
		return retries;
	}
	public void setRetries(int retries)
	{
		this.retries = retries;
	}
	public boolean getExpectResponse()
	{
		return expectResponse;
	}
	public void setExpectResponse(boolean expectResponse)
	{
		this.expectResponse = expectResponse;
	}
	public boolean getIgnoreResponse()
	{
		return ignoreResponse;
	}
	public void setIgnoreResponse(boolean ignoreResponse)
	{
		this.ignoreResponse = ignoreResponse;
	}
	public int getReadTimeOut()
	{
		return readTimeOut;
	}
	public void setReadTimeOut(int readTimeOut)
	{
		this.readTimeOut = readTimeOut;
	}
	public boolean getAlwaysRetryOnSoTimeout()
	{
		return alwaysRetryOnSoTimeout;
	}
	public void setAlwaysRetryOnSoTimeout(boolean alwaysRetryOnSoTimeout)
	{
		this.alwaysRetryOnSoTimeout = alwaysRetryOnSoTimeout;
	}
	
	public Throwable getCreationStack()
	{
		return creationStack;
	}
	
	/**
	 * Helper method
	 */
	public String getMessageName()
	{
		Document xml = XmlUtil.getDocumentFromXmlString(messageString);
		if (xml != null)
		{
			return xml.getDocumentElement().getNodeName();
		}
		
		return "";
	}
}
