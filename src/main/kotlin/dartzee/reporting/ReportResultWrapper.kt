package dartzee.reporting

import dartzee.game.GameType
import dartzee.utils.getGameDesc
import java.sql.ResultSet
import java.sql.Timestamp

data class ReportResultWrapper(val localId: Long,
                               val gameType: GameType,
                               val gameParams: String,
                               val dtStart: Timestamp,
                               val dtFinish: Timestamp,
                               val localMatchId: Long,
                               val matchOrdinal: Int)
{
    private val participants = mutableListOf<ParticipantWrapper>()

    fun getTableRow(): Array<Any>
    {
        val gameTypeDesc = getGameDesc(gameType, gameParams)
        val playerDesc = getPlayerDesc()

        var matchDesc = ""
        if (localMatchId > -1)
        {
            matchDesc = "#$localMatchId (Game $matchOrdinal)"
        }

        return arrayOf(localId, gameTypeDesc, playerDesc, dtStart, dtFinish, matchDesc)
    }

    private fun getPlayerDesc(): String
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
            val gameType = GameType.valueOf(rs.getString("GameType"))
            val gameParams = rs.getString("GameParams")
            val dtStart = rs.getTimestamp("DtCreation")
            val dtFinish = rs.getTimestamp("DtFinish")
            val localMatchId = rs.getLong("LocalMatchId")
            val matchOrdinal = rs.getInt("MatchOrdinal")

            val ret = ReportResultWrapper(localId, gameType, gameParams, dtStart, dtFinish, localMatchId, matchOrdinal)
            ret.addParticipant(rs)
            return ret
        }

        fun getTableRowsFromWrappers(wrappers: List<ReportResultWrapper>) = wrappers.map { it.getTableRow() }
    }
}
