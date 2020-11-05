package dartzee.achievements

import dartzee.`object`.*
import dartzee.db.AchievementEntity
import dartzee.db.BulkInserter
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.utils.Database
import java.sql.ResultSet

const val X01_ROUNDS_TABLE = "X01Rounds"
const val LAST_ROUND_FROM_PARTICIPANT = "CEIL(CAST(pt.FinalScore AS DECIMAL)/3)"

fun getGolfSegmentCases(): String
{
    val sb = StringBuilder()
    sb.append(" WHEN drt.SegmentType = '${SegmentType.DOUBLE}' THEN 1")
    sb.append(" WHEN drt.SegmentType = '${SegmentType.TREBLE}' THEN 2")
    sb.append(" WHEN drt.SegmentType = '${SegmentType.INNER_SINGLE}' THEN 3")
    sb.append(" WHEN drt.SegmentType = '${SegmentType.OUTER_SINGLE}' THEN 4")
    sb.append(" ELSE 5")

    return sb.toString()
}

fun appendPlayerSql(sb: StringBuilder, players: List<PlayerEntity>, alias: String? = "pt")
{
    if (players.isEmpty())
    {
        return
    }

    val keys = players.joinToString { p -> "'${p.rowId}'"}
    val column = if (alias != null) "$alias.PlayerId" else "PlayerId"
    sb.append(" AND $column IN ($keys)")
}

fun ensureX01RoundsTableExists(players: List<PlayerEntity>, database: Database)
{
    val created = database.createTableIfNotExists(
        X01_ROUNDS_TABLE,
        "PlayerId VARCHAR(36), GameId VARCHAR(36), ParticipantId VARCHAR(36), StartingScore INT, RoundNumber INT, " +
            "TotalDartsThrown INT, RemainingScore INT, LastDartScore INT, LastDartMultiplier INT, DtRoundFinished TIMESTAMP")

    if (!created)
    {
        return
    }

    val tmp1 = database.createTempTable("X01RoundsPt1",
        "PlayerId VARCHAR(36), GameId VARCHAR(36), ParticipantId VARCHAR(36), StartingScore INT, RoundNumber INT")

    var sb = StringBuilder()
    sb.append(" INSERT INTO $tmp1")
    sb.append(" SELECT pt.PlayerId, pt.GameId, pt.RowId, d.StartingScore, d.RoundNumber")
    sb.append(" FROM Dart d, Participant pt, Game g")
    sb.append(" WHERE d.ParticipantId = pt.RowId")
    sb.append(" AND d.PlayerId = pt.PlayerId")
    sb.append(" AND pt.GameId = g.RowId")
    sb.append(" AND g.GameType = '${GameType.X01}'")
    sb.append(" AND d.Ordinal = 1")
    appendPlayerSql(sb, players)
    database.executeUpdate(sb.toString())

    val tmp2 = database.createTempTable("X01RoundsPt2",
        "PlayerId VARCHAR(36), GameId VARCHAR(36), ParticipantId VARCHAR(36), StartingScore INT, RoundNumber INT, LastDartOrdinal INT")

    sb = StringBuilder()
    sb.append(" INSERT INTO $tmp2")
    sb.append(" SELECT zz.PlayerId, zz.GameId, zz.ParticipantId, zz.StartingScore, zz.RoundNumber, MAX(drt.Ordinal)")
    sb.append(" FROM $tmp1 zz, Dart drt")
    sb.append(" WHERE zz.PlayerId = drt.PlayerId")
    sb.append(" AND zz.ParticipantId = drt.ParticipantId")
    sb.append(" AND zz.RoundNumber = drt.RoundNumber")
    sb.append(" GROUP BY zz.PlayerId, zz.GameId, zz.ParticipantId, zz.StartingScore, zz.RoundNumber")
    database.executeUpdate(sb.toString())

    sb = StringBuilder()
    sb.append(" INSERT INTO $X01_ROUNDS_TABLE")
    sb.append(" SELECT zz.PlayerId, zz.GameId, zz.ParticipantId, zz.StartingScore, zz.RoundNumber, zz.LastDartOrdinal,")
    sb.append(" drt.StartingScore - (drt.Score * drt.Multiplier), drt.Score, drt.Multiplier, drt.DtCreation")
    sb.append(" FROM $tmp2 zz, Dart drt")
    sb.append(" WHERE zz.PlayerId = drt.PlayerId")
    sb.append(" AND zz.ParticipantId = drt.ParticipantId")
    sb.append(" AND zz.RoundNumber = drt.RoundNumber")
    sb.append(" AND zz.LastDartOrdinal = drt.Ordinal")
    database.executeUpdate(sb.toString())
}

fun bulkInsertFromResultSet(rs: ResultSet, database: Database, achievementRef: Int)
{
    val entities = mutableListOf<AchievementEntity>()
    while (rs.next())
    {
        val playerId = rs.getString("PlayerId")
        val gameId = rs.getString("GameId")
        val dtAchieved = rs.getTimestamp("DtAchieved")

        entities.add(AchievementEntity.factory(achievementRef, playerId, gameId, -1, "", dtAchieved, database))
    }

    BulkInserter.insert(entities, database = database)
}