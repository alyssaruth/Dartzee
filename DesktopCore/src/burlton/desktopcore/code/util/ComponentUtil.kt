package burlton.desktopcore.code.util

import burlton.core.code.util.Debug
import java.awt.Component
import java.awt.Container
import java.awt.Window
import java.awt.event.ActionListener
import java.lang.reflect.Method
import javax.swing.AbstractButton
import javax.swing.ButtonGroup
import javax.swing.JComponent

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

fun Container.addActionListenerToAllChildren(listener: ActionListener)
{
    val children = getAllChildComponentsForType(this, JComponent::class.java)
    children.forEach {
        //Check for an `addActionListener` method and call it if it exists
        val removeMethod = it.javaClass.findMethod("removeActionListener", ActionListener::class.java)
        val method = it.javaClass.findMethod("addActionListener", ActionListener::class.java)
        removeMethod?.invoke(it, listener)
        method?.invoke(it, listener)
    }
}

fun Class<*>.findMethod(name: String, vararg parameterTypes: Class<*>): Method?
{
    return try
    {
        getMethod(name, parameterTypes[0])
    }
    catch (e: Exception) { null }
}

fun Container.enableChildren(enable: Boolean)
{
    getAllChildComponentsForType(this, Component::class.java).forEach{
        it.isEnabled = enable
    }
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