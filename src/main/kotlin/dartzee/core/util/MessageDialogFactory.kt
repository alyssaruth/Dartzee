package dartzee.core.util

import dartzee.core.screen.LoadingDialog
import dartzee.logging.*
import dartzee.utils.InjectedThings.logger
import java.awt.Component
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane

class MessageDialogFactory: AbstractMessageDialogFactory()
{
    private val loadingDialog = LoadingDialog()

    override fun showInfo(text: String)
    {
        logDialogShown("Info", "Information", text)
        JOptionPane.showMessageDialog(null, text, "Information", JOptionPane.INFORMATION_MESSAGE)
        logDialogClosed("Info", null)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <K> showInput(title: String, message: String, options: Array<K>?, defaultOption: K?): K?
    {
        logDialogShown("Input", title, message)
        val result = JOptionPane.showInputDialog(null, message, title, JOptionPane.PLAIN_MESSAGE, null, options, defaultOption) as K?
        logDialogClosed("Input", result)
        return result
    }

    override fun showError(text: String)
    {
        dismissLoading()

        logDialogShown("Error", "Error", text)
        JOptionPane.showMessageDialog(null, text, "Error", JOptionPane.ERROR_MESSAGE)
        logDialogClosed("Error", null)
    }

    override fun showOption(title: String, message: String, options: List<String>): String?
    {
        logDialogShown("Option", title, message)

        val typedArray = options.toTypedArray()
        val selection = JOptionPane.showOptionDialog(
            null, message, title, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, typedArray, options.first())

        val selectionStr = if (selection > -1) typedArray[selection] else null
        logDialogClosed("Option", selectionStr)
        return selectionStr
    }

    override fun showQuestion(text: String, allowCancel: Boolean): Int
    {
        logDialogShown("Question", "Question", text)
        val option = if (allowCancel) JOptionPane.YES_NO_CANCEL_OPTION else JOptionPane.YES_NO_OPTION
        val result = JOptionPane.showConfirmDialog(null, text, "Question", option, JOptionPane.QUESTION_MESSAGE)
        logDialogClosed("Question", result)
        return result
    }

    override fun showLoading(text: String)
    {
        logDialogShown("Loading", "", text)
        loadingDialog.showDialog(text)
    }

    override fun dismissLoading()
    {
        if (loadingDialog.isVisible)
        {
            logDialogClosed("Loading", null)
        }

        loadingDialog.dismissDialog()
    }

    override fun chooseDirectory(parent: Component?): File?
    {
        logDialogShown("File selector", "", "")

        val fc = JFileChooser()
        fc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        val option = fc.showDialog(parent, "Select")
        if (option != JFileChooser.APPROVE_OPTION)
        {
            return null
        }

        logDialogClosed("File selector", fc.selectedFile.absolutePath)

        return fc.selectedFile
    }

    private fun logDialogShown(type: String, title: String, message: String)
    {
        logger.info(CODE_DIALOG_SHOWN, "$type dialog shown: $message", KEY_DIALOG_TYPE to type, KEY_DIALOG_TITLE to title, KEY_DIALOG_MESSAGE to message)
    }

    private fun logDialogClosed(type: String, selection: Any?)
    {
        var message = "$type dialog closed"
        selection?.let { message += " - selected ${translateOption(it)}" }

        JOptionPane.NO_OPTION

        logger.info(CODE_DIALOG_CLOSED, message,
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