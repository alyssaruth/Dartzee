package burlton.desktopcore.code.util

object DialogUtil
{
    private var dialogFactory: AbstractMessageDialogFactory? = null

    fun init(implementation: AbstractMessageDialogFactory?)
    {
        dialogFactory = implementation
    }

    @JvmStatic fun showInfo(infoText: String) = dialogFactory!!.showInfo(infoText)
    @JvmStatic fun showError(errorText: String) = dialogFactory!!.showError(errorText)
    @JvmStatic fun showErrorLater(errorText: String) = dialogFactory!!.showErrorLater(errorText)
    @JvmStatic fun showQuestion(message: String, allowCancel: Boolean = false) = dialogFactory!!.showQuestion(message, allowCancel)
    @JvmStatic fun showLoadingDialog(text: String) = dialogFactory!!.showLoading(text)
    @JvmStatic fun dismissLoadingDialog() = dialogFactory!!.dismissLoading()
    @JvmStatic fun showInput(title: String, message: String, options: Array<*>, defaultOption: Any? = null) = dialogFactory!!.showInput(title, message, options, defaultOption)
}
