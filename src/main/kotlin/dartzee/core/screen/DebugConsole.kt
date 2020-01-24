package dartzee.core.screen

import dartzee.core.util.Debug
import dartzee.core.util.DebugOutput
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext

class DebugConsole : JFrame(), DebugOutput
{
    private val doc = DefaultStyledDocument()
    private val scrollPane = JScrollPane()
    private val textArea = JTextPane(doc)

    init
    {
        title = "Console"
        setSize(1000, 600)
        setLocationRelativeTo(null)
        contentPane.layout = BorderLayout(0, 0)
        contentPane.add(scrollPane)
        textArea.foreground = Color.GREEN
        textArea.background = Color.BLACK
        textArea.isEditable = false
        scrollPane.setViewportView(textArea)
    }

    override fun append(text: String)
    {
        val cx = StyleContext()
        val style = cx.addStyle(text, null)
        if (text.contains(Debug.SQL_PREFIX))
        {
            when
            {
                text.contains("INSERT") -> StyleConstants.setForeground(style, Color.ORANGE)
                text.contains("UPDATE") -> StyleConstants.setForeground(style, Color.ORANGE)
                text.contains("DELETE") -> StyleConstants.setForeground(style, Color.RED)
                else -> StyleConstants.setForeground(style, Color.CYAN)
            }
        }

        try
        {
            doc.insertString(doc.length, text, style)
            textArea.select(doc.length, doc.length)
        }
        catch (ble: BadLocationException)
        {
            Debug.stackTrace(ble, "BLE trying to append: $text")
        }
    }

    override fun getLogs(): String
    {
        return try
        {
            doc.getText(0, doc.length)
        }
        catch (t: Throwable)
        {
            Debug.stackTrace(t)
            ""
        }
    }

    override fun clear()
    {
        textArea.text = ""
    }
}