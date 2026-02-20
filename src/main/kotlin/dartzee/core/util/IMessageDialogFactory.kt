package dartzee.core.util

import java.awt.Component
import java.io.File

interface IMessageDialogFactory {
    fun showError(text: String)

    fun showQuestion(text: String, allowCancel: Boolean = false): Int

    fun showOption(title: String, message: String, options: List<String>): String?

    fun <K> showInput(title: String, message: String, options: Array<K>?, defaultOption: K?): K?

    fun chooseDirectory(parent: Component?): File?
}
