package burlton.desktopcore.test.helpers

import burlton.desktopcore.code.util.AbstractMessageDialogFactory
import javax.swing.JOptionPane

class TestMessageDialogFactory: AbstractMessageDialogFactory()
{
    var questionOption = JOptionPane.NO_OPTION
    var loadingVisible = false

    val infosShown = mutableListOf<String>()
    val errorsShown = mutableListOf<String>()
    val questionsShown = mutableListOf<String>()

    val loadingsShown = mutableListOf<String>()

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

}