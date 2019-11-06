package burlton.desktopcore.code.bean

import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.AbstractAction
import javax.swing.JComboBox
import javax.swing.JTextField
import javax.swing.text.JTextComponent


fun <K> JComboBox<K>.items(): List<K>
{
    val list = mutableListOf<K>()
    for (i in 0 until model.size)
    {
        list.add(model.getElementAt(i))
    }

    return list
}

fun <K> JComboBox<K>.selectedItemTyped(): K = getItemAt(selectedIndex)

fun <T> JComboBox<*>.findByConcreteClass(clazz: Class<T>): T? = items().find { clazz.isInstance(it) } as T?
inline fun <reified T> JComboBox<*>.findByClass(): T? = items().find { it is T } as T?

inline fun <reified T> JComboBox<*>.selectByClass() = findByClass<T>()?.also { selectedItem = it }

fun JTextField.addUpdateListener(actionListener: ActionListener)
{
    addFocusListener(object: FocusListener
    {
        override fun focusLost(e: FocusEvent?)
        {
            val event = ActionEvent(this, -1, "")
            actionListener.actionPerformed(event)
        }
        override fun focusGained(e: FocusEvent?){}
    })
}

fun ScrollTable.addKeyAction(key: Int, fn: () -> Unit)
{
    val action = object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            fn()
        }
    }

    addKeyAction(key, "ACTION_$key", action)
}

fun JTextComponent.addGhostText(text: String)
{
    this.layout = BorderLayout()
    this.add(GhostText(text, this))
}