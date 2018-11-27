package burlton.core.code.util;

public interface Base64Interface
{
	public String encode(byte[] bytes);
	public byte[] decode(byte[] bytes) throws Exception;
	public byte[] decode(String str) throws Exception;
}
