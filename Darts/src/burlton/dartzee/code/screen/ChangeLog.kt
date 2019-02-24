package burlton.dartzee.code.screen

import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextArea

class ChangeLog : JFrame() {

    private val scrollPane = JScrollPane()
    private val textArea = JTextArea()

    init
    {
        title = "Change Log"
        setSize(500, 570)
        setLocationRelativeTo(null)
        contentPane.layout = BorderLayout(0, 0)
        contentPane.add(scrollPane)
        textArea.foreground = Color.BLACK
        textArea.background = Color.WHITE
        textArea.isEditable = false
        appendChangeLog()
        scrollPane.setViewportView(textArea)
    }

    private fun appendChangeLog()
    {
        textArea.text = ""

        val text = javaClass.getResource("/ChangeLog").readText()
        textArea.append(text)

        textArea.caretPosition = 0
        textArea.lineWrap = true
    }
}