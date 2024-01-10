package dartzee.core.bean

import java.awt.EventQueue
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.AbstractAction
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

class CheatBar : JTextField(), ActionListener {
    private lateinit var listener: AbstractDevScreen

    init {
        border = null
        isOpaque = false
        isVisible = false
        border = BorderFactory.createEmptyBorder(0, 5, 0, 0)

        addActionListener(this)
    }

    fun setCheatListener(listener: AbstractDevScreen) {
        this.listener = listener

        val triggerStroke = listener.getKeyStrokeForCommandBar()
        val content = listener.contentPane as JPanel

        val inputMap = content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        inputMap.put(triggerStroke, "showCheatBar")

        val actionMap = content.actionMap
        actionMap.put(
            "showCheatBar",
            object : AbstractAction() {
                override fun actionPerformed(e: ActionEvent) {
                    if (listener.commandsEnabled()) {
                        listener.enableCheatBar(true)
                        EventQueue.invokeLater { grabFocus() }
                    }
                }
            }
        )
    }

    override fun actionPerformed(arg0: ActionEvent) {
        val text = text
        setText(null)

        val result = listener.processCommandWithTry(text)
        if (result.isEmpty()) {
            listener.enableCheatBar(false)
        } else {
            setText(result)
        }
    }
}
