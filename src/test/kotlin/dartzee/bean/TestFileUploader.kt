package dartzee.bean

import com.github.alyssaburlton.swingtest.clickChild
import dartzee.core.bean.FileUploader
import dartzee.core.bean.IFileUploadListener
import dartzee.core.helper.verifyNotCalled
import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.mockk
import org.junit.jupiter.api.Test
import javax.swing.JButton
import javax.swing.filechooser.FileNameExtensionFilter

class TestFileUploader : AbstractTest()
{
    @Test
    fun `Should error if upload is pressed with no file`()
    {
        val uploader = FileUploader(FileNameExtensionFilter("all", "*"))
        val listener = mockk<IFileUploadListener>(relaxed = true)
        uploader.addFileUploadListener(listener)

        uploader.clickChild<JButton>(text = "Upload")
        verifyNotCalled { listener.fileUploaded(any()) }
        dialogFactory.errorsShown.shouldContainExactly("You must select a file to upload.")
    }
}