package burlton.dartzee.code.db

class RoundDetailEntity: AbstractEntity<RoundDetailEntity>()
{
    var playerId: String = ""
    var participantId: String = ""
    var roundNumber: Int = -1
    var detail: String = ""

    override fun getTableName() = "RoundDetail"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("PlayerId VARCHAR(36) NOT NULL, "
                + "ParticipantId VARCHAR(36) NOT NULL, "
                + "RoundNumber INT NOT NULL, "
                + "Detail VARCHAR(5000) NOT NULL")
    }

    override fun addListsOfColumnsForIndexes(indexes: MutableList<List<String>>)
    {
        indexes.add(listOf("PlayerId", "ParticipantId", "RoundNumber"))
    }
}