package dartzee.bean

import dartzee.db.PlayerEntity
import dartzee.db.PlayerImageEntity
import dartzee.screen.PlayerImageDialog
import dartzee.utils.PLAYER_IMAGE_HEIGHT
import dartzee.utils.PLAYER_IMAGE_WIDTH
import dartzee.utils.ResourceCache
import java.awt.Cursor
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.border.EtchedBorder

class PlayerAvatar : JLabel(ResourceCache.AVATAR_UNSET) {
    private var player: PlayerEntity? = null

    var avatarId = ""
    var readOnly = false

    init {
        preferredSize = Dimension(PLAYER_IMAGE_WIDTH, PLAYER_IMAGE_HEIGHT)
        border = EtchedBorder(EtchedBorder.RAISED, null, null)
        horizontalAlignment = SwingConstants.CENTER

        addMouseListener(AvatarClickListener())
    }

    fun init(player: PlayerEntity, saveChanges: Boolean) {
        // Only set the player variable if we want to allow the label to directly make changes to
        // it.
        if (saveChanges) {
            this.player = player
        }

        avatarId = player.playerImageId
        icon =
            if (player.playerImageId.isNotEmpty()) player.getAvatar()
            else ResourceCache.AVATAR_UNSET
    }

    /** MouseListener */
    private inner class AvatarClickListener : MouseAdapter() {
        override fun mouseClicked(arg0: MouseEvent?) {
            if (readOnly) {
                return
            }

            val dlg = PlayerImageDialog(::imageSelected)
            dlg.isVisible = true
        }

        private fun imageSelected(imageId: String) {
            avatarId = imageId
            val newIcon = PlayerImageEntity.retrieveImageIconForId(avatarId)
            icon = newIcon

            player?.run {
                playerImageId = avatarId
                saveToDatabase()
            }
        }

        override fun mouseEntered(arg0: MouseEvent?) {
            if (!readOnly) {
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            }
        }

        override fun mouseExited(arg0: MouseEvent?) {
            if (!readOnly) {
                cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
            }
        }
    }
}
