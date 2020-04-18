package dartzee.logging

import dartzee.utils.InjectedThings.logger
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext

class LoggingConsole: JFrame(), ILogDestination
{
    val doc = DefaultStyledDocument()
    val scrollPane = JScrollPane()
    private val textArea = JTextPane(doc)
    private val contextPanel = JPanel()
    val labelContext = JLabel()

    init
    {
        title = "Console"
        setSize(1000, 600)
        setLocationRelativeTo(null)
        contentPane.layout = BorderLayout(0, 0)
        contentPane.add(contextPanel, BorderLayout.NORTH)
        contentPane.add(scrollPane)
        contextPanel.add(labelContext)
        textArea.foreground = Color.GREEN
        textArea.background = Color.BLACK
        textArea.isEditable = false
        scrollPane.setViewportView(textArea)

        labelContext.border = EmptyBorder(5, 0, 5, 0)
        labelContext.foreground = Color.GREEN
        contextPanel.background = Color.BLACK
        contextPanel.border = MatteBorder(0, 0, 2, 0, Color.GREEN)
    }

    override fun log(record: LogRecord)
    {
        val cx = StyleContext()
        val text = record.toString()
        val style = cx.addStyle(text, null)
        if (record.loggingCode == CODE_SQL)
        {
            when
            {
                text.contains("INSERT") -> StyleConstants.setForeground(style, Color.ORANGE)
                text.contains("UPDATE") -> StyleConstants.setForeground(style, Color.ORANGE)
                text.contains("DELETE") -> StyleConstants.setForeground(style, Color.PINK)
                else -> StyleConstants.setForeground(style, Color.CYAN)
            }
        }

        if (record.severity == Severity.ERROR)
        {
            StyleConstants.setForeground(style, Color.RED)
        }

        try
        {
            doc.insertString(doc.length, "\n$text", style)
            record.getThrowableStr()?.let { doc.insertString(doc.length, "\n$it", style) }

            textArea.select(doc.length, doc.length)
        }
        catch (ble: BadLocationException)
        {
            System.err.println("BLE trying to append: $text")
            System.err.println(extractStackTrace(ble))
        }

        updateLoggingContext()
    }

    private fun updateLoggingContext()
    {
        val cxFields = logger.loggingContext.toMap()

        labelContext.text = cxFields.entries.joinToString("  |  ") { "${it.key}: ${it.value}" }
        labelContext.repaint()
    }

    fun clear()
    {
        textArea.text = ""
    }
}