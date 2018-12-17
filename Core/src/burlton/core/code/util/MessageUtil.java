
package burlton.core.code.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.SecretKey;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MessageUtil implements OnlineConstants
{	
	public static final String LIVE_IP = "54.149.26.201"; //Live
	public static final String LAPTOP_IP = "82.3.206.177"; //Laptop (in Leeds)
	public static final String LOCAL_IP = "localhost";
	
	public static final String SERVER_IP = LIVE_IP;
	
	private static final Random RAND = new Random();
	
	public static int millisDelay = 0;
	public static PublicKey publicKey = null;
	public static SecretKey symmetricKey = null;
	public static SecretKey tempSymmetricKey = null; //Used to cache a key
	
	private static boolean shuttingDown = false;
	private static int cachedPortNumber = -1;
	
	public static void generatePublicKey()
	{
		Debug.append("Reading in public key...");
		
		try (InputStream in = MessageUtil.class.getResourceAsStream("/public.key");
		  ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in)))
		{
			BigInteger m = (BigInteger) oin.readObject();
			BigInteger e = (BigInteger) oin.readObject();
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			MessageUtil.publicKey = fact.generatePublic(keySpec);
			Debug.append("Key read successfully");
		} 
		catch (Throwable e) 
		{
			Debug.stackTrace(e, "Unable to read public key - won't be able to communicate with Server.");
		} 
	}
	
	public static void sendMessage(Document message, int millis)
	{
		sendMessage(message, millis, 5);
	}
	public static void sendMessage(Document message, int millis, int retries)
	{
		String messageString = XmlUtil.getStringFromDocument(message);
		sendMessage(messageString, millis, retries);
	}
	public static void sendMessage(String messageString, int millis)
	{
		sendMessage(messageString, millis, 5);
	}
	public static void sendMessage(String messageString, int millis, int retries)
	{
		MessageSenderParams params = new MessageSenderParams(messageString, millis, retries);
		sendMessage(params, true);
	}
	public static void sendMessage(MessageSenderParams message, boolean encrypt)
	{
		String messageString = message.getMessageString();
		String encryptedMessageString = messageString;
		if (encrypt)
		{
			encryptedMessageString = EncryptionUtil.encrypt(messageString, symmetricKey);
		}
		
		message.setEncryptedMessageString(encryptedMessageString);
		
		AbstractClient.getInstance().sendAsyncInSingleThread(message);
	}
	
	public static boolean serverIsShuttingDown()
	{
		return shuttingDown;
	}
	public static void setShuttingDown(boolean serverShuttingDown)
	{
		shuttingDown = serverShuttingDown;
	}
	
	public static int getRandomPortNumber()
	{
		synchronized (RAND)
		{
			if (cachedPortNumber != -1)
			{
				return cachedPortNumber;
			}
			
			int portsToChoose = SERVER_PORT_NUMBER_UPPER_BOUND - SERVER_PORT_NUMBER_LOWER_BOUND;
			cachedPortNumber = SERVER_PORT_NUMBER_LOWER_BOUND + RAND.nextInt(portsToChoose);
			return cachedPortNumber;
		}
	}
	
	public static boolean changeCachedPort(Element rootElement)
	{
		ArrayList<Integer> list = new ArrayList<>();
		
		NodeList usedPorts = rootElement.getElementsByTagName("UsedPort");
		int size = usedPorts.getLength();
		for (int i=0; i<size; i++)
		{
			Element portElement = (Element)usedPorts.item(i);
			int port = XmlUtil.getAttributeInt(portElement, "PortNumber");
			list.add(port);
		}
		
		int portsToChoose = SERVER_PORT_NUMBER_UPPER_BOUND - SERVER_PORT_NUMBER_LOWER_BOUND;
		if (list.size() >= portsToChoose)
		{
			cachedPortNumber = -1;
			return false;
		}
		
		Random rand = new Random();
		cachedPortNumber = SERVER_PORT_NUMBER_LOWER_BOUND + rand.nextInt(portsToChoose);
		while (list.contains(cachedPortNumber))
		{
			cachedPortNumber = SERVER_PORT_NUMBER_LOWER_BOUND + rand.nextInt(portsToChoose);
		}
		
		return true;
	}
	
	/*public static int getRandomLoadTestPortNumber()
	{
		Random rand = new Random();
		int portsToChoose = LOAD_TEST_PORT_NUMBER_UPPER_BOUND - LOAD_TEST_PORT_NUMBER_LOWER_BOUND;
		return LOAD_TEST_PORT_NUMBER_LOWER_BOUND + rand.nextInt(portsToChoose);
	}*/
	
	public static InetAddress factoryInetAddress(String ipAddress)
	{
		InetAddress address = null;
		
		try
		{
			address = InetAddress.getByName(ipAddress);
		}
		catch (UnknownHostException uhe)
		{
			Debug.stackTrace(uhe, "Failed to create InetAddress");
		}
		
		return address;
	}
	
	public static void stackTraceAndDumpMessages(Throwable t, Throwable clientStackTrace, String messageStr, 
	  String encryptedResponseStr)
	{
		AbstractClient.getInstance().finishServerCommunication();
		
		Debug.stackTraceSilently(t);
		Debug.stackTrace(clientStackTrace, "Previous stack:");
		Debug.append("messageString was: " + messageStr);
		
		String responseStr = null;
		if (symmetricKey != null)
		{
			responseStr = EncryptionUtil.decrypt(encryptedResponseStr, symmetricKey);
		}
		
		if (responseStr != null)
		{
			Debug.append("responseString: " + responseStr);
		}
		else
		{
			Debug.append("Unable to decrypt response");
		}
	}
}