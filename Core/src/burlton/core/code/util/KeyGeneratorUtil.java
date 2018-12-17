package burlton.core.code.util;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class KeyGeneratorUtil 
{
	public static SecretKey generateSymmetricKey()
	{
		SecretKey symmetricKey = null;
		try
		{
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(128);
			symmetricKey = keyGen.generateKey();
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t, "Failed to generate symmetric key.");
		}
		
	    return symmetricKey;
	}
}
