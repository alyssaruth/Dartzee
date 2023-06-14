package dartzee.screen

import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldMatch
import com.github.alyssaburlton.swingtest.shouldMatchImage
import dartzee.bean.PlayerImageRadio
import dartzee.clickOk
import dartzee.core.bean.FileUploader
import dartzee.core.helper.verifyNotCalled
import dartzee.core.util.getAllChildComponentsForType
import dartzee.db.PlayerImageEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayerImage
import dartzee.only
import dartzee.selectImage
import dartzee.selectTab
import dartzee.uploadFileFromResource
import dartzee.utils.PLAYER_IMAGE_HEIGHT
import dartzee.utils.PLAYER_IMAGE_WIDTH
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import javax.swing.JPanel
import javax.swing.JTabbedPane

class TestPlayerImageDialog : AbstractTest()
{
    @Test
    fun `Should populate correctly with pre-existing images`()
    {
        PlayerImageEntity().createPresets()
        insertPlayerImage("dibble")

        val dialog = PlayerImageDialog(makeCallback())
        val tabbedPane = dialog.getChild<JTabbedPane>()
        val presetTab = tabbedPane.getChild<JPanel>("presetTab")
        val uploadTab = tabbedPane.getChild<JPanel>("uploadTab")

        presetTab.getAllChildComponentsForType<PlayerImageRadio>().size shouldBe PlayerImageEntity.avatarPresets.size
        uploadTab.getAllChildComponentsForType<PlayerImageRadio>().size shouldBe 1
    }

    @Test
    fun `Should show an error if no selection is made`()
    {
        val callback = makeCallback()
        val dlg = PlayerImageDialog(callback)
        dlg.clickOk()

        dialogFactory.errorsShown.shouldContainExactly("You must select an image.")
        verifyNotCalled { callback(any()) }
    }

    @Test
    @Tag("screenshot")
    fun `Should accept and crop a valid new avatar`()
    {
        val dlg = PlayerImageDialog(makeCallback())
        dlg.isVisible = true
        dlg.getChild<JTabbedPane>().selectTab<JPanel>("uploadTab")
        dlg.uploadResource("/outer-wilds.jpeg")

        val uploadPanel = dlg.getChild<JPanel>("uploadTab")
        val radioButtons = uploadPanel.getAllChildComponentsForType<PlayerImageRadio>()
        val resultingRadioButton = radioButtons.only()
        resultingRadioButton.shouldMatchImage("uploadedImage", pixelTolerance = 0.05)

        val entities = PlayerImageEntity().retrieveEntities()
        val resultingEntity = entities.only()
        resultingEntity.preset shouldBe false
        resultingEntity.asImageIcon().shouldMatch(resultingRadioButton.lblImg.icon)
    }

    @Test
    fun `Should reject uploading a non-image file`()
    {
        val dlg = PlayerImageDialog(makeCallback())
        dlg.getChild<JTabbedPane>().selectTab<JPanel>("uploadTab")
        dlg.uploadResource("/aiModel.json")

        dialogFactory.errorsShown.shouldContainExactly("You must select a valid image file.")
        PlayerImageEntity().retrieveEntities().shouldBeEmpty()
    }

    @Test
    fun `Should reject uploading an image that is too small`()
    {
        val dlg = PlayerImageDialog(makeCallback())
        dlg.getChild<JTabbedPane>().selectTab<JPanel>("uploadTab")
        dlg.uploadResource("/stats_large.png")

        dialogFactory.errorsShown.shouldContainExactly("The image is too small - it must be at least $PLAYER_IMAGE_WIDTH x $PLAYER_IMAGE_HEIGHT px.")

        dlg.getAllChildComponentsForType<PlayerImageRadio>().shouldBeEmpty()
        PlayerImageEntity().retrieveEntities().shouldBeEmpty()
    }

    @Test
    fun `Should invoke callback when Ok is pressed with an image selected`()
    {
        val img = insertPlayerImage("dibble")
        val callback = makeCallback()
        val dlg = PlayerImageDialog(callback)
        dlg.selectImage(img.rowId)
        dlg.clickOk()

        verify { callback(img.rowId) }
    }

    private fun PlayerImageDialog.uploadResource(resourceName: String)
    {
        val uploader = getChild<FileUploader>()
        uploader.uploadFileFromResource(resourceName)
    }

    private fun makeCallback(): (String) -> Unit = mockk(relaxed = true)
}