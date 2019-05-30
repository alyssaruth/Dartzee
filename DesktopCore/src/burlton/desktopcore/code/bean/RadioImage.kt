package burlton.desktopcore.code.bean

import java.awt.Color
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

open class RadioImage(img: ImageIcon) : JPanel(), ChangeListener, MouseListener
{
    private val rdbtn = JRadioButton()
    private val lblImg = JLabel()

    init
    {
        border = EmptyBorder(1, 1, 1, 1)
        lblImg.icon = img

        add(rdbtn)
        add(lblImg)

        rdbtn.addChangeListener(this)
        lblImg.addMouseListener(this)
    }

    fun isSelected() = rdbtn.isSelected

    fun addToButtonGroup(bg: ButtonGroup)
    {
        bg.add(rdbtn)
    }

    override fun stateChanged(arg0: ChangeEvent)
    {
        if (rdbtn.isSelected)
        {
            border = LineBorder(Color.BLACK)
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
