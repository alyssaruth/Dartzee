package dartzee.core.helper

import dartzee.core.util.IMessageDialogFactory
import java.awt.Component
import java.io.File

class TestMessageDialogFactory : IMessageDialogFactory {
    // Directory
    var directoryToSelect: File? = null

    // Inputs
    var inputSelection: Any? = null
    var inputOptionsPresented: Array<*>? = null
    val inputsShown = mutableListOf<String>()

    val optionSequence = mutableListOf<String?>()
    val optionsShown = mutableListOf<String>()

    override fun <K> showInput(
        title: String,
        message: String,
        options: Array<K>?,
        defaultOption: K?,
    ): K? {
        inputsShown.add(title)
        inputOptionsPresented = options

        val selection = inputSelection
        selection ?: return null

        if (options == null || options.contains(inputSelection)) {
            @Suppress("UNCHECKED_CAST")
            return inputSelection as K
        }

        throw Exception(
            "Running a test where $inputSelection was to be returned, but wasn't a valid selection in the dialog shown."
        )
    }

    override fun showOption(title: String, message: String, options: List<String>): String? {
        optionsShown.add(message)
        val selection = optionSequence.removeAt(0)
        return selection
    }

    override fun chooseDirectory(parent: Component?) = directoryToSelect

    fun reset() {
        inputsShown.clear()
        inputOptionsPresented = arrayOf<Any>()
        optionSequence.clear()
        optionsShown.clear()
        directoryToSelect = null
    }
}
