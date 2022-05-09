package dartzee.bean

import dartzee.game.state.IWrappedParticipant
import dartzee.utils.ResourceCache
import java.awt.Color
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.border.EtchedBorder
import javax.swing.border.LineBorder

class ParticipantAvatar(pt: IWrappedParticipant) : JLabel(ResourceCache.AVATAR_UNSET)
{
    init
    {
        preferredSize = Dimension(150, 150)
        border = EtchedBorder(EtchedBorder.RAISED, null, null)
        horizontalAlignment = SwingConstants.CENTER
        icon = pt.getCombinedAvatar()
    }

    fun setSelected(selected: Boolean)
    {
        border = if (selected) LineBorder(Color.RED, 2) else EtchedBorder(EtchedBorder.RAISED, null, null)
    }
}