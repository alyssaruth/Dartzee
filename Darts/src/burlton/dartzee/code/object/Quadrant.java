package burlton.dartzee.code.object;

/**
 * Represents a quadrant on a planar graph. 
 * NOTE: The sign of y is different because in Java, (0,0) is in the top left. This means that moving "up" 
 * actually DECREASES y. "yIsPositive" means y is positive IN JAVA.
 */
public class Quadrant
{
	private boolean xIsPositive = false;
	private boolean yIsPositive = false;
	private boolean sinForX = false;
	
	private int minimumAngle = 0;
	private int maximumAngle = 0;
	
	public static Quadrant factory(int minimumAngle, int maximumAngle, boolean xIsPositive, boolean yIsPositive)
	{
		Quadrant quadrant = new Quadrant();
		
		quadrant.setMinimumAngle(minimumAngle);
		quadrant.setMaximumAngle(maximumAngle);
		quadrant.setXIsPositive(xIsPositive);
		quadrant.setYIsPositive(yIsPositive);
		
		boolean sinForX = xIsPositive ^ yIsPositive;
		quadrant.setSinForX(sinForX);
		return quadrant;
	}
	
	
	public boolean getXIsPositive()
	{
		return xIsPositive;
	}
	public void setXIsPositive(boolean xIsPositive)
	{
		this.xIsPositive = xIsPositive;
	}
	public boolean getYIsPositive()
	{
		return yIsPositive;
	}
	public void setYIsPositive(boolean yIsPositive)
	{
		this.yIsPositive = yIsPositive;
	}
	public int getMinimumAngle()
	{
		return minimumAngle;
	}
	public void setMinimumAngle(int minimumAngle)
	{
		this.minimumAngle = minimumAngle;
	}
	public int getMaximumAngle()
	{
		return maximumAngle;
	}
	public void setMaximumAngle(int maximumAngle)
	{
		this.maximumAngle = maximumAngle;
	}
	public boolean isSinForX()
	{
		return sinForX;
	}
	public void setSinForX(boolean sinForX)
	{
		this.sinForX = sinForX;
	}
}
