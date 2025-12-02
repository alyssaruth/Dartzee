package dartzee.achievements.x01

import dartzee.achievements.AbstractMultiRowAchievement
import dartzee.achievements.X01_ROUNDS_TABLE
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.achievements.ensureX01RoundsTableExists
import dartzee.db.AchievementEntity
import dartzee.game.GameType
import dartzee.utils.Database

abstract class AbstractAchievementX01ScoreVariants(private val enforceThreeDarts: Boolean) :
    AbstractMultiRowAchievement() {
    abstract val targetScore: Int

    override val gameType = GameType.X01
    override val allowedForTeams = true

    override fun getBreakdownColumns() = listOf("Method", "Game", "Date Achieved")

    override fun getBreakdownRow(a: AchievementEntity) =
        arrayOf<Any>(a.achievementDetail, a.localGameIdEarned, a.dtAchieved)

    override fun populateForConversion(playerIds: List<String>, database: Database) {
        ensureX01RoundsTableExists(playerIds, database)
        val roundWithTargetScore =
            database.createTempTable(
                "RoundsScored$targetScore",
                "PlayerId VARCHAR(36), ParticipantId VARCHAR(36), GameId VARCHAR(36), RoundNumber INT",
            )

        var sb = StringBuilder()
        sb.append(" INSERT INTO $roundWithTargetScore")
        sb.append(" SELECT PlayerId, ParticipantId, GameId, RoundNumber")
        sb.append(" FROM $X01_ROUNDS_TABLE")
        sb.append(" WHERE StartingScore - RemainingScore = $targetScore")

        if (enforceThreeDarts) {
            sb.append(" AND TotalDartsThrown = 3")
        }

        sb.append(" AND (RemainingScore > 1 OR RemainingScore = 0 AND LastDartMultiplier = 2)")

        if (!database.executeUpdate(sb)) return

        val tempTable =
            database.createTempTable(
                "RoundsScored${targetScore}NoMisses",
                "PlayerId VARCHAR(36), ParticipantId VARCHAR(36), GameId VARCHAR(36), Ordinal INT, Score INT, Multiplier INT, RoundNumber INT, DartCount INT, DtCreation TIMESTAMP",
            )
        tempTable ?: return

        sb = StringBuilder()
        sb.append(" INSERT INTO $tempTable")
        sb.append(
            " SELECT zz.PlayerId, zz.ParticipantId, zz.GameId, d.Ordinal, d.Score, d.Multiplier, d.RoundNumber, CASE WHEN drtThird.Multiplier IS NULL THEN 2 ELSE 3 END, d.DtCreation"
        )
        sb.append(" FROM Dart d, Dart drtFirst, Dart drtSecond, $roundWithTargetScore zz")
        sb.append(" LEFT OUTER JOIN Dart drtThird ON (")
        sb.append(" drtThird.ParticipantId = zz.ParticipantId")
        sb.append(" AND drtThird.PlayerId = zz.PlayerId")
        sb.append(" AND drtThird.RoundNumber = zz.RoundNumber")
        sb.append(" AND drtThird.Ordinal = 3")
        sb.append(")")
        sb.append(" WHERE drtFirst.ParticipantId = zz.ParticipantId")
        sb.append(" AND drtFirst.PlayerId = zz.PlayerId")
        sb.append(" AND drtFirst.RoundNumber = zz.RoundNumber")
        sb.append(" AND drtSecond.ParticipantId = zz.ParticipantId")
        sb.append(" AND drtSecond.PlayerId = zz.PlayerId")
        sb.append(" AND drtSecond.RoundNumber = zz.RoundNumber")
        sb.append(" AND d.RoundNumber = zz.RoundNumber")
        sb.append(" AND d.ParticipantId = zz.ParticipantId")
        sb.append(" AND d.PlayerId = zz.PlayerId")
        sb.append(" AND drtFirst.Ordinal = 1")
        sb.append(" AND drtSecond.Ordinal = 2")

        if (enforceThreeDarts) {
            sb.append(" AND drtFirst.Multiplier > 0")
            sb.append(" AND drtSecond.Multiplier > 0")
            sb.append(" AND drtThird.Multiplier > 0")
        }

        if (!database.executeUpdate(sb)) return

        database.executeUpdate(
            "CREATE INDEX ${tempTable}_PlayerId_ParticipantId_RoundNumber ON $tempTable(PlayerId, ParticipantId, RoundNumber)"
        )
        val tempTableTwo =
            database.createTempTable(
                "RoundsScored${targetScore}Flat",
                "PlayerId VARCHAR(36), GameId VARCHAR(36), DtAchieved TIMESTAMP, Method VARCHAR(100)",
            )

        sb = StringBuilder()
        sb.append(" INSERT INTO $tempTableTwo")
        sb.append(
            " SELECT highestDart.PlayerId, highestDart.GameId, highestDart.DtCreation, ${getThreeDartMethodSqlStr()} AS Method"
        )
        sb.append(" FROM $tempTable highestDart, $tempTable mediumDart")
        sb.append(" LEFT OUTER JOIN $tempTable lowestDart ON (")
        sb.append(" mediumDart.ParticipantId = lowestDart.ParticipantId")
        sb.append(" AND mediumDart.PlayerId = lowestDart.PlayerId")
        sb.append(" AND mediumDart.RoundNumber = lowestDart.RoundNumber")
        sb.append(" AND (${getDartHigherThanSql("mediumDart", "lowestDart")})")
        sb.append(" )")
        sb.append(" WHERE highestDart.ParticipantId = mediumDart.ParticipantId")
        sb.append(" AND highestDart.PlayerId = mediumDart.PlayerId")
        sb.append(" AND highestDart.RoundNumber = mediumDart.RoundNumber")
        sb.append(" AND (${getDartHigherThanSql("highestDart", "mediumDart")})")
        sb.append(" AND (lowestDart.Ordinal = 1 OR mediumDart.Ordinal = 1 OR highestDart.Ordinal = 1)")
        sb.append(" AND (lowestDart.Ordinal = 2 OR mediumDart.Ordinal = 2 OR highestDart.Ordinal = 2)")
        sb.append(" AND (lowestDart.Ordinal = highestDart.DartCount OR mediumDart.Ordinal = highestDart.DartCount OR highestDart.Ordinal = highestDart.DartCount)")
        sb.append(
            " GROUP BY highestDart.PlayerId, highestDart.GameId, highestDart.DtCreation, ${getThreeDartMethodSqlStr()}"
        )

        if (!database.executeUpdate(sb)) return

        sb = StringBuilder()
        sb.append(" SELECT PlayerId, GameId, DtAchieved, Method")
        sb.append(" FROM $tempTableTwo zz")
        sb.append(" WHERE NOT EXISTS")
        sb.append(" (")
        sb.append("     SELECT 1")
        sb.append("     FROM $tempTableTwo zz2")
        sb.append("     WHERE zz.PlayerId = zz2.PlayerId")
        sb.append("     AND zz.Method = zz2.Method")
        sb.append("     AND zz2.DtAchieved < zz.DtAchieved")
        sb.append(" )")

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(
                rs,
                database,
                achievementType,
                achievementDetailFn = { rs.getString("Method") },
            )
        }
    }

    private fun getDartHigherThanSql(hAlias: String, lAlias: String): String {
        val sb = StringBuilder()

        sb.append(
            "($hAlias.Score * $hAlias.Multiplier) > ($lAlias.Score * $lAlias.Multiplier)"
        ) // Higher score outright
        sb.append(
            " OR (($hAlias.Score * $hAlias.Multiplier) = ($lAlias.Score * $lAlias.Multiplier) AND $hAlias.Multiplier > $lAlias.Multiplier)"
        )
        sb.append(
            " OR ($hAlias.Score = $lAlias.Score AND $hAlias.Multiplier = $lAlias.Multiplier AND $hAlias.Ordinal > $lAlias.Ordinal)"
        )

        return sb.toString()
    }

    private fun getThreeDartMethodSqlStr() =
        "${getDartStrSql("highestDart")} || ', ' || ${getDartStrSql("mediumDart")} || ${getDartStrSql("lowestDart", true)}"

    private fun getDartStrSql(alias: String, nullable: Boolean = false) =
        if (nullable)
            "CASE WHEN $alias.Multiplier IS NULL THEN '' WHEN $alias.Multiplier = 0 THEN '' ELSE ', ' || ${dartStrSql(alias)} END"
        else dartStrSql(alias)

    private fun dartStrSql(alias: String) =
        "${getDartMultiplierStrSql(alias)} || ${getDartScoreStrSql(alias)}"

    private fun getDartMultiplierStrSql(alias: String) =
        "CASE WHEN $alias.Multiplier = 3 THEN 'T' WHEN $alias.Multiplier = 2 THEN 'D' ELSE '' END"

    private fun getDartScoreStrSql(alias: String) =
        "RTRIM(CAST(CASE WHEN $alias.Multiplier = 0 THEN 0 ELSE $alias.Score END AS CHAR(5)))"
}
