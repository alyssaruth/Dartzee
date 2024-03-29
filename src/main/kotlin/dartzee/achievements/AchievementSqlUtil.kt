package dartzee.achievements

import dartzee.achievements.dartzee.DARTZEE_ACHIEVEMENT_MIN_ROUNDS
import dartzee.db.AchievementEntity
import dartzee.db.BulkInserter
import dartzee.db.EntityName
import dartzee.game.GameType
import dartzee.`object`.SegmentType
import dartzee.utils.Database
import dartzee.utils.getQuotedIdStr
import dartzee.utils.isNullStatement
import java.sql.ResultSet

const val X01_ROUNDS_TABLE = "X01Rounds"
const val LAST_ROUND_FROM_PARTICIPANT = "CEIL(CAST(pt.FinalScore AS DECIMAL)/3)"
const val LAST_ROUND_FROM_TEAM = "CEIL(CAST(t.FinalScore AS DECIMAL)/3)"

fun getGolfSegmentCases(): String {
    val sb = StringBuilder()
    sb.append(" WHEN drt.SegmentType = '${SegmentType.DOUBLE}' THEN 1")
    sb.append(" WHEN drt.SegmentType = '${SegmentType.TREBLE}' THEN 2")
    sb.append(" WHEN drt.SegmentType = '${SegmentType.INNER_SINGLE}' THEN 3")
    sb.append(" WHEN drt.SegmentType = '${SegmentType.OUTER_SINGLE}' THEN 4")
    sb.append(" ELSE 5")

    return sb.toString()
}

fun getPlayerSql(
    playerIds: List<String>,
    alias: String? = "pt",
    whereOrAnd: String = "AND"
): String {
    if (playerIds.isEmpty()) {
        return ""
    }

    val keys = playerIds.getQuotedIdStr()
    val column = if (alias != null) "$alias.PlayerId" else "PlayerId"
    return "$whereOrAnd $column IN $keys"
}

fun appendPlayerSql(
    sb: StringBuilder,
    playerIds: List<String>,
    alias: String? = "pt",
    whereOrAnd: String = "AND"
) {
    sb.append(" ${getPlayerSql(playerIds, alias, whereOrAnd)}")
}

fun ensureX01RoundsTableExists(playerIds: List<String>, database: Database) {
    val created =
        database.createTableIfNotExists(
            X01_ROUNDS_TABLE,
            "PlayerId VARCHAR(36), GameId VARCHAR(36), GameParams VARCHAR(255), ParticipantId VARCHAR(36), StartingScore INT, RoundNumber INT, " +
                "TotalDartsThrown INT, RemainingScore INT, LastDartScore INT, LastDartMultiplier INT, DtRoundFinished TIMESTAMP"
        )

    if (!created) {
        return
    }

    val tmp1 =
        database.createTempTable(
            "X01RoundsPt1",
            "PlayerId VARCHAR(36), GameId VARCHAR(36), GameParams VARCHAR(255), ParticipantId VARCHAR(36), StartingScore INT, RoundNumber INT, LastDartOrdinal INT"
        )

    database.executeUpdate(
        """
        INSERT INTO $tmp1
        SELECT pt.PlayerId, pt.GameId, g.GameParams, pt.RowId, drtFirst.StartingScore, drtFirst.RoundNumber, MAX(drt.Ordinal)
        FROM ${EntityName.Dart} drtFirst, ${EntityName.Participant} pt, ${EntityName.Game} g, ${EntityName.Dart} drt
        WHERE drtFirst.ParticipantId = pt.RowId
        AND drtFirst.PlayerId = pt.PlayerId
        AND pt.GameId = g.RowId
        AND g.GameType = '${GameType.X01}'
        AND drtFirst.Ordinal = 1
        AND drtFirst.PlayerId = drt.PlayerId
        AND drtFirst.ParticipantId = drt.ParticipantId
        AND drtFirst.RoundNumber = drt.RoundNumber
        ${getPlayerSql(playerIds)}
        GROUP BY pt.PlayerId, pt.GameId, g.GameParams, pt.RowId, drtFirst.StartingScore, drtFirst.RoundNumber
    """
            .trimIndent()
    )

    database.executeUpdate(
        """
        INSERT INTO $X01_ROUNDS_TABLE
        SELECT 
            zz.PlayerId, 
            zz.GameId, 
            zz.GameParams,
            zz.ParticipantId, 
            zz.StartingScore, 
            zz.RoundNumber, 
            zz.LastDartOrdinal, 
            drt.StartingScore - (drt.Score * drt.Multiplier), 
            drt.Score, 
            drt.Multiplier, 
            drt.DtCreation
        FROM $tmp1 zz, ${EntityName.Dart} drt
        WHERE zz.PlayerId = drt.PlayerId
        AND zz.ParticipantId = drt.ParticipantId
        AND zz.RoundNumber = drt.RoundNumber
        AND zz.LastDartOrdinal = drt.Ordinal
    """
            .trimIndent()
    )
}

fun buildQualifyingDartzeeGamesTable(database: Database): String? {
    val dartzeeGames =
        database.createTempTable(
            "DartzeeGames",
            "GameId VARCHAR(36), RoundCount INT, TemplateName VARCHAR(1000)"
        )

    val sb = StringBuilder()
    sb.append(" INSERT INTO $dartzeeGames")
    sb.append(" SELECT g.RowId, COUNT(1) + 1, ${isNullStatement("dt.Name", "''", "TemplateName")}")
    sb.append(" FROM ${EntityName.DartzeeRule} dr, ${EntityName.Game} g")
    sb.append(" LEFT OUTER JOIN ${EntityName.DartzeeTemplate} dt ON (g.GameParams = dt.RowId)")
    sb.append(" WHERE dr.EntityId = g.RowId")
    sb.append(" AND dr.EntityName = '${EntityName.Game}'")
    sb.append(" AND g.GameType = '${GameType.DARTZEE}'")
    sb.append(" GROUP BY g.RowId, dt.Name")
    sb.append(" HAVING COUNT(1) >= ${DARTZEE_ACHIEVEMENT_MIN_ROUNDS - 1}")

    if (!database.executeUpdate(sb)) return null

    return dartzeeGames
}

fun bulkInsertFromResultSet(
    rs: ResultSet,
    database: Database,
    achievementType: AchievementType,
    achievementDetailFn: (() -> String)? = null,
    achievementCounterFn: (() -> Int)? = null,
    oneRowPerPlayer: Boolean = false
) {
    val playerIdsSeen = mutableSetOf<String>()

    val entities = mutableListOf<AchievementEntity>()
    while (rs.next()) {
        val playerId = rs.getString("PlayerId")
        val gameId = rs.getString("GameId")
        val dtAchieved = rs.getTimestamp("DtAchieved")
        val detail = achievementDetailFn?.invoke().orEmpty()
        val counter = achievementCounterFn?.invoke() ?: -1

        if (!oneRowPerPlayer || playerIdsSeen.add(playerId)) {
            entities.add(
                AchievementEntity.factory(
                    achievementType,
                    playerId,
                    gameId,
                    counter,
                    detail,
                    dtAchieved,
                    database
                )
            )
        }
    }

    BulkInserter.insert(entities, database = database)
}
