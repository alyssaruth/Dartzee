package dartzee.bean

import dartzee.game.state.IWrappedParticipant
import dartzee.utils.PLAYER_IMAGE_HEIGHT
import dartzee.utils.PLAYER_IMAGE_WIDTH
import dartzee.utils.ResourceCache
import java.awt.Color
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.border.EtchedBorder
import javax.swing.border.LineBorder

class ParticipantAvatar(private val pt: IWrappedParticipant) : JLabel(ResourceCache.AVATAR_UNSET)
{
    init
    {
        size = Dimension(PLAYER_IMAGE_WIDTH, PLAYER_IMAGE_HEIGHT)
        border = EtchedBorder(EtchedBorder.RAISED, null, null)
        horizontalAlignment = SwingConstants.CENTER
        icon = pt.getAvatar(1, false)
    }

    fun setSelected(selected: Boolean, roundNumber: Int)
    {
        border = if (selected) LineBorder(Color.BLACK, 2) else EtchedBorder(EtchedBorder.RAISED, null, null)
        icon = pt.getAvatar(roundNumber, selected)
    }
}