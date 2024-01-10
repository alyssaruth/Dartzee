package dartzee.achievements.x01

import dartzee.achievements.AbstractMultiRowAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.achievements.getPlayerSql
import dartzee.db.AchievementEntity
import dartzee.db.EntityName
import dartzee.game.GameType
import dartzee.`object`.Dart
import dartzee.utils.Database
import dartzee.utils.ResourceCache
import java.sql.ResultSet

class AchievementX01StylishFinish : AbstractMultiRowAchievement() {
    override val name = "Stylish Finish"
    override val desc = "Finishes that involved hitting another double or treble"
    override val achievementType = AchievementType.X01_STYLISH_FINISH
    override val redThreshold = 1
    override val orangeThreshold = 5
    override val yellowThreshold = 10
    override val greenThreshold = 25
    override val blueThreshold = 50
    override val pinkThreshold = 100
    override val maxValue = 100
    override val gameType = GameType.X01
    override val allowedForTeams = true

    override fun getBreakdownColumns() = listOf("Finish", "Method", "Game", "Date Achieved")

    override fun getBreakdownRow(a: AchievementEntity) =
        arrayOf<Any>(a.achievementCounter, a.achievementDetail, a.localGameIdEarned, a.dtAchieved)

    override fun populateForConversion(playerIds: List<String>, database: Database) {
        val tmp1 =
            database.createTempTable(
                "MultiDartFinishes",
                "PlayerId VARCHAR(36), GameId VARCHAR(36), ParticipantId VARCHAR(36), RoundNumber INT, DtAchieved TIMESTAMP"
            ) ?: return

        database.executeUpdate(
            """
            INSERT INTO $tmp1
            SELECT pt.PlayerId, pt.GameId, pt.RowId, drt.RoundNumber, drt.DtCreation
            FROM ${EntityName.Dart} drt, ${EntityName.Participant} pt, ${EntityName.Game} g
            WHERE drt.ParticipantId = pt.RowId
              AND drt.PlayerId = pt.PlayerId
              AND pt.GameId = g.RowId
              AND g.GameType = '${GameType.X01}'
              AND drt.Ordinal > 1
              AND drt.Multiplier = 2
              AND drt.StartingScore = (drt.Score * drt.Multiplier)
              ${getPlayerSql(playerIds)}
        """
                .trimIndent()
        )

        val drtTmp =
            database.createTempTable(
                "RelevantDarts",
                "StartingScore INT, Score INT, Multiplier INT, ParticipantId VARCHAR(36), RoundNumber INT, Ordinal INT"
            ) ?: return

        database.executeUpdate(
            """
            INSERT INTO $drtTmp
            SELECT StartingScore, Score, Multiplier, drt.ParticipantId, drt.RoundNumber, drt.Ordinal
            FROM ${EntityName.Dart} drt, $tmp1 zz
            WHERE zz.ParticipantId = drt.ParticipantId
            AND zz.PlayerId = drt.PlayerId
            AND zz.RoundNumber = drt.RoundNumber
        """
                .trimIndent()
        )

        database.executeUpdate("CREATE INDEX PtId_RoundNo ON $drtTmp (ParticipantId, RoundNumber)")

        database
            .executeQuery(
                """
            SELECT zz.PlayerId, zz.GameId, zz.ParticipantId, drt.StartingScore, zz.DtAchieved,
                drt.Score AS DartOneScore, drt.Multiplier AS DartOneMultiplier,
                drt2.Score AS DartTwoScore, drt2.Multiplier AS DartTwoMultiplier,
                drt3.Score AS DartThreeScore, drt3.Multiplier AS DartThreeMultiplier
            FROM $tmp1 zz, $drtTmp drt, $drtTmp drt2
            LEFT JOIN $drtTmp drt3 ON 
                drt3.Ordinal = 3 AND drt3.RoundNumber = drt2.RoundNumber AND drt3.ParticipantId = drt2.ParticipantId
            WHERE zz.ParticipantId = drt.ParticipantId
              AND zz.RoundNumber = drt.RoundNumber
              AND drt.Ordinal = 1
              AND zz.ParticipantId = drt2.ParticipantId
              AND zz.RoundNumber = drt2.RoundNumber
              AND drt2.Ordinal = 2
              AND (drt.Multiplier > 1 OR (drt2.Multiplier > 1 AND drt3.Multiplier IS NOT NULL))
        """
                    .trimIndent()
            )
            .use { rs ->
                bulkInsertFromResultSet(
                    rs,
                    database,
                    achievementType,
                    achievementCounterFn = { rs.getInt("StartingScore") },
                    achievementDetailFn = { extractMethodStr(rs) }
                )
            }
    }

    private fun extractMethodStr(rs: ResultSet) =
        listOfNotNull(extractDart(rs, "One"), extractDart(rs, "Two"), extractDart(rs, "Three"))
            .joinToString()

    private fun extractDart(rs: ResultSet, numberDesc: String): Dart? {
        val score = rs.getInt("Dart${numberDesc}Score")
        if (rs.wasNull()) return null
        val multiplier = rs.getInt("Dart${numberDesc}Multiplier")

        return Dart(score, multiplier)
    }

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_X01_STYLISH_FINISH
}
