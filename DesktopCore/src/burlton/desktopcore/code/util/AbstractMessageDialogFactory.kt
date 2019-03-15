package burlton.desktopcore.code.util

import javax.swing.SwingUtilities

abstract class AbstractMessageDialogFactory
{
    abstract fun showInfo(text: String)
    abstract fun showError(text: String)
    abstract fun showQuestion(text: String, allowCancel: Boolean = false): Int
    abstract fun showLoading(text: String)
    abstract fun dismissLoading()

    fun showErrorLater(text: String)
    {
        SwingUtilities.invokeLater{ showError(text)}
    }
}