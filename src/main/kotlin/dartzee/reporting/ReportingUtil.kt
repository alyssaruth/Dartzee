package dartzee.reporting

import dartzee.db.EntityName
import dartzee.game.GameType
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.isNullStatement
import java.sql.ResultSet
import java.sql.Timestamp
import javax.swing.JCheckBox

fun <T> grabIfSelected(checkbox: JCheckBox, getter: () -> T) =
    if (checkbox.isSelected) getter() else null

fun runReport(rp: ReportParameters?): List<ReportResultWrapper> {
    rp ?: return emptyList()

    val ptTemp = makeParticipantTempTable() ?: return emptyList()

    var sql = buildBasicSqlStatement(ptTemp)
    sql += rp.getExtraWhereSql(ptTemp)
    sql += "\n ORDER BY LocalId, TeamOrdinal, Ordinal"

    val hm = mutableMapOf<Long, ReportResultWrapper>()
    mainDatabase.executeQuery(sql).use { rs ->
        while (rs.next()) {
            val localId = rs.getLong("LocalId")
            val wrapper =
                hm.getOrPut(localId) { ReportResultWrapper.factoryFromResultSet(localId, rs) }
            wrapper.addParticipant(rs)
        }
    }

    return hm.values.toList()
}

private fun makeParticipantTempTable(): String? {
    val tempTable =
        mainDatabase.createTempTable(
            "reportParticipants",
            "GameId VARCHAR(36), PlayerId VARCHAR(36), FinishingPosition INT, FinalScore INT, " +
                "TeamId VARCHAR(36), Resigned BOOLEAN, TeamOrdinal INT, Ordinal INT"
        ) ?: return null

    mainDatabase.executeUpdate(
        """
        INSERT INTO 
            $tempTable
        SELECT
            pt.GameId,
            pt.PlayerId,
            ${isNullStatement("t.FinishingPosition", "pt.FinishingPosition", "FinishingPosition")},
            ${isNullStatement("t.FinalScore", "pt.FinalScore", "FinalScore")},
            pt.TeamId,
            ${isNullStatement("t.Resigned", "pt.Resigned", "Resigned")},
            ${isNullStatement("t.Ordinal", "99", "TeamOrdinal")},
            pt.Ordinal
        FROM 
            ${EntityName.Participant} pt LEFT OUTER JOIN ${EntityName.Team} t ON (pt.TeamId = t.RowId)
    """
            .trimIndent()
    )

    mainDatabase.executeUpdate("CREATE INDEX ${tempTable}_GameId ON $tempTable(GameId)")

    return tempTable
}

private fun buildBasicSqlStatement(ptTempTable: String) =
    """
    SELECT 
        g.RowId, 
        g.LocalId, 
        g.GameType, 
        g.GameParams, 
        g.DtCreation, 
        g.DtFinish, 
        p.Name, 
        pt.FinishingPosition, 
        pt.Resigned,
        pt.TeamId,
        g.DartsMatchId, 
        g.MatchOrdinal, 
        dt.Name AS TemplateName,
        ${isNullStatement("m.LocalId", "-1", "LocalMatchId")}
    FROM
        $ptTempTable pt,
        ${EntityName.Player} p,
        ${EntityName.Game} g
    LEFT OUTER JOIN 
        ${EntityName.DartsMatch} m ON (g.DartsMatchId = m.RowId)
    LEFT OUTER JOIN 
        ${EntityName.DartzeeTemplate} dt ON (g.GameType = '${GameType.DARTZEE}' AND g.GameParams = dt.RowId)
    WHERE
        pt.GameId = g.RowId
    AND pt.PlayerId = p.RowId
"""
        .trimIndent()

data class ReportResultWrapper(
    val localId: Long,
    val gameType: GameType,
    val gameParams: String,
    val dtStart: Timestamp,
    val dtFinish: Timestamp,
    val localMatchId: Long,
    val matchOrdinal: Int,
    val templateName: String?
) {
    private val participants = mutableListOf<ParticipantWrapper>()

    fun getTableRow(): Array<Any> {
        val gameTypeDesc =
            templateName?.let { "Dartzee - $templateName" } ?: gameType.getDescription(gameParams)
        val playerDesc = getPlayerDesc()

        var matchDesc = ""
        if (localMatchId > -1) {
            matchDesc = "#$localMatchId (Game $matchOrdinal)"
        }

        return arrayOf(localId, gameTypeDesc, playerDesc, dtStart, dtFinish, matchDesc)
    }

    private fun getPlayerDesc() = participants.joinToString()

    fun addParticipant(rs: ResultSet) {
        val playerName = rs.getString("Name")
        val finishPos = rs.getInt("FinishingPosition")
        val resigned = rs.getBoolean("Resigned")
        val teamId = rs.getString("TeamId")

        val existing = if (teamId.isNotEmpty()) participants.find { it.teamId == teamId } else null
        if (existing != null) {
            existing.playerName = "${existing.playerName} & $playerName"
        } else {
            participants.add(ParticipantWrapper(playerName, finishPos, resigned, teamId))
        }
    }

    companion object {
        fun factoryFromResultSet(localId: Long, rs: ResultSet): ReportResultWrapper {
            val gameType = GameType.valueOf(rs.getString("GameType"))
            val gameParams = rs.getString("GameParams")
            val dtStart = rs.getTimestamp("DtCreation")
            val dtFinish = rs.getTimestamp("DtFinish")
            val localMatchId = rs.getLong("LocalMatchId")
            val matchOrdinal = rs.getInt("MatchOrdinal")
            val templateName = rs.getString("TemplateName")

            return ReportResultWrapper(
                localId,
                gameType,
                gameParams,
                dtStart,
                dtFinish,
                localMatchId,
                matchOrdinal,
                templateName,
            )
        }

        fun getTableRowsFromWrappers(wrappers: List<ReportResultWrapper>) =
            wrappers.map { it.getTableRow() }
    }
}
