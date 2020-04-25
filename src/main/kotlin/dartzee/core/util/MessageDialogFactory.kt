package dartzee.core.util

import dartzee.core.screen.LoadingDialog
import dartzee.logging.CODE_DIALOG_SHOWN
import dartzee.logging.KEY_DIALOG_MESSAGE
import dartzee.logging.KEY_DIALOG_TITLE
import dartzee.logging.KEY_DIALOG_TYPE
import dartzee.utils.InjectedThings.logger
import javax.swing.JOptionPane

class MessageDialogFactory: AbstractMessageDialogFactory()
{
    private val loadingDialog = LoadingDialog()

    override fun showInfo(text: String)
    {
        logDialogShown("Info", "Information", text)
        JOptionPane.showMessageDialog(null, text, "Information", JOptionPane.INFORMATION_MESSAGE)
    }

    override fun <K> showInput(title: String, message: String, options: Array<K>?, defaultOption: K?): K?
    {
        logDialogShown("Input", title, message)
        return JOptionPane.showInputDialog(null, message, title, JOptionPane.PLAIN_MESSAGE, null, options, defaultOption) as K?
    }

    override fun showError(text: String)
    {
        dismissLoading()

        logDialogShown("Error", "Error", text)
        JOptionPane.showMessageDialog(null, text, "Error", JOptionPane.ERROR_MESSAGE)
    }

    override fun showQuestion(text: String, allowCancel: Boolean): Int
    {
        logDialogShown("Question", "Question", text)
        val option = if (allowCancel) JOptionPane.YES_NO_CANCEL_OPTION else JOptionPane.YES_NO_OPTION
        return JOptionPane.showConfirmDialog(null, text, "Question", option, JOptionPane.QUESTION_MESSAGE)
    }

    override fun showLoading(text: String)
    {
        logDialogShown("Loading", "", text)
        loadingDialog.showDialog(text)
    }

    override fun dismissLoading()
    {
        loadingDialog.dismissDialog()
    }

    private fun logDialogShown(type: String, title: String, message: String)
    {
        logger.info(CODE_DIALOG_SHOWN, "$type dialog shown: $message", KEY_DIALOG_TYPE to type, KEY_DIALOG_TITLE to title, KEY_DIALOG_MESSAGE to message)
    }
}