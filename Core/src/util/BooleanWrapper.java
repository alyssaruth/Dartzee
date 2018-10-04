package util;

public class BooleanWrapper
{
	private boolean b = false;
	
	public BooleanWrapper(boolean b)
	{
		this.b = b;
	}
	
	public boolean getValue()
	{
		return b;
	}
	public void setValue(boolean b)
	{
		this.b = b;
	}
}