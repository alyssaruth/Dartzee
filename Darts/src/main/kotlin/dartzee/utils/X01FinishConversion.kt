package dartzee.utils

import dartzee.achievements.LAST_ROUND_FROM_PARTICIPANT
import dartzee.db.BulkInserter
import dartzee.db.GAME_TYPE_X01
import dartzee.db.X01FinishEntity

object X01FinishConversion
{
    fun convertX01Finishes()
    {
        X01FinishEntity().createTable()

        val zzParticipants = prepareParticipantTempTable()
        val sql = getX01FinishSql(zzParticipants)

        val finishes = mutableListOf<X01FinishEntity>()
        DatabaseUtil.executeQuery(sql).use { rs ->
            while (rs.next())
            {
                val playerId = rs.getString("PlayerId")
                val gameId = rs.getString("GameId")
                val finish = rs.getInt("StartingScore")
                val dtFinished = rs.getTimestamp("DtFinished")

                val entity = X01FinishEntity()
                entity.assignRowId()
                entity.playerId = playerId
                entity.gameId = gameId
                entity.finish = finish
                entity.dtCreation = dtFinished

                finishes.add(entity)
            }
        }

        BulkInserter.insert(finishes)
        DatabaseUtil.dropTable(zzParticipants)
    }
    private fun prepareParticipantTempTable(): String
    {
        val zzParticipants = DatabaseUtil.createTempTable("FinishedParticipants", "PlayerId VARCHAR(36), GameId VARCHAR(36), ParticipantId VARCHAR(36), RoundNumber INT, DtFinished TIMESTAMP")
        zzParticipants ?: return ""

        val sbPt = StringBuilder()
        sbPt.append("INSERT INTO $zzParticipants ")
        sbPt.append(" SELECT p.RowId, g.RowId, pt.RowId, $LAST_ROUND_FROM_PARTICIPANT, pt.DtFinished")
        sbPt.append(" FROM Player p, Participant pt, Game g")
        sbPt.append(" WHERE pt.GameId = g.RowId")
        sbPt.append(" AND g.GameType = $GAME_TYPE_X01")
        sbPt.append(" AND pt.FinalScore > -1")
        sbPt.append(" AND pt.PlayerId = p.RowId")

        DatabaseUtil.executeUpdate(sbPt.toString())
        DatabaseUtil.executeUpdate("CREATE INDEX ${zzParticipants}_PlayerId ON $zzParticipants(PlayerId, ParticipantId, RoundNumber)")

        return zzParticipants
    }
    private fun getX01FinishSql(zzFinishedParticipants: String): String
    {
        val sb = StringBuilder()
        sb.append("SELECT zz.PlayerId, zz.GameId, drtFirst.StartingScore, zz.DtFinished")
        sb.append(" FROM Dart drtFirst, $zzFinishedParticipants zz")
        sb.append(" WHERE drtFirst.PlayerId = zz.PlayerId")
        sb.append(" AND drtFirst.ParticipantId = zz.ParticipantId")
        sb.append(" AND drtFirst.RoundNumber = zz.RoundNumber")
        sb.append(" AND drtFirst.Ordinal = 1")

        return sb.toString()
    }
}