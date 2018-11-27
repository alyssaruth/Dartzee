package burlton.dartzee.code.bean;

import burlton.desktopcore.code.bean.RadioImage;
import burlton.dartzee.code.db.PlayerImageEntity;

/**
 * Wrap up a PlayerImage so we can render the icon, and store its ID to point a PlayerEntity at it
 */
public class PlayerImageRadio extends RadioImage
{
	private long playerImageId = -1;
	
	public PlayerImageRadio(PlayerImageEntity pi)
	{
		super(pi.getAsImageIcon());
		
		playerImageId = pi.getRowId();
	}
	
	
	public long getPlayerImageId()
	{
		return playerImageId;
	}
	public void setPlayerImageId(long playerImageId)
	{
		this.playerImageId = playerImageId;
	}
}
