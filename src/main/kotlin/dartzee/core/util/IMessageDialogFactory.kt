package dartzee.core.util

interface IMessageDialogFactory {
    fun showOption(title: String, message: String, options: List<String>): String?

    fun <K> showInput(title: String, message: String, options: Array<K>?, defaultOption: K?): K?
}
