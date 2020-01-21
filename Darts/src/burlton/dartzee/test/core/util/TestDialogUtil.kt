package burlton.dartzee.test.core.util

import burlton.dartzee.code.core.util.DialogUtil
import burlton.dartzee.test.core.helper.AbstractTest
import burlton.dartzee.test.core.helper.TestMessageDialogFactory
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.Test

class TestDialogUtil: AbstractTest()
{
    var factoryMock = mockk<TestMessageDialogFactory>(relaxed = true)

    @Test
    fun `Should pass method calls on to implementation`()
    {
        DialogUtil.init(factoryMock)

        DialogUtil.showError("Test")
        DialogUtil.showInfo("Info")
        DialogUtil.showLoadingDialog("Loading...")
        DialogUtil.showQuestion("Q")
        DialogUtil.dismissLoadingDialog()
        DialogUtil.showErrorLater("Later error")

        verifySequence{
            factoryMock.showError("Test")
            factoryMock.showInfo("Info")
            factoryMock.showLoading("Loading...")
            factoryMock.showQuestion("Q", false)
            factoryMock.dismissLoading()
            factoryMock.showErrorLater("Later error")
        }
    }
}