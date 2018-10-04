package code.reporting;

/**
 * Lightweight wrapper object to represent a participant
 * Used in reporting
 */
public final class ParticipantWrapper
{
	private String playerName = null;
	private int finishingPosition = -1;
	
	public ParticipantWrapper(String playerName, int finishingPosition)
	{
		this.playerName = playerName;
		this.finishingPosition = finishingPosition;
	}
	
	@Override
	public String toString()
	{
		return playerName + " (" + getPositionDesc() + ")";
	}
	private String getPositionDesc()
	{
		if (finishingPosition == -1)
		{
			return "-";
		}
		
		return "" + finishingPosition;
	}
	
	public String getPlayerName()
	{
		return playerName;
	}
	public int getFinishingPosition()
	{
		return finishingPosition;
	}

}
