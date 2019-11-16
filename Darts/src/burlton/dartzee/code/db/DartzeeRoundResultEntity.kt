package burlton.dartzee.code.db

class DartzeeRoundResultEntity: AbstractEntity<DartzeeRoundResultEntity>()
{
    var playerId: String = ""
    var participantId: String = ""
    var roundNumber: Int = -1
    var ruleNumber: Int = -1
    var success: Boolean = false

    override fun getTableName() = "DartzeeRoundResult"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("PlayerId VARCHAR(36) NOT NULL, "
                + "ParticipantId VARCHAR(36) NOT NULL, "
                + "RoundNumber INT NOT NULL, "
                + "RuleNumber INT NOT NULL, "
                + "Success BOOLEAN NOT NULL")
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<List<String>>)
    {
        indexes.add(listOf("PlayerId", "ParticipantId", "RoundNumber"))
    }
}