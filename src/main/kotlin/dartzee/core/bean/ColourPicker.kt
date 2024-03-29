package dartzee.core.bean

import dartzee.bean.IMouseListener
import dartzee.core.util.InjectedDesktopCore
import java.awt.Color
import java.awt.Cursor
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.border.LineBorder

class ColourPicker : JLabel(), IMouseListener {
    var selectedColour: Color = Color.BLACK
        private set

    private var listener: ColourSelectionListener? = null
    private val img: BufferedImage = BufferedImage(45, 30, BufferedImage.TYPE_INT_ARGB)

    init {
        border = LineBorder(Color(0, 0, 0))
        setSize(45, 30)
        isOpaque = true
        addMouseListener(this)
    }

    fun addColourSelectionListener(listener: ColourSelectionListener?) {
        this.listener = listener
    }

    fun updateSelectedColor(newColor: Color?, notify: Boolean = true) {
        newColor ?: return

        this.selectedColour = newColor
        img.paint { newColor }

        icon = ImageIcon(img)
        repaint()

        if (notify) {
            listener?.colourSelected(newColor)
        }
    }

    override fun mouseReleased(e: MouseEvent) {
        val newColour = InjectedDesktopCore.colourSelector.selectColour(selectedColour)
        updateSelectedColor(newColour)
    }

    override fun mouseEntered(e: MouseEvent) {
        cursor = Cursor(Cursor.HAND_CURSOR)
    }

    override fun mouseExited(e: MouseEvent) {
        cursor = Cursor(Cursor.DEFAULT_CURSOR)
    }
}
