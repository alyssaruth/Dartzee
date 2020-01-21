package burlton.dartzee.code.core.screen

import net.miginfocom.swing.MigLayout
import javax.swing.*

class LoadingDialog : JDialog()
{
    private val lblMessage = JLabel("Communicating with Server...")

    init
    {
        layout = MigLayout("gapy 12px", "[grow]", "[]")
        setSize(250, 115)
        setLocationRelativeTo(null)
        isResizable = false
        isModal = true
        isAlwaysOnTop = true
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        lblMessage.horizontalAlignment = SwingConstants.CENTER
        contentPane.add(lblMessage, "cell 0 0, alignx center")

        val progressBar = JProgressBar()
        progressBar.isIndeterminate = true

        contentPane.add(progressBar, "cell 0 1, alignx center")
    }

    fun showDialog(textToShow: String)
    {
        val showRunnable = Runnable {
            lblMessage.text = textToShow
            isVisible = true
        }

        SwingUtilities.invokeLater(showRunnable)
    }

    fun dismissDialog()
    {
        val hideRunnable = Runnable { isVisible = false }

        SwingUtilities.invokeLater(hideRunnable)
    }
}