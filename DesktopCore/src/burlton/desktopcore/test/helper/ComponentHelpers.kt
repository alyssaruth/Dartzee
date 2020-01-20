package burlton.desktopcore.test.helper

import burlton.dartzee.code.core.bean.ScrollTable
import io.mockk.mockk
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.FocusEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.MOUSE_CLICKED
import java.awt.event.MouseListener
import java.awt.image.BufferedImage
import javax.swing.*

val MOUSE_EVENT_SINGLE_CLICK = MouseEvent(JButton(), MOUSE_CLICKED, System.currentTimeMillis(), -1, 0, 0, 1, false)

fun makeMouseEvent(clickCount: Int = 1, x: Int = 0, y: Int = 0, component: Component = JButton()): MouseEvent
{
    return MouseEvent(component, MOUSE_CLICKED, System.currentTimeMillis(), -1, x, y, clickCount, false)
}

fun makeActionEvent(component: Component): ActionEvent
{
    return ActionEvent(component, 0, null)
}

fun JComponent.simulateLoseFocus()
{
    focusListeners.forEach { it.focusLost(FocusEvent(this, FocusEvent.FOCUS_LOST)) }
}

/**
 * Test methods
 */
fun JComponent.processKeyPress(key: Int)
{
    if (this is ScrollTable)
    {
        this.table.processKeyPress(key)
        return
    }

    val actionName = inputMap[KeyStroke.getKeyStroke(key, JComponent.WHEN_FOCUSED)]
    if (!actionMap.keys().contains(actionName)) {
        return
    }

    val action = actionMap[actionName]
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

fun JLabel.getIconImage(): BufferedImage = (icon as ImageIcon).image as BufferedImage