package dartzee.core.helper

import dartzee.core.util.AbstractMessageDialogFactory
import javax.swing.JOptionPane

class TestMessageDialogFactory: AbstractMessageDialogFactory()
{
    //Inputs
    var inputSelection: Any? = null
    var inputOptionsPresented: Array<*>? = null
    val inputsShown = mutableListOf<String>()

    //Questions
    var questionOption = JOptionPane.NO_OPTION
    val questionsShown = mutableListOf<String>()

    var loadingVisible = false

    val infosShown = mutableListOf<String>()
    val errorsShown = mutableListOf<String>()

    val loadingsShown = mutableListOf<String>()

    override fun <K> showInput(title: String, message: String, options: Array<K>?, defaultOption: K?): K?
    {
        inputsShown.add(title)
        inputOptionsPresented = options

        inputSelection ?: return null

        if (options == null || options.contains(inputSelection))
        {
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

    override fun showQuestion(text: String, allowCancel: Boolean): Int
    {
        questionsShown.add(text)
        return questionOption
    }

    override fun showLoading(text: String)
    {
        loadingsShown.add(text)
        loadingVisible = true
    }

    override fun dismissLoading()
    {
        loadingVisible = false
    }

    fun reset()
    {
        inputsShown.clear()
        inputOptionsPresented = arrayOf<Any>()
        infosShown.clear()
        errorsShown.clear()
        questionsShown.clear()
        loadingsShown.clear()
        loadingVisible = false
    }

}