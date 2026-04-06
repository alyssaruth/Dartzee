package dartzee.screen

import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import com.github.lgooddatepicker.components.DatePicker
import dartzee.bean.PlayerAvatar
import dartzee.core.helper.verifyNotCalled
import dartzee.db.EntityName
import dartzee.db.PlayerEntity
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.helper.randomGuid
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import java.sql.Timestamp
import java.time.LocalDate
import javax.swing.JTextField
import org.junit.jupiter.api.Test

class HumanConfigurationDialogTest : AbstractTest() {
    @Test
    fun `Should start with correct state for new player`() {
        val dlg = HumanConfigurationDialog(mockk())
        dlg.getChild<JTextField>("nameField").text shouldBe ""
        dlg.title shouldBe "New Player"
    }

    @Test
    fun `Should start with correct state for amending a player`() {
        val player = insertPlayer(name = "Bongo")

        val dlg = HumanConfigurationDialog(mockk(), player)
        dlg.getChild<JTextField>("nameField").text shouldBe "Bongo"
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
    fun `Should not save changes if there is a validation error`() {
        val callback = mockCallback()
        val player = PlayerEntity.factoryCreate()

        val dlg = HumanConfigurationDialog(callback, player)
        dlg.getChild<JTextField>("nameField").text = "Barry"
        dlg.clickOk(async = true)

        val error = getErrorDialog()
        error.getDialogMessage() shouldBe "You must select an avatar."
        error.clickOk()
        flushEdt()

        verifyNotCalled { callback(any()) }
        player.name shouldBe ""
        getCountFromTable(EntityName.Player) shouldBe 0
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
        dlg.getChild<DatePicker>().date = LocalDate.of(1992, 2, 18)
        dlg.clickOk()

        val updatedPlayer = PlayerEntity().retrieveForId(player.rowId)!!
        updatedPlayer.name shouldBe "Alyssa"
        updatedPlayer.playerImageId shouldBe newAvatar.rowId
        updatedPlayer.dateOfBirth shouldBe Timestamp.valueOf("1992-02-18 00:00:00")

        verify { callback(player) }
    }

    private fun mockCallback() = mockk<(player: PlayerEntity) -> Unit>(relaxed = true)
}
