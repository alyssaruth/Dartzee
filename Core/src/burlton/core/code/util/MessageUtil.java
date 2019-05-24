
package burlton.core.code.util;

import javax.crypto.SecretKey;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Random;

public class MessageUtil implements OnlineConstants
{
	public static final String SERVER_IP = "54.149.26.201"; //Live
	
	private static final Random RAND = new Random();
	
	public static int millisDelay = 0;
	public static PublicKey publicKey = null;
	public static SecretKey symmetricKey = null;

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