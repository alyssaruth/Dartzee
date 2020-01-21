package burlton.dartzee.code.core.screen

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.border.EmptyBorder

class ProgressDialog(private var workToDo: Int, private var message: String) : JDialog(), ActionListener
{
    private var cancelPressed = false

    private val progressBar = JProgressBar()
    private val btnCancel = JButton("Cancel")

    init
    {
        val panel = JPanel()
        panel.border = EmptyBorder(10, 0, 0, 0)
        contentPane.add(panel, BorderLayout.CENTER)
        progressBar.preferredSize = Dimension(200, 20)
        progressBar.isStringPainted = true
        panel.add(progressBar)

        val panelCancel = JPanel()
        panelCancel.border = EmptyBorder(5, 0, 5, 0)
        contentPane.add(panelCancel, BorderLayout.SOUTH)
        panelCancel.add(btnCancel)
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE

        btnCancel.addActionListener(this)
        btnCancel.isVisible = false
    }

    fun setVisibleLater()
    {
        SwingUtilities.invokeLater { isVisible = true }
    }

    fun resetProgress()
    {
        SwingUtilities.invokeLater {
            progressBar.minimum = 0
            progressBar.maximum = workToDo
            progressBar.value = 0
            progressBar.string = "$workToDo $message"
        }
    }

    fun incrementProgressLater(increment: Int = 1)
    {
        SwingUtilities.invokeLater {
            val newValue = progressBar.value + increment
            progressBar.value = newValue
            progressBar.string = (workToDo - newValue).toString() + " " + message
            progressBar.repaint()
        }
    }


    fun cancelPressed(): Boolean
    {
        return cancelPressed
    }

    fun disposeLater()
    {
        SwingUtilities.invokeLater { dispose() }
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        cancelPressed = true
    }

    fun showCancel(showCancel: Boolean)
    {
        btnCancel.isVisible = showCancel

        val height = if (showCancel) 120 else 90
        setSize(300, height)
    }

    companion object
    {
        fun factory(title: String, message: String, workToDo: Int): ProgressDialog
        {
            val dialog = ProgressDialog(workToDo, message)
            dialog.resetProgress()
            dialog.title = title
            dialog.setSize(300, 90)
            dialog.isResizable = false
            dialog.setLocationRelativeTo(null)
            dialog.isModal = true

            return dialog
        }
    }
}
