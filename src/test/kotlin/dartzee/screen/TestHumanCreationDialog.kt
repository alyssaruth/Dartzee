package dartzee.screen

import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
import dartzee.helper.randomGuid
import io.kotlintest.shouldBe
import org.junit.Test

class TestHumanCreationDialog: AbstractTest()
{
    @Test
    fun `Should be modal and non-resizable`()
    {
        val dlg = HumanCreationDialog()
        dlg.isModal shouldBe true
        dlg.isResizable shouldBe false
    }

    @Test
    fun `Should start with empty values`()
    {
        val dlg = HumanCreationDialog()
        dlg.textFieldName.text shouldBe ""
        dlg.createdPlayer shouldBe false
        dlg.avatar.avatarId shouldBe ""
    }

    @Test
    fun `Should save a human player`()
    {
        val avatarId = randomGuid()

        val dlg = HumanCreationDialog()
        dlg.textFieldName.text = "Barry"
        dlg.avatar.avatarId = avatarId
        dlg.btnOk.doClick()


        dlg.createdPlayer shouldBe true

        val player = PlayerEntity.retrieveForName("Barry")!!
        player.playerImageId shouldBe avatarId
        player.strategy shouldBe -1
        player.strategyXml shouldBe ""
    }
}