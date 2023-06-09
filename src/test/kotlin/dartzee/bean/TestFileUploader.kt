package dartzee.bean

import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import dartzee.awaitWindow
import dartzee.clickButton
import dartzee.clickCancel
import dartzee.core.bean.FileUploader
import dartzee.core.bean.IFileUploadListener
import dartzee.core.helper.verifyNotCalled
import dartzee.helper.AbstractTest
import dartzee.typeText
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.io.File
import javax.swing.JComboBox
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
        val listener = makeFileListener(true)
        uploader.addFileUploadListener(listener)

        uploader.clickButton("Upload")
        verifyNotCalled { listener.fileUploaded(any()) }
        dialogFactory.errorsShown.shouldContainExactly("You must select a file to upload.")
    }

    @Test
    fun `Should not pick a file if cancelled`()
    {
        val uploader = FileUploader(FileNameExtensionFilter("all", "*"))
        val listener = makeFileListener(true)
        uploader.addFileUploadListener(listener)

        uploader.clickButton("...", async = true)

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
        val listener = makeFileListener(true)
        uploader.addFileUploadListener(listener)

        uploader.clickButton("...", async = true)

        val chooserDialog = awaitFileChooser()
        chooserDialog.getChild<JTextComponent>().typeText(path)

        chooserDialog.clickButton("Open")
        flushEdt()

        uploader.getChild<JTextField>().text shouldBe path
        uploader.clickButton("Upload")
        flushEdt()

        uploader.getChild<JTextField>().text shouldBe ""
        verify { listener.fileUploaded(File(path)) }
    }

    @Test
    fun `Should not clear text if file listener reports failure`()
    {
        val rsrc = javaClass.getResource("/outer-wilds.jpeg")!!
        val path = rsrc.path

        val uploader = FileUploader(FileNameExtensionFilter("all", "*"))
        val listener = makeFileListener(false)
        uploader.addFileUploadListener(listener)

        uploader.clickButton("...", async = true)

        val chooserDialog = awaitFileChooser()
        chooserDialog.getChild<JTextComponent>().typeText(path)
        chooserDialog.clickButton("Open")
        flushEdt()

        uploader.getChild<JTextField>().text shouldBe path
        uploader.clickButton("Upload")
        flushEdt()

        uploader.getChild<JTextField>().text shouldBe path
        verify { listener.fileUploaded(File(path)) }
    }

    @Test
    fun `Should respect the file filter passed in`()
    {
        val filter = FileNameExtensionFilter("all", "*")
        val uploader = FileUploader(filter)
        val listener = makeFileListener(false)
        uploader.addFileUploadListener(listener)

        uploader.clickButton("...", async = true)

        val chooserDialog = awaitFileChooser()
        val combo = chooserDialog.getChild<JComboBox<FileNameExtensionFilter>> { it.selectedItem is FileNameExtensionFilter }
        combo.selectedItem.shouldBe(filter)
        combo.itemCount shouldBe 1
    }

    private fun makeFileListener(success: Boolean = true): IFileUploadListener {
        val listener = mockk<IFileUploadListener>(relaxed = true)
        every { listener.fileUploaded(any()) } returns success
        return listener
    }

    private fun awaitFileChooser() = awaitWindow<JDialog> { it.title == "Open" }
}