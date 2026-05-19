package dartzee.core.util

interface IMessageDialogFactory {
    fun <K> showInput(title: String, message: String, options: Array<K>?, defaultOption: K?): K?
}
