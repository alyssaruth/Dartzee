package util;

import sun.misc.BASE64Encoder;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class Base64Desktop implements Base64Interface
{
	@Override
	public String encode(byte[] bytes)
	{
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(bytes);	
	}

	@Override
	public byte[] decode(byte[] bytes) throws Exception
	{
		return Base64.decode(bytes);
	}
	
	@Override
	public byte[] decode(String str) throws Exception
	{
		return Base64.decode(str);
	}
}
