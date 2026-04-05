package dartzee.screen

import com.github.lgooddatepicker.components.DatePicker
import dartzee.bean.PlayerAvatar
import dartzee.core.util.DialogUtil
import dartzee.core.util.toLocalDate
import dartzee.core.util.toTimestamp
import dartzee.db.PlayerEntity
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import net.miginfocom.swing.MigLayout

class PlayerDemographicsPanel(private val player: PlayerEntity) : JPanel() {
    val avatar = PlayerAvatar()
    private val datePicker = DatePicker()
    private val textFieldName = JTextField().also { it.name = "nameField" }

    init {
        layout = BorderLayout(0, 0)

        val avatarPanel = JPanel()
        avatarPanel.layout = MigLayout("al center center")
        avatarPanel.add(avatar)
        add(avatarPanel, BorderLayout.CENTER)

        val formPanel = JPanel()
        formPanel.layout = MigLayout("al center center")
        formPanel.add(JLabel("Name"), "cell 0 0")
        formPanel.add(textFieldName, "cell 1 0")
        formPanel.add(JLabel("Birthday"), "cell 0 1")
        formPanel.add(datePicker, "cell 1 1")
        textFieldName.columns = 10

        add(formPanel, BorderLayout.EAST)

        textFieldName.text = player.name
        avatar.init(player, false)
        avatar.readOnly = player.retrievedFromDb
        datePicker.date = player.dateOfBirth.toLocalDate()
    }

    fun getPlayerName(): String = textFieldName.text

    fun writeDetails() {
        val name = textFieldName.text
        player.name = name
        player.playerImageId = avatar.avatarId
        player.dateOfBirth = datePicker.date.toTimestamp()
    }

    fun valid(): Boolean {
        val name = textFieldName.text
        if (!isValidName(name)) {
            return false
        }

        val avatarId = avatar.avatarId
        if (avatarId.isEmpty()) {
            DialogUtil.showErrorOLD("You must select an avatar.")
            return false
        }

        return true
    }

    private fun isValidName(name: String?): Boolean {
        if (name.isNullOrEmpty()) {
            DialogUtil.showErrorOLD("You must enter a name for this player.")
            return false
        }

        val length = name.length
        if (length < 3) {
            DialogUtil.showErrorOLD("The player name must be at least 3 characters long.")
            return false
        }

        if (length > 25) {
            DialogUtil.showErrorOLD("The player name cannot be more than 25 characters long.")
            return false
        }

        val existingPlayer = PlayerEntity.retrieveForName(name)
        if (existingPlayer != null && existingPlayer.rowId != player.rowId) {
            DialogUtil.showErrorOLD("A player with the name $name already exists.")
            return false
        }

        return true
    }
}
