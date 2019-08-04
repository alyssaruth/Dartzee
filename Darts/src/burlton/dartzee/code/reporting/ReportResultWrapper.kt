package burlton.dartzee.code.reporting

import burlton.dartzee.code.db.GameEntity
import java.sql.ResultSet
import java.sql.Timestamp

class ReportResultWrapper
{
    var localId: Long = -1
    var gameType = -1
    var gameParams: String? = null
    var dtStart: Timestamp? = null
    var dtFinish: Timestamp? = null
    var localMatchId: Long = -1
    var matchOrdinal = -1

    private val participants = mutableListOf<ParticipantWrapper>()

    fun getTableRow(): Array<Any?>
    {
        val gameTypeDesc = GameEntity.getTypeDesc(gameType, gameParams!!)
        val playerDesc = getPlayerDesc()

        var matchDesc = ""
        if (localMatchId > -1)
        {
            matchDesc = "#" + localMatchId + " (Game " + (matchOrdinal + 1) + ")"
        }

        return arrayOf(localId, gameTypeDesc, playerDesc, dtStart, dtFinish, matchDesc)
    }

    fun getPlayerDesc(): String
    {
        participants.sortBy { it.finishingPosition }
        return participants.joinToString()
    }

    fun addParticipant(rs: ResultSet)
    {
        val playerName = rs.getString("Name")
        val finishPos = rs.getInt("FinishingPosition")
        participants.add(ParticipantWrapper(playerName, finishPos))
    }

    companion object
    {
        fun factoryFromResultSet(localId: Long, rs: ResultSet): ReportResultWrapper
        {
            val ret = ReportResultWrapper()

            ret.localId = localId
            ret.gameType = rs.getInt("GameType")
            ret.gameParams = rs.getString("GameParams")
            ret.dtStart = rs.getTimestamp("DtCreation")
            ret.dtFinish = rs.getTimestamp("DtFinish")

            ret.addParticipant(rs)

            ret.localMatchId = rs.getLong("LocalMatchId")
            ret.matchOrdinal = rs.getInt("MatchOrdinal")

            return ret
        }

        fun getTableRowsFromWrappers(wrappers: List<ReportResultWrapper>): List<Array<Any?>>
        {
            val rows = mutableListOf<Array<Any?>>()
            for (wrapper in wrappers)
            {
                rows.add(wrapper.getTableRow())
            }

            return rows
        }
    }
}
