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
}
