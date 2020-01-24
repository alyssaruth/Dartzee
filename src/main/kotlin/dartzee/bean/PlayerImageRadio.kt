package dartzee.bean

import dartzee.db.PlayerImageEntity
import java.awt.Color
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

/**
 * Wrap up a PlayerImage so we can render the icon, and store its ID to point a PlayerEntity at it
 */
class PlayerImageRadio(pi: PlayerImageEntity) :  JPanel(), ChangeListener, MouseListener, FocusListener
{
    var playerImageId = ""
    val rdbtn = JRadioButton()
    val lblImg = JLabel()

    init
    {
        border = EmptyBorder(1, 1, 1, 1)
        lblImg.icon = pi.asImageIcon()
        playerImageId = pi.rowId

        add(rdbtn)
        add(lblImg)

        rdbtn.addFocusListener(this)
        rdbtn.addChangeListener(this)
        lblImg.addMouseListener(this)
    }

    fun isSelected() = rdbtn.isSelected

    fun addToButtonGroup(bg: ButtonGroup)
    {
        bg.add(rdbtn)
    }

    override fun stateChanged(arg0: ChangeEvent) = updateBorder()
    override fun focusLost(e: FocusEvent?) = updateBorder()
    override fun focusGained(e: FocusEvent?) = updateBorder()
    private fun updateBorder()
    {
        if (rdbtn.isSelected)
        {
            border = LineBorder(Color.BLACK)
        }
        else if (rdbtn.hasFocus())
        {
            border = BorderFactory.createDashedBorder(Color.GRAY)
        }
        else
        {
            border = EmptyBorder(1, 1, 1, 1)
        }
    }

    override fun mouseClicked(arg0: MouseEvent)
    {
        rdbtn.isSelected = true
    }

    override fun mouseEntered(arg0: MouseEvent){}
    override fun mouseExited(arg0: MouseEvent){}
    override fun mousePressed(arg0: MouseEvent){}
    override fun mouseReleased(arg0: MouseEvent){}
}
