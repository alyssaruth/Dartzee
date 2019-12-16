package burlton.dartzee.code.bean

import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.db.PlayerImageEntity
import burlton.dartzee.code.screen.PlayerImageDialog
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.border.EtchedBorder
import javax.swing.border.LineBorder

class PlayerAvatar : JLabel(AVATAR_UNSET)
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

    fun setSelected(selected: Boolean)
    {
        border = if (selected) LineBorder(Color.RED, 2) else EtchedBorder(EtchedBorder.RAISED, null, null)
    }

    fun init(player: PlayerEntity?, saveChanges: Boolean)
    {
        //Only set the player variable if we want to allow the label to directly make changes to it.
        if (saveChanges)
        {
            this.player = player
        }

        avatarId = player?.playerImageId ?: ""
        icon = player?.getAvatar() ?: AVATAR_UNSET
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

            val dlg = PlayerImageDialog()
            dlg.isVisible = true

            avatarId = dlg.playerImageIdSelected

            if (!avatarId.isEmpty())
            {
                val newIcon = PlayerImageEntity.retrieveImageIconForId(avatarId)
                icon = newIcon

                player?.let{
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

    companion object
    {
        private val AVATAR_UNSET = ImageIcon(PlayerAvatar::class.java.getResource("/avatars/Unset.png"))
    }
}
