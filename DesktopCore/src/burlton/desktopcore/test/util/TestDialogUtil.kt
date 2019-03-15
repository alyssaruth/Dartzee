package burlton.desktopcore.test.util

import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.test.helpers.TestMessageDialogFactory
import io.kotlintest.shouldThrow
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.Test

class TestDialogUtil
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

    @Test
    fun `Should throw errors if factory is unset`()
    {
        DialogUtil.init(null)

        shouldThrow<KotlinNullPointerException>{
            DialogUtil.showError("")
        }

        shouldThrow<KotlinNullPointerException>{
            DialogUtil.showInfo("")
        }

        shouldThrow<KotlinNullPointerException>{
            DialogUtil.showErrorLater("")
        }

        shouldThrow<KotlinNullPointerException>{
            DialogUtil.showQuestion("")
        }

        shouldThrow<KotlinNullPointerException>{
            DialogUtil.showLoadingDialog("")
        }

        shouldThrow<KotlinNullPointerException>{
            DialogUtil.dismissLoadingDialog()
        }
    }
}