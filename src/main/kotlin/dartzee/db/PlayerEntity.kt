package dartzee.db

import dartzee.ai.DartsAiModel
import dartzee.core.util.DateStatics
import dartzee.core.util.getEndOfTimeSqlString
import dartzee.core.util.toLocalDate
import dartzee.theme.themedIcon
import dartzee.utils.Database
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.mainDatabase
import javax.swing.ImageIcon

open class PlayerEntity(database: Database = mainDatabase) :
    AbstractEntity<PlayerEntity>(database) {
    // DB Fields
    var name = ""
    var strategy = ""
    var dtDeleted = DateStatics.END_OF_TIME
    var playerImageId = ""
    var dateOfBirth = DateStatics.END_OF_TIME

    override fun getTableName() = EntityName.Player

    override fun getCreateTableSqlSpecific(): String {
        return ("Name varchar(25) NOT NULL, " +
            "Strategy varchar(1000) NOT NULL, " +
            "DtDeleted timestamp NOT NULL, " +
            "PlayerImageId VARCHAR(36) NOT NULL, " +
            "DateOfBirth timestamp NOT NULL")
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<List<String>>) {
        val nameIndex = listOf("Name")
        val strategyDtDeletedIndex = listOf("Strategy", "DtDeleted")

        indexes.add(nameIndex)
        indexes.add(strategyDtDeletedIndex)
    }

    override fun toString() = name

    fun isHuman() = strategy.isEmpty()

    fun isAi() = strategy.isNotEmpty()

    open fun getModel() = DartsAiModel.fromJson(strategy)

    fun getAvatar() = PlayerImageEntity.retrieveImageIconForId(playerImageId)

    fun getFlag() = getPlayerFlag(isHuman())

    fun birthdayIsToday(): Boolean {
        val localDate = dateOfBirth.toLocalDate() ?: return false

        return localDate.month == InjectedThings.now.month &&
            localDate.dayOfMonth == InjectedThings.now.dayOfMonth
    }

    companion object {
        val ICON_AI = ImageIcon(PlayerEntity::class.java.getResource("/flags/aiFlag.png"))

        fun getPlayerFlag(human: Boolean) =
            if (human) themedIcon("/flags/humanFlag.png") else ICON_AI

        /** Retrieval methods */
        fun retrievePlayers(startingSql: String): List<PlayerEntity> {
            var whereSql = startingSql
            if (!startingSql.isEmpty()) {
                whereSql += " AND "
            }
            whereSql += "DtDeleted = " + getEndOfTimeSqlString()

            return PlayerEntity().retrieveEntities(whereSql)
        }

        fun retrieveForName(name: String): PlayerEntity? {
            val whereSql = "Name = '$name' AND DtDeleted = ${getEndOfTimeSqlString()}"
            val players = PlayerEntity().retrieveEntities(whereSql)
            return if (players.isEmpty()) null else players[0]
        }

        fun factoryCreate(): PlayerEntity {
            val entity = PlayerEntity()
            entity.assignRowId()

            return entity
        }
    }
}
