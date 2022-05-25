package dartzee.stats

import dartzee.core.obj.HashMapCount
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.mainDatabase
import java.sql.SQLException

fun getGameCounts(player: PlayerEntity): HashMapCount<GameType>
{
    val hmTypeToCount = HashMapCount<GameType>()
    val query = "SELECT g.GameType FROM Participant pt, Game g WHERE pt.GameId = g.RowId AND pt.PlayerId = '${player.rowId}'"
    mainDatabase.executeQuery(query).use { rs ->
        while (rs.next())
        {
            val gameType = GameType.valueOf(rs.getString("GameType"))
            hmTypeToCount.incrementCount(gameType)
        }
    }

    return hmTypeToCount
}

fun retrieveGameData(playerId: String, gameType: GameType): Map<Long, GameWrapper>
{
    val hm = mutableMapOf<Long, GameWrapper>()
    val zzParticipants = buildParticipantTable(playerId, gameType) ?: return hm

    val sb = StringBuilder()
    sb.append(" SELECT zz.LocalId, zz.GameParams, zz.DtCreation, zz.DtFinish, zz.FinalScore, ")
    sb.append(" drt.RoundNumber,")
    sb.append(" drt.Ordinal, drt.Score, drt.Multiplier, drt.StartingScore, drt.SegmentType")
    sb.append(" FROM Dart drt, $zzParticipants zz")
    sb.append(" WHERE drt.ParticipantId = zz.ParticipantId")
    sb.append(" AND drt.PlayerId = zz.PlayerId")

    try
    {
        mainDatabase.executeQuery(sb).use { rs ->
            while (rs.next())
            {
                val gameId = rs.getLong("LocalId")
                val gameParams = rs.getString("GameParams")
                val dtStart = rs.getTimestamp("DtCreation")
                val dtFinish = rs.getTimestamp("DtFinish")
                val numberOfDarts = rs.getInt("FinalScore")
                val roundNumber = rs.getInt("RoundNumber")
                val ordinal = rs.getInt("Ordinal")
                val score = rs.getInt("Score")
                val multiplier = rs.getInt("Multiplier")
                val startingScore = rs.getInt("StartingScore")
                val segmentType = SegmentType.valueOf(rs.getString("SegmentType"))

                val wrapper = hm.getOrPut(gameId) { GameWrapper(gameId, gameParams, dtStart, dtFinish, numberOfDarts) }

                val dart = Dart(score, multiplier, segmentType = segmentType)
                dart.ordinal = ordinal
                dart.startingScore = startingScore
                dart.roundNumber = roundNumber
                wrapper.addDart(roundNumber, dart)
            }
        }
    }
    catch (sqle: SQLException)
    {
        InjectedThings.logger.logSqlException(sb.toString(), "", sqle)
    }
    finally
    {
        mainDatabase.dropTable(zzParticipants)
    }

    return hm
}
private fun buildParticipantTable(playerId: String, gameType: GameType): String?
{
    val tmp = mainDatabase.createTempTable("ParticipantsForStats", "LocalId INT, GameParams VARCHAR(255), DtCreation TIMESTAMP, DtFinish TIMESTAMP, PlayerId VARCHAR(36), ParticipantId VARCHAR(36), FinalScore INT")
    tmp ?: return null

    val sb = StringBuilder()
    sb.append(" INSERT INTO $tmp")
    sb.append(" SELECT g.LocalId, g.GameParams, g.DtCreation, g.DtFinish, pt.PlayerId, pt.RowId AS ParticipantId, pt.FinalScore ")
    sb.append(" FROM Participant pt, Game g")
    sb.append(" WHERE pt.GameId = g.RowId")
    sb.append(" AND pt.PlayerId = '$playerId'")
    sb.append(" AND g.GameType = '$gameType'")

    mainDatabase.executeUpdate(sb)

    mainDatabase.executeUpdate("CREATE INDEX ${tmp}_PlayerId_ParticipantId ON $tmp(PlayerId, ParticipantId)")
    return tmp
}