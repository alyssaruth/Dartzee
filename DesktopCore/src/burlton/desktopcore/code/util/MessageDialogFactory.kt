package burlton.desktopcore.code.util

import burlton.desktopcore.code.screen.LoadingDialog
import javax.swing.JOptionPane

class MessageDialogFactory: AbstractMessageDialogFactory()
{
    private val loadingDialog = LoadingDialog()

    override fun showInfo(text: String)
    {
        JOptionPane.showMessageDialog(null, text, "Information", JOptionPane.INFORMATION_MESSAGE)
    }

    override fun <K> showInput(title: String, message: String, options: Array<K>?, defaultOption: K?): K?
    {
        return JOptionPane.showInputDialog(null, message,  title, JOptionPane.PLAIN_MESSAGE, null, options, defaultOption) as K?
    }

    override fun showError(text: String)
    {
        dismissLoading()

        JOptionPane.showMessageDialog(null, text, "Error", JOptionPane.ERROR_MESSAGE)
    }

    override fun showQuestion(text: String, allowCancel: Boolean): Int
    {
        val option = if (allowCancel) JOptionPane.YES_NO_CANCEL_OPTION else JOptionPane.YES_NO_OPTION
        return JOptionPane.showConfirmDialog(null, text, "Question", option, JOptionPane.QUESTION_MESSAGE)
    }

    override fun showLoading(text: String)
    {
        loadingDialog.showDialog(text)
    }

    override fun dismissLoading()
    {
        loadingDialog.dismissDialog()
    }

}