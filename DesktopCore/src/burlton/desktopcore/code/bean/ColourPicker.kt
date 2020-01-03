package burlton.desktopcore.code.bean

import burlton.desktopcore.code.util.InjectedDesktopCore
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

    private var listener: ColourSelectionListener? = null
    private var img: BufferedImage = BufferedImage(30, 20, BufferedImage.TYPE_INT_ARGB)

    init
    {
        border = LineBorder(Color(0, 0, 0))
        setSize(30, 20)
        isOpaque = true
        addMouseListener(this)
    }

    fun addColourSelectionListener(listener: ColourSelectionListener?)
    {
        this.listener = listener
    }

    fun updateSelectedColor(newColor: Color?)
    {
        newColor ?: return

        this.selectedColour = newColor

        for (x in 0 until width)
        {
            for (y in 0 until height)
            {
                img.setRGB(x, y, newColor.rgb)
            }
        }

        icon = ImageIcon(img)
        repaint()
    }

    override fun mouseClicked(arg0: MouseEvent)
    {
        val newColour = InjectedDesktopCore.colourSelector.selectColour(selectedColour)
        updateSelectedColor(newColour)

        listener?.colourSelected(newColour)
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
    override fun mouseReleased(arg0: MouseEvent) {}
}