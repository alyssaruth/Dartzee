package dartzee.core.helper

import dartzee.core.util.IMessageDialogFactory

class TestMessageDialogFactory : IMessageDialogFactory {
    // Inputs
    var inputSelection: Any? = null
    var inputOptionsPresented: Array<*>? = null
    val inputsShown = mutableListOf<String>()

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

    fun reset() {
        inputsShown.clear()
        inputOptionsPresented = arrayOf<Any>()
    }
}
