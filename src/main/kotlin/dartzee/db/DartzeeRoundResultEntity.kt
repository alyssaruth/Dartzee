package dartzee.db

import dartzee.dartzee.DartzeeRoundResult
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase

class DartzeeRoundResultEntity(database: Database = mainDatabase): AbstractEntity<DartzeeRoundResultEntity>(database)
{
    var playerId: String = ""
    var participantId: String = ""
    var roundNumber: Int = -1
    var ruleNumber: Int = -1
    var success: Boolean = false
    var score: Int = -1

    override fun getTableName() = "DartzeeRoundResult"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("PlayerId VARCHAR(36) NOT NULL, "
                + "ParticipantId VARCHAR(36) NOT NULL, "
                + "RoundNumber INT NOT NULL, "
                + "RuleNumber INT NOT NULL, "
                + "Success BOOLEAN NOT NULL, "
                + "Score INT NOT NULL")
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<List<String>>)
    {
        indexes.add(listOf("PlayerId", "ParticipantId", "RoundNumber"))
    }

    fun toDto(): DartzeeRoundResult = DartzeeRoundResult(ruleNumber, success, score)

    companion object
    {
        fun factoryAndSave(dto: DartzeeRoundResult, pt: ParticipantEntity, roundNumber: Int): DartzeeRoundResultEntity
        {
            val entity = DartzeeRoundResultEntity()
            entity.assignRowId()
            entity.ruleNumber = dto.ruleNumber
            entity.success = dto.success
            entity.playerId = pt.playerId
            entity.participantId = pt.rowId
            entity.roundNumber = roundNumber
            entity.score = dto.score

            entity.saveToDatabase()
            return entity
        }
    }
}