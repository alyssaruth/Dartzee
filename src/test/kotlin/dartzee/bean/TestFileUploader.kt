package dartzee.bean

import com.github.alyssaburlton.swingtest.awaitCondition
import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import dartzee.clickCancel
import dartzee.core.bean.FileUploader
import dartzee.core.bean.IFileUploadListener
import dartzee.core.helper.verifyNotCalled
import dartzee.core.util.runOnEventThread
import dartzee.core.util.runOnEventThreadBlocking
import dartzee.findWindow
import dartzee.helper.AbstractTest
import dartzee.typeText
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.io.File
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JTextField
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.text.JTextComponent

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

    @Test
    fun `Should not pick a file if cancelled`()
    {
        val uploader = FileUploader(FileNameExtensionFilter("all", "*"))
        val listener = mockk<IFileUploadListener>(relaxed = true)
        uploader.addFileUploadListener(listener)

        runOnEventThread { uploader.clickChild<JButton>(text = "...") }

        val chooserDialog = awaitFileChooser()
        chooserDialog.clickCancel()

        uploader.getChild<JTextField>().text shouldBe ""
        verifyNotCalled { listener.fileUploaded(any()) }
    }

    @Test
    fun `Should be able to upload a file`()
    {
        val rsrc = javaClass.getResource("/outer-wilds.jpeg")!!
        val path = rsrc.path

        val uploader = FileUploader(FileNameExtensionFilter("all", "*"))
        val listener = mockk<IFileUploadListener>(relaxed = true)
        every { listener.fileUploaded(any()) } returns true
        uploader.addFileUploadListener(listener)

        runOnEventThread { uploader.clickChild<JButton>(text = "...") }

        val chooserDialog = awaitFileChooser()
        chooserDialog.getChild<JTextComponent>().typeText(path)

        runOnEventThreadBlocking { chooserDialog.clickChild<JButton>(text = "Open") }
        flushEdt()

        uploader.getChild<JTextField>().text shouldBe path
        runOnEventThreadBlocking { uploader.clickChild<JButton>(text = "Upload") }
        flushEdt()

        uploader.getChild<JTextField>().text shouldBe ""
        verify { listener.fileUploaded(File(path)) }
    }

    private fun awaitFileChooser(): JDialog
    {
        awaitCondition { findWindow<JDialog> { it.title == "Open" } != null }
        return findWindow<JDialog> { it.title == "Open" }!!
    }
}