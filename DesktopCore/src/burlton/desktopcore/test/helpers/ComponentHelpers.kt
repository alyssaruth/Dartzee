package burlton.desktopcore.test.helpers

import burlton.core.code.util.Debug
import burlton.desktopcore.code.bean.ScrollTable
import io.mockk.mockk
import java.awt.Component
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.MOUSE_CLICKED
import java.awt.event.MouseListener
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.KeyStroke

val MOUSE_EVENT_SINGLE_CLICK = MouseEvent(JButton(), MOUSE_CLICKED, System.currentTimeMillis(), -1, 0, 0, 1, false)

fun makeMouseEvent(clickCount: Int = 1, x: Int = 0, y: Int = 0): MouseEvent
{
    return MouseEvent(JButton(), MOUSE_CLICKED, System.currentTimeMillis(), -1, x, y, clickCount, false)
}

/**
 * Test methods
 */
fun processKeyPress(component: JComponent, key: Int)
{
    if (component is ScrollTable)
    {
        processKeyPress(component.table, key)
        return
    }

    val actionName = component.inputMap[KeyStroke.getKeyStroke(key, JComponent.WHEN_FOCUSED)]

    Debug.append("" + actionName)
    val action = component.actionMap[actionName]

    action.actionPerformed(mockk(relaxed = true))
}

fun doubleClick(component: Component)
{
    val mouseEvent = MouseEvent(component, MOUSE_CLICKED, System.currentTimeMillis(), -1,
    0, 0, 2, false)

    if (component is MouseListener)
    {
        component.mouseClicked(mouseEvent)
    }
}

fun singleClick(component: Component)
{
    val mouseEvent = MouseEvent(component, MOUSE_CLICKED, System.currentTimeMillis(), -1,
            0, 0, 1, false)

    component.mouseListeners.forEach { it.mouseClicked(mouseEvent) }
}