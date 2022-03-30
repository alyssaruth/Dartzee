package dartzee.screen

import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
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
    fun `Should start with empty values`()
    {
        val dlg = HumanConfigurationDialog()
        dlg.textFieldName.text shouldBe ""
        dlg.avatar.avatarId shouldBe ""
    }

    @Test
    fun `Should save a human player`()
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
}