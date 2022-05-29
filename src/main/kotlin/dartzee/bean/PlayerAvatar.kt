package dartzee.bean

import dartzee.db.PlayerEntity
import dartzee.db.PlayerImageEntity
import dartzee.utils.InjectedThings.playerImageSelector
import dartzee.utils.ResourceCache
import java.awt.Cursor
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.border.EtchedBorder

class PlayerAvatar : JLabel(ResourceCache.AVATAR_UNSET)
{
    private var player: PlayerEntity? = null

    var avatarId = ""
    var readOnly = false

    init
    {
        preferredSize = Dimension(150, 150)
        border = EtchedBorder(EtchedBorder.RAISED, null, null)
        horizontalAlignment = SwingConstants.CENTER

        addMouseListener(AvatarClickListener())
    }

    fun init(player: PlayerEntity, saveChanges: Boolean)
    {
        //Only set the player variable if we want to allow the label to directly make changes to it.
        if (saveChanges)
        {
            this.player = player
        }

        avatarId = player.playerImageId
        icon = player.getAvatar() ?: ResourceCache.AVATAR_UNSET
    }

    /**
     * MouseListener
     */
    private inner class AvatarClickListener : MouseAdapter()
    {
        override fun mouseClicked(arg0: MouseEvent?)
        {
            if (readOnly)
            {
                return
            }

            val playerImageId = playerImageSelector.selectImage()
            if (playerImageId != null)
            {
                avatarId = playerImageId
                val newIcon = PlayerImageEntity.retrieveImageIconForId(avatarId)
                icon = newIcon

                player?.let {
                    it.playerImageId = avatarId
                    it.saveToDatabase()
                }
            }
        }

        override fun mouseEntered(arg0: MouseEvent?)
        {
            if (!readOnly)
            {
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            }
        }

        override fun mouseExited(arg0: MouseEvent?)
        {
            if (!readOnly)
            {
                cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
            }
        }
    }
}
