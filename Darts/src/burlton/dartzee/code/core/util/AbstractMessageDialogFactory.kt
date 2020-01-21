package burlton.dartzee.code.core.util

import javax.swing.SwingUtilities

abstract class AbstractMessageDialogFactory
{
    abstract fun showInfo(text: String)
    abstract fun showError(text: String)
    abstract fun showQuestion(text: String, allowCancel: Boolean = false): Int
    abstract fun showLoading(text: String)
    abstract fun <K> showInput(title: String, message: String, options: Array<K>?, defaultOption: K?): K?
    abstract fun dismissLoading()

    fun showErrorLater(text: String)
    {
        SwingUtilities.invokeLater{ showError(text)}
    }
}