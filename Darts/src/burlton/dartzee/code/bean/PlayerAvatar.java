package burlton.dartzee.code.bean;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import burlton.dartzee.code.screen.PlayerImageDialog;
import burlton.dartzee.code.screen.ScreenCache;
import burlton.dartzee.code.db.PlayerEntity;
import burlton.dartzee.code.db.PlayerImageEntity;

public class PlayerAvatar extends JLabel
{
	private static final ImageIcon AVATAR_UNSET = new ImageIcon(PlayerAvatar.class.getResource("/avatars/Unset.png"));
	
	private PlayerEntity player = null;
	private String avatarId = "";
	private boolean readOnly = false;
	
	public PlayerAvatar()
	{
		super(AVATAR_UNSET);
		setPreferredSize(new Dimension(150, 150));
		setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		setHorizontalAlignment(SwingConstants.CENTER);
		
		addMouseListener(new AvatarClickListener());
	}
	
	public void init(PlayerEntity player, boolean saveChanges)
	{
		//Only set the player variable if we want to allow the label to directly make changes to it.
		if (saveChanges)
		{
			this.player = player;
		}
		
		if (player != null)
		{
			this.avatarId = player.getPlayerImageId();
			
			ImageIcon newIcon = player.getAvatar();
			setIcon(newIcon);
		}
		else
		{
			avatarId = "";
			setIcon(AVATAR_UNSET);
		}
	}
	
	/**
	 * Gets / Sets
	 */
	public String getAvatarId()
	{
		return avatarId;
	}
	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
	}
	
	/**
	 * MouseListener
	 */
	private final class AvatarClickListener extends MouseAdapter
	{
		@Override
		public void mouseClicked(MouseEvent arg0)
		{
			if (readOnly)
			{
				return;
			}
			
			PlayerImageDialog dlg = ScreenCache.getPlayerImageDialog();
			dlg.reset();
			dlg.setVisible(true);
			
			avatarId = dlg.getPlayerImageIdSelected();
			if (!avatarId.isEmpty())
			{
				ImageIcon newIcon = PlayerImageEntity.retrieveImageIconForId(avatarId);
				setIcon(newIcon);
				
				if (player != null)
				{
					player.setPlayerImageId(avatarId);
					player.saveToDatabase();
				}
			}
		}
		
		@Override
		public void mouseEntered(MouseEvent arg0)
		{
			if (!readOnly)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
		}
		
		@Override
		public void mouseExited(MouseEvent arg0)
		{
			if (!readOnly)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}
}
