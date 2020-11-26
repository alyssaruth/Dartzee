package dartzee.logging

import dartzee.core.bean.WrapLayout
import dartzee.screen.FocusableWindow
import dartzee.utils.DartsDatabaseUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.border.MatteBorder
import javax.swing.text.BadLocationException
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext

class LoggingConsole: FocusableWindow(), ILogDestination
{
    override val windowName = "Console"

    val doc = DefaultStyledDocument()
    val scrollPane = JScrollPane()
    private val textArea = JTextPane(doc)
    private val contextPanel = JPanel()

    init
    {
        title = "Console"
        setSize(1000, 600)
        setLocationRelativeTo(null)
        contentPane.layout = BorderLayout(0, 0)
        contentPane.add(contextPanel, BorderLayout.NORTH)
        contentPane.add(scrollPane)
        textArea.foreground = Color.GREEN
        textArea.background = Color.BLACK
        textArea.isEditable = false
        scrollPane.setViewportView(textArea)

        contextPanel.background = Color.BLACK
        contextPanel.border = MatteBorder(0, 0, 2, 0, Color.GREEN)
        contextPanel.layout = WrapLayout()
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

            val dbName = record.keyValuePairs[KEY_DATABASE_NAME]
            if (dbName != DartsDatabaseUtil.DATABASE_NAME)
            {
                StyleConstants.setBackground(style, Color.DARK_GRAY)
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

            val threadStack = record.keyValuePairs[KEY_STACK]
            threadStack?.let { doc.insertString(doc.length, "\n$it", style) }

            textArea.select(doc.length, doc.length)
        }
        catch (ble: BadLocationException)
        {
            System.err.println("BLE trying to append: $text")
            System.err.println(extractStackTrace(ble))
        }
    }

    override fun contextUpdated(context: Map<String, Any?>)
    {
        contextPanel.removeAll()
        val labels = context.map(::factoryLabelForContext)

        labels.forEach { contextPanel.add(it) }

        contextPanel.validate()
        contextPanel.repaint()
    }

    private fun factoryLabelForContext(field: Map.Entry<String, Any?>): Component
    {
        val label = JLabel("${field.key}: ${field.value}")
        label.foreground = Color.GREEN
        label.border = EmptyBorder(5, 5, 5, 5)

        val panel = JPanel()
        panel.border = LineBorder(Color.GREEN)
        panel.add(label)
        panel.isOpaque = false
        panel.background = null
        return panel
    }

    fun clear()
    {
        textArea.text = ""
    }
}