package dartzee.core.screen

import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JProgressBar
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import net.miginfocom.swing.MigLayout

class LoadingDialog(val message: String) : JDialog() {
    private val lblMessage = JLabel(message)

    init {
        layout = MigLayout("gapy 12px", "[grow]", "[]")
        setSize(250, 115)
        setLocationRelativeTo(null)
        isResizable = false
        isModal = true
        isAlwaysOnTop = true
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        lblMessage.horizontalAlignment = SwingConstants.CENTER
        contentPane.add(lblMessage, "cell 0 0, alignx center")

        val progressBar = JProgressBar()
        progressBar.isIndeterminate = true

        contentPane.add(progressBar, "cell 0 1, alignx center")
    }

    fun showDialog() {
        val showRunnable = Runnable { isVisible = true }

        SwingUtilities.invokeLater(showRunnable)
    }

    fun dismissDialog() {
        val hideRunnable = Runnable { isVisible = false }

        SwingUtilities.invokeLater(hideRunnable)
    }
}
