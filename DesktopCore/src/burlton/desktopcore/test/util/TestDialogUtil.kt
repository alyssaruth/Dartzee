package burlton.desktopcore.test.util

import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.test.helpers.AbstractDesktopTest
import burlton.desktopcore.test.helpers.TestMessageDialogFactory
import io.kotlintest.shouldThrow
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.Test

class TestDialogUtil: AbstractDesktopTest()
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