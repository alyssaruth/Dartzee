package dartzee.core.util

import dartzee.core.screen.LoadingDialog
import dartzee.logging.CODE_DIALOG_CLOSED
import dartzee.logging.CODE_DIALOG_SHOWN
import dartzee.logging.KEY_DIALOG_MESSAGE
import dartzee.logging.KEY_DIALOG_SELECTION
import dartzee.logging.KEY_DIALOG_TITLE
import dartzee.logging.KEY_DIALOG_TYPE
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings
import java.awt.Component
import java.io.File
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

object DialogUtil
{
    private var loadingDialog: LoadingDialog? = null
    private var dialogFactory: AbstractMessageDialogFactory = MessageDialogFactory()

    fun init(implementation: AbstractMessageDialogFactory)
    {
        dialogFactory = implementation
    }

    fun showInfoOLD(infoText: String)
    {
        logDialogShown("Info", "Information", infoText)
        dialogFactory.showInfo(infoText)
        logDialogClosed("Info", null)
    }

    fun showInfo(infoText: String, parent: Component = ScreenCache.mainScreen)
    {
        logDialogShown("Info", "Information", infoText)
        JOptionPane.showMessageDialog(parent, infoText, "Information", JOptionPane.INFORMATION_MESSAGE)
        logDialogClosed("Info", null)
    }

    fun showCustomMessage(message: Any, parent: Component = ScreenCache.mainScreen)
    {
        logDialogShown("CustomInfo", "Information", "?")
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE)
        logDialogClosed("CustomInfo", null)
    }

    fun showErrorOLD(errorText: String)
    {
        dismissLoadingDialogOLD()

        logDialogShown("Error", "Error", errorText)
        dialogFactory.showError(errorText)
        logDialogClosed("Error", null)
    }

    fun showError(errorText: String, parent: Component? = ScreenCache.mainScreen)
    {
        dismissLoadingDialog()

        logDialogShown("Error", "Error", errorText)
        JOptionPane.showMessageDialog(parent, errorText, "Error", JOptionPane.ERROR_MESSAGE)
        logDialogClosed("Error", null)
    }

    fun showErrorLater(errorText: String)
    {
        SwingUtilities.invokeLater { showErrorOLD(errorText) }
    }

    fun showQuestionOLD(message: String, allowCancel: Boolean = false): Int
    {
        logDialogShown("Question", "Question", message)
        val selection = dialogFactory.showQuestion(message, allowCancel)
        logDialogClosed("Question", selection)
        return selection
    }

    fun showQuestion(message: String, allowCancel: Boolean = false, parent: Component = ScreenCache.mainScreen): Int
    {
        logDialogShown("Question", "Question", message)
        val option = if (allowCancel) JOptionPane.YES_NO_CANCEL_OPTION else JOptionPane.YES_NO_OPTION
        val selection = JOptionPane.showConfirmDialog(parent, message, "Question", option, JOptionPane.QUESTION_MESSAGE)
        logDialogClosed("Question", selection)
        return selection
    }

    fun showLoadingDialogOLD(text: String)
    {
        logDialogShown("Loading", "", text)
        dialogFactory.showLoading(text)
    }

    fun showLoadingDialog(text: String)
    {
        logDialogShown("Loading", "", text)
        loadingDialog = LoadingDialog()
        loadingDialog?.showDialog(text)
    }

    fun dismissLoadingDialog()
    {
        val wasVisible = loadingDialog?.isVisible ?: false
        loadingDialog?.dismissDialog()
        if (wasVisible)
        {
            logDialogClosed("Loading", null)
        }
    }

    fun dismissLoadingDialogOLD()
    {
        val dismissed = dialogFactory.dismissLoading()
        if (dismissed)
        {
            logDialogClosed("Loading", null)
        }
    }

    fun showOption(title: String, message: String, options: List<String>): String?
    {
        logDialogShown("Option", title, message)
        val selectionStr = dialogFactory.showOption(title, message, options)
        logDialogClosed("Option", selectionStr)
        return selectionStr
    }

    fun <K> showInput(title: String, message: String, options: Array<K>? = null, defaultOption: K? = null): K?
    {
        logDialogShown("Input", title, message)
        val selection = dialogFactory.showInput(title, message, options, defaultOption)
        logDialogClosed("Input", selection)
        return selection
    }

    fun chooseDirectory(parent: Component?): File?
    {
        logDialogShown("File selector", "", "")
        val file = dialogFactory.chooseDirectory(parent)
        logDialogClosed("File selector", file?.absolutePath)
        return file
    }


    private fun logDialogShown(type: String, title: String, message: String)
    {
        InjectedThings.logger.info(CODE_DIALOG_SHOWN, "$type dialog shown: $message", KEY_DIALOG_TYPE to type, KEY_DIALOG_TITLE to title, KEY_DIALOG_MESSAGE to message)
    }

    private fun logDialogClosed(type: String, selection: Any?)
    {
        var message = "$type dialog closed"
        selection?.let { message += " - selected ${translateOption(it)}" }

        InjectedThings.logger.info(
            CODE_DIALOG_CLOSED, message,
            KEY_DIALOG_TYPE to type,
            KEY_DIALOG_SELECTION to selection)
    }
    private fun translateOption(option: Any?) =
        when (option)
        {
            JOptionPane.YES_OPTION -> "Yes"
            JOptionPane.NO_OPTION -> "No"
            JOptionPane.CANCEL_OPTION -> "Cancel"
            else -> option
        }
}
