package dartzee.screen

import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.getChild
import dartzee.bean.PlayerAvatar
import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.helper.randomGuid
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import javax.swing.JTextField
import org.junit.jupiter.api.Test

class TestHumanConfigurationDialog : AbstractTest() {
    @Test
    fun `Should start with correct state for new player`() {
        val dlg = HumanConfigurationDialog(mockk())
        dlg.getChild<JTextField>("nameField").text shouldBe ""
        dlg.getChild<PlayerAvatar>().avatarId shouldBe ""
        dlg.getChild<PlayerAvatar>().readOnly shouldBe false
        dlg.title shouldBe "New Player"
    }

    @Test
    fun `Should start with correct state for amending a player`() {
        val avatar = insertPlayerImage()
        val player = insertPlayer(name = "Bongo", playerImageId = avatar.rowId)

        val dlg = HumanConfigurationDialog(mockk(), player)
        dlg.getChild<JTextField>("nameField").text shouldBe "Bongo"
        dlg.getChild<PlayerAvatar>().avatarId shouldBe avatar.rowId
        dlg.getChild<PlayerAvatar>().readOnly shouldBe true
        dlg.title shouldBe "Amend Player"
    }

    @Test
    fun `Should save a new player`() {
        val avatarId = randomGuid()

        val dlg = HumanConfigurationDialog(mockCallback())
        dlg.getChild<JTextField>("nameField").text = "Barry"
        dlg.getChild<PlayerAvatar>().avatarId = avatarId
        dlg.clickOk()

        val player = PlayerEntity.retrieveForName("Barry")!!
        player.playerImageId shouldBe avatarId
        player.strategy shouldBe ""
    }

    @Test
    fun `Should save changes to an existing player`() {
        val oldAvatar = insertPlayerImage()
        val newAvatar = insertPlayerImage()
        val player = insertPlayer(name = "Alex", playerImageId = oldAvatar.rowId)

        val callback = mockCallback()
        val dlg = HumanConfigurationDialog(callback, player)
        dlg.getChild<JTextField>("nameField").text = "Alyssa"
        dlg.getChild<PlayerAvatar>().avatarId = newAvatar.rowId
        dlg.clickOk()

        val updatedPlayer = PlayerEntity().retrieveForId(player.rowId)!!
        updatedPlayer.name shouldBe "Alyssa"
        updatedPlayer.playerImageId shouldBe newAvatar.rowId

        verify { callback(player) }
    }

    private fun mockCallback() = mockk<(player: PlayerEntity) -> Unit>(relaxed = true)
}
