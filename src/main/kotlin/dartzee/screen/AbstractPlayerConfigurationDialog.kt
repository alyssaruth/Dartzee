package dartzee.screen

import dartzee.bean.PlayerAvatar
import dartzee.core.screen.SimpleDialog
import dartzee.core.util.DialogUtil
import dartzee.db.PlayerEntity
import javax.swing.JTextField

abstract class AbstractPlayerConfigurationDialog(protected val saveCallback: (player: PlayerEntity) -> Unit, protected val player: PlayerEntity): SimpleDialog()
{
    //Components
    protected val avatar = PlayerAvatar()
    protected val textFieldName = JTextField().also { it.name = "nameField" }

    //Abstract methods
    abstract fun savePlayer()

    override fun okPressed()
    {
        if (valid())
        {
            savePlayer()
            saveCallback(player)
        }
    }

    /**
     * Basic validation on the player name and avatar selections
     */
    protected fun valid(): Boolean
    {
        val name = textFieldName.text
        if (!isValidName(name))
        {
            return false
        }

        val avatarId = avatar.avatarId
        if (avatarId.isEmpty())
        {
            DialogUtil.showErrorOLD("You must select an avatar.")
            return false
        }

        return true
    }

    private fun isValidName(name: String?): Boolean
    {
        if (name.isNullOrEmpty())
        {
            DialogUtil.showErrorOLD("You must enter a name for this player.")
            return false
        }

        val length = name.length
        if (length < 3)
        {
            DialogUtil.showErrorOLD("The player name must be at least 3 characters long.")
            return false
        }

        if (length > 25)
        {
            DialogUtil.showErrorOLD("The player name cannot be more than 25 characters long.")
            return false
        }


        val existingPlayer = PlayerEntity.retrieveForName(name)
        if (existingPlayer != null && existingPlayer.rowId != player.rowId)
        {
            DialogUtil.showErrorOLD("A player with the name $name already exists.")
            return false
        }

        return true
    }
}
