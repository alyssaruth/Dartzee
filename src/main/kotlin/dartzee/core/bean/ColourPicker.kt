package dartzee.core.bean

import dartzee.core.util.InjectedDesktopCore
import dartzee.utils.DartsColour
import java.awt.Color
import java.awt.Cursor
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.border.LineBorder

class ColourPicker : JLabel(), MouseListener
{
    var selectedColour: Color = Color.BLACK
        private set

    private var listener: ColourSelectionListener? = null
    private var img: BufferedImage = BufferedImage(45, 30, BufferedImage.TYPE_INT_ARGB)

    init
    {
        border = LineBorder(Color(0, 0, 0))
        setSize(45, 30)
        isOpaque = true
        addMouseListener(this)
    }

    fun addColourSelectionListener(listener: ColourSelectionListener?)
    {
        this.listener = listener
    }

    fun updateSelectedColor(newColor: Color?, notify: Boolean = true)
    {
        newColor ?: return

        this.selectedColour = newColor
        img.paint { newColor }

        icon = ImageIcon(img)
        repaint()

        if (notify) {
            listener?.colourSelected(newColor)
        }
    }

    fun getPrefString() = DartsColour.toPrefStr(selectedColour)

    override fun mouseReleased(arg0: MouseEvent)
    {
        val newColour = InjectedDesktopCore.colourSelector.selectColour(selectedColour)
        updateSelectedColor(newColour)
    }

    override fun mouseEntered(arg0: MouseEvent)
    {
        cursor = Cursor(Cursor.HAND_CURSOR)
    }

    override fun mouseExited(arg0: MouseEvent)
    {
        cursor = Cursor(Cursor.DEFAULT_CURSOR)
    }

    override fun mousePressed(arg0: MouseEvent) {}
    override fun mouseClicked(arg0: MouseEvent) {}
}