package dartzee.core.util

import dartzee.logging.*
import dartzee.utils.InjectedThings
import java.awt.Component
import java.io.File
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

object DialogUtil
{
    private var dialogFactory: AbstractMessageDialogFactory = MessageDialogFactory()

    fun init(implementation: AbstractMessageDialogFactory)
    {
        dialogFactory = implementation
    }

    fun showInfo(infoText: String)
    {
        logDialogShown("Info", "Information", infoText)
        dialogFactory.showInfo(infoText)
        logDialogClosed("Info", null)
    }

    fun showCustomMessage(message: Any)
    {
        logDialogShown("CustomInfo", "Information", "?")
        dialogFactory.showCustomMessage(message)
        logDialogClosed("CustomInfo", null)
    }

    fun showError(errorText: String)
    {
        dismissLoadingDialog()

        logDialogShown("Error", "Error", errorText)
        dialogFactory.showError(errorText)
        logDialogClosed("Error", null)
    }

    fun showErrorLater(errorText: String)
    {
        SwingUtilities.invokeLater { showError(errorText) }
    }

    fun showQuestion(message: String, allowCancel: Boolean = false): Int
    {
        logDialogShown("Question", "Question", message)
        val selection = dialogFactory.showQuestion(message, allowCancel)
        logDialogClosed("Question", selection)
        return selection
    }

    fun showLoadingDialog(text: String)
    {
        logDialogShown("Loading", "", text)
        dialogFactory.showLoading(text)
    }

    fun dismissLoadingDialog()
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
