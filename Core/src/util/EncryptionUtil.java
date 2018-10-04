package util;

import java.security.Key;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtil 
{
	//public static final String DEV_MODE_SYMMETRIC_KEY_STR = "nISBSiWGpVInzurAhSc/Kg==";
	public static final int MINIMUM_PASSWORD_LENGTH = 7;
	
	private static final String ALGORITHM_RSA_ECB_PKCS1PADDING = "RSA/ECB/PKCS1Padding";
	private static final String ALGORITHM_AES_ECB_PKCS5PADDING = "AES/ECB/PKCS5Padding";
	
	public static boolean failedDecryptionLogging = false;
	public static Base64Interface base64Interface = null;
	
	/*public static SecretKey getDevModeKey()
	{
		return reconstructKeyFromString(DEV_MODE_SYMMETRIC_KEY_STR);
	}*/
	
	public static String convertSecretKeyToString(SecretKey secretKey)
	{
		byte[] keyBytes = secretKey.getEncoded();
		return base64Interface.encode(keyBytes);
	}
	
	public static SecretKey reconstructKeyFromString(String keyStr)
	{
		SecretKey secretKey = null;
		
		try
		{
			byte[] encodedKey = base64Interface.decode(keyStr.getBytes());
	    	secretKey = new SecretKeySpec(encodedKey, "AES");
		}
		catch (Throwable t)
		{
			Debug.append("Caught " + t + " trying to reconstruct secret key from String");
		}
		
		return secretKey;
	}
	
	public static String encrypt(String messageString, Key key)
	{
		return encrypt(messageString, key, false);
	}
	public static String encrypt(String messageString, Key key, boolean asymmetric)
	{
		String encryptedString = null;
		try
		{
			byte[] messageBytes = messageString.getBytes();
			String algorithm = ALGORITHM_AES_ECB_PKCS5PADDING;
			if (asymmetric)
			{
				algorithm = ALGORITHM_RSA_ECB_PKCS1PADDING;
			}
			
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] cipherData = cipher.doFinal(messageBytes);
			encryptedString = base64Interface.encode(cipherData);
			
			//Strip out any newline characters
			encryptedString = encryptedString.replaceAll("\n", "");
			encryptedString = encryptedString.replaceAll("\r", "");
		}
		catch (Throwable t)
		{
			Debug.append("Caught " + t + " trying to encrypt message: " + messageString);
		}
		
		if (encryptedString != null)
		{
			encryptedString = encryptedString.intern();
			return encryptedString.intern();
		}
		
		return encryptedString;
	}
	
	public static String decryptIfPossible(String encryptedMessage, Key key)
	{
		if (key == null)
		{
			return encryptedMessage;
		}
		
		return decrypt(encryptedMessage, key);
	}
	public static String decrypt(String encryptedMessage, Key key)
	{
		return decrypt(encryptedMessage, key, false);
	}
	public static String decrypt(String encryptedMessage, Key key, boolean asymmetric)
	{
		String messageString = null;
		try
		{
			byte[] cipherData = base64Interface.decode(encryptedMessage);
			String algorithm = ALGORITHM_AES_ECB_PKCS5PADDING;
			if (asymmetric)
			{
				algorithm = ALGORITHM_RSA_ECB_PKCS1PADDING;
			}
			
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] messageBytes = cipher.doFinal(cipherData);
			messageString = new String(messageBytes);
		}
		catch (Throwable t)
		{
			Debug.append("Caught " + t + " trying to decrypt message: " + encryptedMessage, failedDecryptionLogging);
		}
		
		if (messageString != null)
		{
			messageString = messageString.intern();
			return messageString.intern();
		}
		
		return messageString;
	}
	
	public static String sha256Hash(String input)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(input.getBytes("UTF-8"));
			byte[] digest = md.digest();
			
			return EncryptionUtil.base64Interface.encode(digest);
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
			return "";
		}
	}
	
	public static void setBase64Interface(Base64Interface base64Interface)
	{
		EncryptionUtil.base64Interface = base64Interface;
	}
}
