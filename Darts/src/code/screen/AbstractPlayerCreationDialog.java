package code.screen;

import javax.swing.JTextField;

import code.bean.PlayerAvatar;
import code.db.PlayerEntity;
import screen.SimpleDialog;
import util.DialogUtil;

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
		
		long avatarId = avatar.getAvatarId();
		if (avatarId == -1)
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
