package burlton.dartzee.code.bean;

import burlton.desktopcore.code.bean.RadioImage;
import burlton.dartzee.code.db.PlayerImageEntity;

/**
 * Wrap up a PlayerImage so we can render the icon, and store its ID to point a PlayerEntity at it
 */
public class PlayerImageRadio extends RadioImage
{
	private String playerImageId = "";
	
	public PlayerImageRadio(PlayerImageEntity pi)
	{
		super(pi.getAsImageIcon());
		
		playerImageId = pi.getRowId();
	}
	
	
	public String getPlayerImageId()
	{
		return playerImageId;
	}
}
