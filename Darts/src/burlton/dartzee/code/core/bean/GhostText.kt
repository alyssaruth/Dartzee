package burlton.dartzee.code.core.bean

import java.awt.Color
import javax.swing.JLabel
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.JTextComponent

class GhostText(ghostText: String, component: JTextComponent): JLabel(), DocumentListener
{
	private val document = component.document

	init
	{
        text = ghostText
        font = component.font
		foreground = Color(foreground.red, foreground.green, foreground.blue, 127)
        border = EmptyBorder(component.insets)
        horizontalAlignment = LEADING

		document.addDocumentListener(this)

		toggleVisibility()
	}

	private fun toggleVisibility()
	{
		isVisible = document.length == 0
	}

    /**
     * DocumentListener
     */
    override fun insertUpdate(e: DocumentEvent)
	{
		toggleVisibility()
	}

    override fun removeUpdate(e: DocumentEvent)
	{
		toggleVisibility()
	}
    override fun changedUpdate(e: DocumentEvent?) {}
}