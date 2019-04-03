package burlton.dartzee.code.db

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class RoundEntity : AbstractEntity<RoundEntity>()
{
    /**
     * DB Fields
     */
    var participantId: String = ""
    var roundNumber = -1

    override fun getTableName() = "Round"

    override fun getCreateTableSqlSpecific(): String
    {
        return "ParticipantId VARCHAR(36) NOT NULL, RoundNumber INT NOT NULL"
    }

    @Throws(SQLException::class)
    override fun populateFromResultSet(entity: RoundEntity, rs: ResultSet)
    {
        entity.participantId = rs.getString("ParticipantId")
        entity.roundNumber = rs.getInt("RoundNumber")
    }

    @Throws(SQLException::class)
    override fun writeValuesToStatement(statement: PreparedStatement, startIndex: Int, emptyStatement: String): String
    {
        var i = startIndex
        var statementStr = emptyStatement
        statementStr = writeString(statement, i++, participantId, statementStr)
        statementStr = writeInt(statement, i, roundNumber, statementStr)

        return statementStr
    }

    fun isForParticipant(pt: ParticipantEntity): Boolean
    {
        val ptId = pt.rowId
        return participantId == ptId
    }

    companion object
    {

        /**
         * This is NOT a 'factoryAndSave' because we only want to save the Round when it's over, along with
         * its corresponding darts. This makes loading unfinished games much simpler.
         */
        fun factory(participant: ParticipantEntity, roundNumber: Int): RoundEntity
        {
            val re = RoundEntity()
            re.assignRowId()

            re.participantId = participant.rowId
            re.roundNumber = roundNumber

            return re
        }
    }
}
