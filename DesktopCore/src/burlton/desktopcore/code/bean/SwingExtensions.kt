package burlton.desktopcore.code.bean

import javax.swing.JComboBox

fun <K> JComboBox<K>.items(): List<K>
{
    val list = mutableListOf<K>()
    for (i in 0..model.size)
    {
        list.add(model.getElementAt(i))
    }

    return list
}

fun <K> JComboBox<K>.selectedItemTyped(): K = getItemAt(selectedIndex)

fun <T> JComboBox<*>.findByConcreteClass(clazz: Class<T>): T? = items().find { clazz.isInstance(it) } as T?
inline fun <reified T> JComboBox<*>.findByClass(): T? = items().find { it is T } as T?

inline fun <reified T> JComboBox<*>.selectByClass()
{
    findByClass<T>()?.let { selectedItem = it }
}