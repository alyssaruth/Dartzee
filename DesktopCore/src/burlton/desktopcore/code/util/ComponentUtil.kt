package burlton.desktopcore.code.util

import burlton.core.code.util.Debug
import java.awt.Component
import java.awt.Container
import java.awt.Window
import javax.swing.AbstractButton
import javax.swing.ButtonGroup

/**
 * Recurses through all child components, returning an ArrayList of all children of the appropriate type
 */
fun <T> getAllChildComponentsForType(parent: Container, desiredClazz: Class<T>): MutableList<T>
{
    val ret = mutableListOf<T>()

    val components = parent.components
    addComponents(ret, components, desiredClazz)

    return ret
}

private fun <T> addComponents(ret: MutableList<T>, components: Array<Component>, desiredClazz: Class<T>)
{
    for (comp in components)
    {
        if (desiredClazz.isInstance(comp))
        {
            ret.add(comp as T)
        }

        if (comp is Container)
        {
            val subComponents = comp.components
            addComponents(ret, subComponents, desiredClazz)
        }
    }
}

fun containsComponent(parent: Container, component: Component): Boolean
{
    val list = getAllChildComponentsForType(parent, Component::class.java)
    return list.contains(component)
}

fun createButtonGroup(vararg buttons: AbstractButton)
{
    if (buttons.isEmpty())
    {
        Debug.stackTrace("Trying to create empty ButtonGroup.")
        return
    }

    val bg = ButtonGroup()
    buttons.forEach {
        bg.add(it)
    }

    //Enable the first button passed in by default
    buttons[0].isSelected = true
}

fun Container.getParentWindow(): Window?
{
    val myParent = parent ?: return null

    return if (myParent is Window) myParent else myParent.getParentWindow()
}