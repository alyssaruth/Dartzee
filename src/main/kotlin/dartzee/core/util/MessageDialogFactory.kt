package dartzee.core.util

import java.awt.Component
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane

class MessageDialogFactory : IMessageDialogFactory {
    @Suppress("UNCHECKED_CAST")
    override fun <K> showInput(
        title: String,
        message: String,
        options: Array<K>?,
        defaultOption: K?,
    ): K? =
        JOptionPane.showInputDialog(
            null,
            message,
            title,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            defaultOption,
        ) as K?

    override fun showError(text: String) {
        JOptionPane.showMessageDialog(null, text, "Error", JOptionPane.ERROR_MESSAGE)
    }

    override fun showOption(title: String, message: String, options: List<String>): String? {
        val typedArray = options.toTypedArray()
        val selection =
            JOptionPane.showOptionDialog(
                null,
                message,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                typedArray,
                options.first(),
            )
        return if (selection > -1) typedArray[selection] else null
    }

    override fun showQuestion(text: String, allowCancel: Boolean): Int {
        val option =
            if (allowCancel) JOptionPane.YES_NO_CANCEL_OPTION else JOptionPane.YES_NO_OPTION
        return JOptionPane.showConfirmDialog(
            null,
            text,
            "Question",
            option,
            JOptionPane.QUESTION_MESSAGE,
        )
    }

    override fun chooseDirectory(parent: Component?): File? {
        val fc = JFileChooser()
        fc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        val option = fc.showDialog(parent, "Select")
        if (option != JFileChooser.APPROVE_OPTION) {
            return null
        }

        return fc.selectedFile
    }
}
