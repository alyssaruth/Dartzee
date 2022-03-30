package dartzee.screen

import dartzee.bean.PlayerAvatar
import dartzee.core.screen.SimpleDialog
import dartzee.core.util.DialogUtil
import dartzee.db.PlayerEntity
import javax.swing.JTextField

abstract class AbstractPlayerConfigurationDialog(protected val player: PlayerEntity): SimpleDialog()
{
    //Components
    val avatar = PlayerAvatar()
    val textFieldName = JTextField()

    //Abstract methods
    abstract fun savePlayer()

    override fun okPressed()
    {
        if (valid())
        {
            savePlayer()
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
            DialogUtil.showError("You must select an avatar.")
            return false
        }

        return true
    }

    private fun isValidName(name: String?): Boolean
    {
        if (name == null || name.isEmpty())
        {
            DialogUtil.showError("You must enter a name for this player.")
            return false
        }

        val length = name.length
        if (length < 3)
        {
            DialogUtil.showError("The player name must be at least 3 characters long.")
            return false
        }

        if (length > 25)
        {
            DialogUtil.showError("The player name cannot be more than 25 characters long.")
            return false
        }


        val existingPlayer = PlayerEntity.retrieveForName(name)
        if (existingPlayer != null && existingPlayer.rowId != player.rowId)
        {
            DialogUtil.showError("A player with the name $name already exists.")
            return false
        }

        return true
    }
}
