package dartzee.core.util

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
}
