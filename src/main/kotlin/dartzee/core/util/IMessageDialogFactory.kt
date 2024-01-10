package dartzee.core.util

import java.awt.Component
import java.io.File

interface IMessageDialogFactory {
    fun showInfo(text: String)

    fun showError(text: String)

    fun showQuestion(text: String, allowCancel: Boolean = false): Int

    fun showOption(title: String, message: String, options: List<String>): String?

    fun showLoading(text: String)

    fun <K> showInput(title: String, message: String, options: Array<K>?, defaultOption: K?): K?

    fun dismissLoading(): Boolean

    fun chooseDirectory(parent: Component?): File?
}
