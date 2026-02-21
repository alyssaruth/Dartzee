package dartzee.bean

import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener

interface IMouseListener : MouseListener, MouseMotionListener {
    override fun mouseMoved(e: MouseEvent) {}

    override fun mouseDragged(e: MouseEvent) {}

    override fun mouseEntered(e: MouseEvent) {}

    override fun mouseExited(e: MouseEvent) {}

    override fun mouseClicked(e: MouseEvent) {}

    override fun mousePressed(e: MouseEvent) {}

    override fun mouseReleased(e: MouseEvent) {}
}
