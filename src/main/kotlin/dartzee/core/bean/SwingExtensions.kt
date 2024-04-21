package dartzee.core.bean

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Image
import java.awt.Point
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.image.BufferedImage
import javax.swing.AbstractAction
import javax.swing.AbstractButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.JTextPane
import javax.swing.KeyStroke
import javax.swing.UIDefaults
import javax.swing.text.JTextComponent

fun <K> JComboBox<K>.items() = (0 until model.size).map { model.getElementAt(it) }

fun <K> JComboBox<K>.selectedItemTyped(): K = getItemAt(selectedIndex)

@Suppress("UNCHECKED_CAST")
fun <T> JComboBox<*>.findByConcreteClass(clazz: Class<T>): T? =
    items().find { clazz.isInstance(it) } as T?

inline fun <reified T> JComboBox<*>.findByClass(): T? = items().find { it is T } as T?

inline fun <reified T> JComboBox<*>.selectByClass() = findByClass<T>()?.also { selectedItem = it }

fun JTextField.addUpdateListener(actionListener: ActionListener) {
    val tf = this
    addFocusListener(
        object : FocusListener {
            override fun focusLost(e: FocusEvent?) {
                val event = ActionEvent(tf, -1, "")
                actionListener.actionPerformed(event)
            }

            override fun focusGained(e: FocusEvent?) {}
        }
    )
}

fun JTable.addKeyAction(key: Int, fn: () -> Unit) {
    val action =
        object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                fn()
            }
        }

    inputMap.put(KeyStroke.getKeyStroke(key, JComponent.WHEN_FOCUSED), "ACTION_$key")
    actionMap.put("ACTION_$key", action)
}

fun JTextComponent.addGhostText(text: String) {
    this.layout = BorderLayout()
    this.add(GhostText(text, this))
}

fun Image.toBufferedImage(
    width: Int,
    height: Int,
    type: Int = BufferedImage.TYPE_INT_RGB
): BufferedImage {
    val bi = BufferedImage(width, height, type)
    val g = bi.createGraphics()
    g.drawImage(this, 0, 0, null)
    g.dispose()
    return bi
}

fun BufferedImage.paint(fn: (pt: Point) -> Color?) {
    val pts = getPointList(width, height)
    val colors = pts.map { fn(it)?.rgb ?: 0 }

    setRGB(0, 0, width, height, colors.toIntArray(), 0, width)
}

fun getPointList(width: Int, height: Int): List<Point> {
    val yRange = 0 until height
    val xRange = 0 until width

    return yRange.map { y -> xRange.map { x -> Point(x, y) } }.flatten()
}

fun AbstractButton.isSelectedAndEnabled() = isEnabled && isSelected

fun JScrollPane.scrollToBottom() {
    val vertical = verticalScrollBar
    vertical.value = vertical.maximum
}

fun makeTransparentTextPane() =
    JTextPane().apply {
        val uiDefault = UIDefaults()
        uiDefault["EditorPane[Enabled].backgroundPainter"] = null
        putClientProperty("Nimbus.Overrides", uiDefault)
        putClientProperty("Nimbus.Overrides.InheritDefaults", false)
        background = null
        isEditable = false
    }
