package burlton.dartzee.code.object;

import java.awt.Color;

public class ColourWrapper 
{
	private Color evenSingleColour;
	private Color evenDoubleColour;
	private Color evenTrebleColour;
	private Color oddSingleColour;
	private Color oddDoubleColour;
	private Color oddTrebleColour;
	private Color innerBullColour;
	private Color outerBullColour;
	private Color outerDartboardColour = Color.black;
	private Color missedBoardColour = null;
	
	//For wireframe mode
	private Color edgeColour = null;

	public ColourWrapper(Color singleColour)
	{
		this.evenSingleColour = singleColour;
		this.evenDoubleColour = singleColour;
		this.evenTrebleColour = singleColour;
		this.oddSingleColour = singleColour;
		this.oddDoubleColour = singleColour;
		this.oddTrebleColour = singleColour;
		this.innerBullColour = singleColour;
		this.outerBullColour = singleColour;
		this.outerDartboardColour = singleColour;
	}
	public ColourWrapper(Color evenSingleColour, Color evenDoubleColour,
			Color evenTrebleColour, Color oddSingleColour,
			Color oddDoubleColour, Color oddTrebleColour,
			Color innerBullColour, Color outerBullColour) 
	{
		this.evenSingleColour = evenSingleColour;
		this.evenDoubleColour = evenDoubleColour;
		this.evenTrebleColour = evenTrebleColour;
		this.oddSingleColour = oddSingleColour;
		this.oddDoubleColour = oddDoubleColour;
		this.oddTrebleColour = oddTrebleColour;
		this.innerBullColour = innerBullColour;
		this.outerBullColour = outerBullColour;
	}
	
	/**
	 * Helpers
	 */
	public Color getBullColour(int multiplier)
	{
		if (multiplier == 1)
		{
			return outerBullColour;
		}
		
		return innerBullColour;
	}
	
	public Color getColour(int multiplier, boolean even)
	{
		if (multiplier == 1)
		{
			return getSingleColour(even);
		}
		else if (multiplier == 2)
		{
			return getDoubleColour(even);
		}
		
		return getTrebleColour(even);
	}
	
	private Color getSingleColour(boolean even)
	{
		return even? evenSingleColour:oddSingleColour;
	}
	private Color getDoubleColour(boolean even)
	{
		return even? evenDoubleColour:oddDoubleColour;
	}
	private Color getTrebleColour(boolean even)
	{
		return even? evenTrebleColour:oddTrebleColour;
	}

	public Color getOuterDartboardColour()
	{
		return outerDartboardColour;
	}

	public void setOuterDartboardColour(Color outerDartboardColour)
	{
		this.outerDartboardColour = outerDartboardColour;
	}

	public Color getMissedBoardColour()
	{
		return missedBoardColour;
	}

	public void setMissedBoardColour(Color missedBoardColour)
	{
		this.missedBoardColour = missedBoardColour;
	}
	public Color getEdgeColour()
	{
		return edgeColour;
	}
	public void setEdgeColour(Color edgeColour)
	{
		this.edgeColour = edgeColour;
	}
}
