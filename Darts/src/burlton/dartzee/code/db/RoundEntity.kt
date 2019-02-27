package burlton.dartzee.code.db

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class RoundEntity : AbstractDartsEntity<RoundEntity>()
{
    /**
     * DB Fields
     */
    var participantId: Long = -1
    var dartzeeRuleId: Long = -1
    var roundNumber = -1

    override fun getTableName(): String
    {
        return "Round"
    }

    override fun getCreateTableSqlSpecific(): String
    {
        return "ParticipantId INT NOT NULL, DartzeeRuleId INT NOT NULL, RoundNumber INT NOT NULL"
    }

    @Throws(SQLException::class)
    override fun populateFromResultSet(entity: RoundEntity, rs: ResultSet)
    {
        entity.participantId = rs.getLong("ParticipantId")
        entity.dartzeeRuleId = rs.getLong("DartzeeRuleId")
        entity.roundNumber = rs.getInt("RoundNumber")
    }

    @Throws(SQLException::class)
    override fun writeValuesToStatement(statement: PreparedStatement, startIndex: Int, emptyStatement: String): String
    {
        var i = startIndex
        var statementStr = emptyStatement
        statementStr = writeLong(statement, i++, participantId, statementStr)
        statementStr = writeLong(statement, i++, dartzeeRuleId, statementStr)
        statementStr = writeInt(statement, i, roundNumber, statementStr)

        return statementStr
    }

    override fun getColumnsAllowedToBeUnset(): ArrayList<String>
    {
        val ret = ArrayList<String>()
        ret.add("DartzeeRuleId")
        return ret
    }

    override fun getGameId(): Long
    {
        return retrieveParticipant()?.gameId ?: -1
    }

    fun retrieveParticipant(): ParticipantEntity?
    {
        return ParticipantEntity().retrieveForId(participantId)
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
