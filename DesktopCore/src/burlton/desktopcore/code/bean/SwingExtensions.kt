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

fun <K> JComboBox<K>.selectedItemTyped() = getItemAt(selectedIndex)