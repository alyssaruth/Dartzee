package burlton.dartzee.code.screen

import burlton.dartzee.code.bean.PlayerAvatar
import burlton.dartzee.code.db.PlayerEntity
import burlton.desktopcore.code.screen.SimpleDialog
import burlton.desktopcore.code.util.DialogUtil
import javax.swing.JTextField

abstract class AbstractPlayerCreationDialog : SimpleDialog()
{
    @JvmField var createdPlayer = false

    //Components
    @JvmField val avatar = PlayerAvatar()
    @JvmField val textFieldName = JTextField()

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
    protected open fun valid(): Boolean
    {
        val name = textFieldName.text
        if (!isValidName(name, doExistenceCheck()))
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

    protected open fun doExistenceCheck() = true

    protected fun isValidName(name: String?, checkForExistence: Boolean): Boolean
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

        if (checkForExistence)
        {
            val existingPlayer = PlayerEntity.retrieveForName(name)
            if (existingPlayer != null)
            {
                DialogUtil.showError("A player with the name $name already exists.")
                return false
            }
        }

        return true
    }
}
