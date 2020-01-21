package burlton.dartzee.code.core.util

object DialogUtil
{
    private var dialogFactory: AbstractMessageDialogFactory = MessageDialogFactory()

    fun init(implementation: AbstractMessageDialogFactory)
    {
        dialogFactory = implementation
    }

    fun showInfo(infoText: String) = dialogFactory.showInfo(infoText)
    fun showError(errorText: String) = dialogFactory.showError(errorText)
    fun showErrorLater(errorText: String) = dialogFactory.showErrorLater(errorText)
    fun showQuestion(message: String, allowCancel: Boolean = false) = dialogFactory.showQuestion(message, allowCancel)
    fun showLoadingDialog(text: String) = dialogFactory.showLoading(text)
    fun dismissLoadingDialog() = dialogFactory.dismissLoading()
    fun <K> showInput(title: String, message: String, options: Array<K>?, defaultOption: K? = null) = dialogFactory.showInput(title, message, options, defaultOption)
}
