package dartzee.screen

import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.helper.randomGuid
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestHumanConfigurationDialog: AbstractTest()
{
    @Test
    fun `Should be modal and non-resizable`()
    {
        val dlg = HumanConfigurationDialog()
        dlg.isModal shouldBe true
        dlg.isResizable shouldBe false
    }

    @Test
    fun `Should start with correct state for new player`()
    {
        val dlg = HumanConfigurationDialog()
        dlg.textFieldName.text shouldBe ""
        dlg.avatar.avatarId shouldBe ""
        dlg.avatar.readOnly shouldBe false
        dlg.title shouldBe "New Player"
    }

    @Test
    fun `Should start with correct state for amending a player`()
    {
        val avatar = insertPlayerImage()
        val player = insertPlayer(name = "Bongo", playerImageId = avatar.rowId)

        val dlg = HumanConfigurationDialog(player)
        dlg.textFieldName.text shouldBe "Bongo"
        dlg.avatar.avatarId shouldBe avatar.rowId
        dlg.avatar.readOnly shouldBe true
        dlg.title shouldBe "Amend Player"
    }

    @Test
    fun `Should save a new player`()
    {
        val avatarId = randomGuid()

        val dlg = HumanConfigurationDialog()
        dlg.textFieldName.text = "Barry"
        dlg.avatar.avatarId = avatarId
        dlg.btnOk.doClick()

        val player = PlayerEntity.retrieveForName("Barry")!!
        player.playerImageId shouldBe avatarId
        player.strategy shouldBe ""
    }

    @Test
    fun `Should save changes to an existing player`()
    {
        val oldAvatar = insertPlayerImage()
        val newAvatar = insertPlayerImage()
        val player = insertPlayer(name = "Alex", playerImageId = oldAvatar.rowId)

        val dlg = HumanConfigurationDialog(player)
        dlg.textFieldName.text = "Alyssa"
        dlg.avatar.avatarId = newAvatar.rowId
        dlg.btnOk.doClick()

        val updatedPlayer = PlayerEntity().retrieveForId(player.rowId)!!
        updatedPlayer.name shouldBe "Alyssa"
        updatedPlayer.playerImageId shouldBe newAvatar.rowId
    }
}