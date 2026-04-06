package dartzee.screen

import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import com.github.lgooddatepicker.components.DatePicker
import dartzee.bean.PlayerAvatar
import dartzee.core.util.DateStatics
import dartzee.db.PlayerEntity
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.runAsync
import dartzee.shouldMatch
import io.kotest.matchers.shouldBe
import java.sql.Timestamp
import java.time.LocalDate
import javax.swing.JTextField
import org.junit.jupiter.api.Test

class PlayerDemographicsPanelTest : AbstractTest() {
    @Test
    fun `Should initialise empty for a new player`() {
        val panel = PlayerDemographicsPanel(PlayerEntity())

        panel.getChild<JTextField>("nameField").text shouldBe ""
        panel.getChild<PlayerAvatar>().icon.shouldMatch("/avatars/Unset.png")
        panel.getChild<PlayerAvatar>().readOnly shouldBe false
        panel.getChild<DatePicker>().date shouldBe null
    }

    @Test
    fun `Should initialise to values for an existing player`() {
        val playerImage = insertPlayerImage(resource = "BaboTwo")
        val player =
            insertPlayer(
                name = "Sir Digby",
                playerImageId = playerImage.rowId,
                dateOfBirth = Timestamp.valueOf("1993-03-05 00:00:00"),
            )

        val panel = PlayerDemographicsPanel(player)
        panel.getChild<JTextField>("nameField").text shouldBe "Sir Digby"
        panel.getChild<PlayerAvatar>().icon.shouldMatch("/avatars/BaboTwo.png")
        panel.getChild<PlayerAvatar>().readOnly shouldBe true
        panel.getChild<DatePicker>().date shouldBe LocalDate.of(1993, 3, 5)
    }

    @Test
    fun `Should not allow an empty player name, and should not call save with a validation error`() {
        val panel = PlayerDemographicsPanel(PlayerEntity())
        panel.assertValidationError("You must enter a name for this player.")
    }

    @Test
    fun `Should not allow a name with fewer than 3 characters`() {
        val panel = PlayerDemographicsPanel(insertPlayer())
        panel.getChild<JTextField>("nameField").text = "AA"

        panel.assertValidationError("The player name must be at least 3 characters long.")
    }

    @Test
    fun `Should not allow a name with more than 25 characters`() {
        val panel = PlayerDemographicsPanel(insertPlayer())
        panel.getChild<JTextField>("nameField").text = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        panel.assertValidationError("The player name cannot be more than 25 characters long.")
    }

    @Test
    fun `Should not allow creation of a player that already exists`() {
        insertPlayer(name = "Barry")

        val panel = PlayerDemographicsPanel(insertPlayer())
        panel.getChild<JTextField>("nameField").text = "Barry"

        panel.assertValidationError("A player with the name Barry already exists.")
    }

    @Test
    fun `Should not allow a player with no avatar`() {
        val panel = PlayerDemographicsPanel(PlayerEntity())
        panel.getChild<JTextField>("nameField").text = "Derek"

        panel.assertValidationError("You must select an avatar.")
    }

    private fun PlayerDemographicsPanel.assertValidationError(expectedMessage: String) {
        var result = true
        runAsync { result = valid() }

        val error = getErrorDialog()
        error.getDialogMessage() shouldBe expectedMessage
        error.clickOk()
        flushEdt()

        result shouldBe false
    }

    @Test
    fun `Should pass validation if name and avatar are valid`() {
        val img = insertPlayerImage()

        val panel = PlayerDemographicsPanel(PlayerEntity())
        panel.getChild<JTextField>("nameField").text = "Derek"
        panel.getChild<PlayerAvatar>().avatarId = img.rowId
        panel.valid() shouldBe true
    }

    @Test
    fun `Should write details to the player`() {
        val player = PlayerEntity()
        val img = insertPlayerImage()

        val panel = PlayerDemographicsPanel(player)
        panel.getChild<JTextField>("nameField").text = "Derek"
        panel.getChild<PlayerAvatar>().avatarId = img.rowId
        panel.getChild<DatePicker>().date = LocalDate.of(1994, 6, 18)

        panel.writeDetails()

        player.name shouldBe "Derek"
        player.playerImageId shouldBe img.rowId
        player.dateOfBirth shouldBe Timestamp.valueOf("1994-06-18 00:00:00")
    }

    @Test
    fun `Should write end of time if date of birth is left null`() {
        val player = PlayerEntity()
        val img = insertPlayerImage()

        val panel = PlayerDemographicsPanel(player)
        panel.getChild<JTextField>("nameField").text = "Derek"
        panel.getChild<PlayerAvatar>().avatarId = img.rowId
        panel.getChild<DatePicker>().date = null

        panel.writeDetails()

        player.dateOfBirth shouldBe DateStatics.END_OF_TIME
    }
}
