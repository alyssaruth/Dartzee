package burlton.dartzee.code.screen;

import burlton.dartzee.code.bean.PlayerAvatar;
import burlton.dartzee.code.db.PlayerEntity;
import burlton.desktopcore.code.screen.SimpleDialog;
import burlton.desktopcore.code.util.DialogUtil;

import javax.swing.*;

public abstract class AbstractPlayerCreationDialog extends SimpleDialog
{
	protected boolean createdPlayer = false;
	
	//Components
	protected final PlayerAvatar avatar = new PlayerAvatar();
	protected final JTextField textFieldName = new JTextField();
	
	//Abstract methods
	protected abstract void savePlayer();
	
	@Override
	public void okPressed()
	{
		if (valid())
		{
			savePlayer();
		}
	}
	
	/**
	 * Basic validation on the player name and avatar selections
	 */
	protected boolean valid()
	{
		String name = textFieldName.getText();
		if (!PlayerEntity.isValidName(name, doExistenceCheck()))
		{
			return false;
		}
		
		String avatarId = avatar.getAvatarId();
		if (avatarId.isEmpty())
		{
			DialogUtil.showError("You must select an avatar.");
			return false;
		}
		
		return true;
	}
	protected boolean doExistenceCheck()
	{
		return true;
	}
	
	public boolean getCreatedPlayer()
	{
		return createdPlayer;
	}
}
