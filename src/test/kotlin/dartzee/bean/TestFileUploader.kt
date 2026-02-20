package dartzee.bean

import com.github.alyssaburlton.swingtest.clickCancel
import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.purgeWindows
import dartzee.core.bean.FileUploader
import dartzee.core.bean.IFileUploadListener
import dartzee.core.bean.selectedItemTyped
import dartzee.core.helper.verifyNotCalled
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.getFileChooser
import dartzee.helper.AbstractTest
import dartzee.preferences.Preferences
import dartzee.uploadFileFromResource
import dartzee.utils.InjectedThings
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JTextField
import javax.swing.filechooser.FileNameExtensionFilter
import org.junit.jupiter.api.Test

class TestFileUploader : AbstractTest() {
    @Test
    fun `Should error if upload is pressed with no file`() {
        val uploader = FileUploader(FileNameExtensionFilter("all", "*"))
        val listener = makeFileListener(true)
        uploader.addFileUploadListener(listener)
        uploader.clickChild<JButton>(text = "Upload", async = true)

        val dlg = getErrorDialog()
        dlg.getDialogMessage() shouldBe "You must select a file to upload."
        dlg.clickOk()

        verifyNotCalled { listener.fileUploaded(any()) }
    }

    @Test
    fun `Should not pick a file or update directory preference if cancelled`() {
        val uploader = FileUploader(FileNameExtensionFilter("all", "*"))
        val originalPath = uploader.getChild<JTextField>().text
        val listener = makeFileListener(true)
        uploader.addFileUploadListener(listener)

        uploader.clickChild<JButton>(text = "...", async = true)

        val chooserDialog = getFileChooser()
        chooserDialog.clickCancel()

        InjectedThings.preferenceService.find(Preferences.imageUploadDirectory) shouldBe null
        uploader.getChild<JTextField>().text shouldBe originalPath
        verifyNotCalled { listener.fileUploaded(any()) }
    }

    @Test
    fun `Should be able to upload a file`() {
        val rsrc = javaClass.getResource("/outer-wilds.jpeg")!!
        val path = File(rsrc.path).path

        val uploader = FileUploader(FileNameExtensionFilter("all", "*"))
        val listener = makeFileListener(true)
        uploader.addFileUploadListener(listener)

        uploader.uploadFileFromResource("/outer-wilds.jpeg")
        uploader.getChild<JTextField>().text shouldBe File(rsrc.path).absoluteFile.parent
        verify { listener.fileUploaded(File(path)) }
    }

    @Test
    fun `Should not update text if file listener reports failure`() {
        val rsrc = javaClass.getResource("/outer-wilds.jpeg")!!
        val path = File(rsrc.path).path

        val uploader = FileUploader(FileNameExtensionFilter("all", "*"))
        val listener = makeFileListener(false)
        uploader.addFileUploadListener(listener)

        uploader.uploadFileFromResource("/outer-wilds.jpeg")
        uploader.getChild<JTextField>().text shouldBe path
        verify { listener.fileUploaded(File(path)) }
    }

    @Test
    fun `Should respect the file filter passed in`() {
        val filter = FileNameExtensionFilter("all", "*")
        val uploader = FileUploader(filter)
        val listener = makeFileListener(false)
        uploader.addFileUploadListener(listener)

        uploader.clickChild<JButton>(text = "...", async = true)

        val chooserDialog = getFileChooser()
        val combo =
            chooserDialog.getChild<JComboBox<FileNameExtensionFilter>> {
                it.selectedItem is FileNameExtensionFilter
            }
        combo.selectedItem.shouldBe(filter)
        combo.itemCount shouldBe 1
    }

    @Test
    fun `Should store last uploaded directory and use it going forwards`() {
        val rsrc = javaClass.getResource("/outer-wilds.jpeg")!!
        val rsrcDirectory = File(rsrc.path).absoluteFile.parent

        val uploaderOne = FileUploader(FileNameExtensionFilter("all", "*"))
        uploaderOne.getChild<JTextField>().text shouldNotBe rsrcDirectory

        uploaderOne.uploadFileFromResource("/outer-wilds.jpeg")
        uploaderOne.getChild<JTextField>().text shouldBe File(rsrc.path).absoluteFile.parent
        purgeWindows()

        InjectedThings.preferenceService.get(Preferences.imageUploadDirectory) shouldBe
            rsrcDirectory

        val uploaderTwo = FileUploader(FileNameExtensionFilter("all", "*"))
        uploaderTwo.getChild<JTextField>().text shouldBe rsrcDirectory

        uploaderTwo.clickChild<JButton>(text = "...", async = true)
        val chooser = getFileChooser()
        val combo = chooser.getChild<JComboBox<File>> { it.selectedItem is File }
        combo.selectedItemTyped() shouldBe File(rsrcDirectory)
    }

    private fun makeFileListener(success: Boolean = true): IFileUploadListener {
        val listener = mockk<IFileUploadListener>(relaxed = true)
        every { listener.fileUploaded(any()) } returns success
        return listener
    }
}
