package dartzee.core.helper

import dartzee.core.util.AbstractMessageDialogFactory
import java.awt.Component
import java.io.File
import javax.swing.JOptionPane

class TestMessageDialogFactory: AbstractMessageDialogFactory()
{
    //Directory
    var directoryToSelect: File? = null

    //Inputs
    var inputSelection: Any? = null
    var inputOptionsPresented: Array<*>? = null
    val inputsShown = mutableListOf<String>()

    //Questions
    var questionOption = JOptionPane.NO_OPTION
    val questionsShown = mutableListOf<String>()

    val optionSequence = mutableListOf<String?>()
    val optionsShown = mutableListOf<String>()

    var loadingVisible = false

    val infosShown = mutableListOf<String>()
    val errorsShown = mutableListOf<String>()
    val customsShown = mutableListOf<Any>()

    val loadingsShown = mutableListOf<String>()

    override fun <K> showInput(title: String, message: String, options: Array<K>?, defaultOption: K?): K?
    {
        inputsShown.add(title)
        inputOptionsPresented = options

        val selection = inputSelection
        selection ?: return null

        if (options == null || options.contains(inputSelection))
        {
            @Suppress("UNCHECKED_CAST")
            return inputSelection as K
        }

        throw Exception("Running a test where $inputSelection was to be returned, but wasn't a valid selection in the dialog shown.")
    }

    override fun showInfo(text: String)
    {
        infosShown.add(text)
    }

    override fun showError(text: String)
    {
        errorsShown.add(text)
    }

    override fun showCustomMessage(messageBody: Any)
    {
        customsShown.add(messageBody)
    }

    override fun showQuestion(text: String, allowCancel: Boolean): Int
    {
        questionsShown.add(text)
        return questionOption
    }

    override fun showOption(title: String, message: String, options: List<String>): String?
    {
        optionsShown.add(message)
        val selection = optionSequence.removeAt(0)
        return selection
    }

    override fun showLoading(text: String)
    {
        loadingsShown.add(text)
        loadingVisible = true
    }

    override fun dismissLoading(): Boolean
    {
        val wasVisible = loadingVisible
        loadingVisible = false
        return wasVisible
    }

    override fun chooseDirectory(parent: Component?) = directoryToSelect

    fun reset()
    {
        inputsShown.clear()
        inputOptionsPresented = arrayOf<Any>()
        infosShown.clear()
        errorsShown.clear()
        questionsShown.clear()
        loadingsShown.clear()
        loadingVisible = false
        optionSequence.clear()
        optionsShown.clear()
        directoryToSelect = null
        customsShown.clear()
    }
}