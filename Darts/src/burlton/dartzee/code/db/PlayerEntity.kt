package burlton.dartzee.code.db

import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.screen.ScreenCache
import burlton.desktopcore.code.util.DateStatics.Companion.END_OF_TIME
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.getEndOfTimeSqlString
import javax.swing.ImageIcon

class PlayerEntity:AbstractEntity<PlayerEntity>()
{
    //DB Fields
    var name = ""
    var strategy = -1
    var strategyXml = ""
    var dtDeleted = END_OF_TIME
    var playerImageId = ""


    override fun getTableName() = "Player"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("Name varchar(25) NOT NULL, "
                + "Strategy int NOT NULL, "
                + "StrategyXml varchar(1000) NOT NULL, "
                + "DtDeleted timestamp NOT NULL, "
                + "PlayerImageId VARCHAR(36) NOT NULL")
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<MutableList<String>>)
    {
        val nameIndex = mutableListOf("Name")
        val strategyDtDeletedIndex = mutableListOf("Strategy", "DtDeleted")

        indexes.add(nameIndex)
        indexes.add(strategyDtDeletedIndex)
    }

    override fun toString() = name

    /**
     * Helpers
     */
    fun isHuman() = (strategy == -1)
    fun isAi() = (strategy > -1)
    fun getModel(): AbstractDartsModel
    {
        val model = AbstractDartsModel.factoryForType(strategy)!!
        model.readXml(strategyXml)
        return model
    }

    fun getAvatar() = if (playerImageId.isEmpty()) null else PlayerImageEntity.retrieveImageIconForId(playerImageId)
    fun getFlag() = getPlayerFlag(isHuman())

    companion object
    {
        private val ICON_AI = ImageIcon(PlayerEntity::class.java.getResource("/flags/aiFlag.png"))
        private val ICON_HUMAN = ImageIcon(PlayerEntity::class.java.getResource("/flags/humanFlag.png"))

        fun getPlayerFlag(human:Boolean) = if (human) ICON_HUMAN else ICON_AI

        /**
         * Retrieval methods
         */
        @JvmStatic fun retrievePlayers(startingSql:String, includeDeleted:Boolean):List<PlayerEntity>
        {
            var whereSql = startingSql
            if (!includeDeleted)
            {
                if (!startingSql.isEmpty())
                {
                    whereSql += " AND "
                }
                whereSql += "DtDeleted = " + getEndOfTimeSqlString()
            }

            return PlayerEntity().retrieveEntities(whereSql)
        }
        private fun retrieveForName(name:String): PlayerEntity?
        {
            val whereSql = "Name = '$name' AND DtDeleted = ${getEndOfTimeSqlString()}"
            val players = PlayerEntity().retrieveEntities(whereSql)
            return if (players.isEmpty()) null else players[0]
        }

        /**
         * Creation/validation
         */
        @JvmStatic fun createNewPlayer(human: Boolean)
        {
            val created = createAndSavePlayerIfValid(human)
            if (created)
            {
                ScreenCache.getPlayerManagementScreen().initialise()
            }
        }
        private fun createAndSavePlayerIfValid(human: Boolean): Boolean
        {
            return if (human) createNewHuman() else createNewAI()
        }

        private fun createNewHuman(): Boolean
        {
            val dlg = ScreenCache.getHumanCreationDialog()
            dlg.init()
            dlg.isVisible = true

            return dlg.createdPlayer
        }
        private fun createNewAI(): Boolean
        {
            val dialog = ScreenCache.getAIConfigurationDialog()
            dialog.init(null)
            dialog.isVisible = true

            return dialog.createdPlayer
        }

        @JvmStatic fun isValidName(name: String?, checkForExistence: Boolean): Boolean
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
                val existingPlayer = retrieveForName(name)
                if (existingPlayer != null)
                {
                    DialogUtil.showError("A player with the name $name already exists.")
                    return false
                }
            }

            return true
        }

        @JvmStatic fun factoryAndSaveHuman(name: String, avatarId: String): PlayerEntity
        {
            val entity = factoryCreate()

            entity.name = name
            entity.playerImageId = avatarId
            entity.saveToDatabase()
            entity.retrievedFromDb = true

            return entity
        }
        @JvmStatic fun factoryCreate(): PlayerEntity
        {
            val entity = PlayerEntity()
            entity.assignRowId()

            return entity
        }
    }
}
