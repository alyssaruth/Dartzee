package dartzee.achievements.golf

import dartzee.achievements.AbstractMultiRowAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.achievements.getPlayerSql
import dartzee.db.AchievementEntity
import dartzee.db.EntityName
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache

class AchievementGolfInBounds : AbstractMultiRowAchievement()
{
    override val name = "In Bounds"
    override val desc = "Games of 18 holes where no hole scored a 5"
    override val achievementType = AchievementType.GOLF_IN_BOUNDS
    override val redThreshold = 1
    override val orangeThreshold = 2
    override val yellowThreshold = 5
    override val greenThreshold = 10
    override val blueThreshold = 15
    override val pinkThreshold = 25
    override val maxValue = 25
    override val gameType = GameType.GOLF
    override val allowedForTeams = false

    override fun getBreakdownColumns() = listOf("Game", "Score", "Date Achieved")
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf<Any>(a.localGameIdEarned, a.achievementDetail.toInt(), a.dtAchieved)

    override fun populateForConversion(playerIds: List<String>, database: Database)
    {
        val relevantParticipants = database.createTempTable("GolfParticipants", "ParticipantId VARCHAR(36), PlayerId VARCHAR(36), GameId VARCHAR(36), DtFinished TIMESTAMP, FinalScore INT") ?: return

        database.executeUpdate("""
            INSERT INTO $relevantParticipants
            SELECT pt.RowId, pt.PlayerId, pt.GameId, pt.DtFinished, pt.FinalScore
            FROM ${EntityName.Participant} pt, ${EntityName.Game} g
            WHERE pt.GameId = g.RowId
            AND g.GameType = '$gameType'
            AND g.GameParams = '18'
            AND pt.FinalScore > -1
            AND pt.TeamId = ''
            ${getPlayerSql(playerIds)}
        """.trimIndent())

        val rounds = database.createTempTable("GolfRounds", "ParticipantId VARCHAR(36), RoundNumber INT, LastDartOrdinal INT") ?: return

        database.executeUpdate("""
            INSERT INTO $rounds
            SELECT zz.ParticipantId, drtFirst.RoundNumber, MAX(drt.Ordinal)
            FROM $relevantParticipants zz, ${EntityName.Dart} drtFirst, ${EntityName.Dart} drt
            WHERE zz.ParticipantId = drtFirst.ParticipantId
            AND zz.PlayerId = drtFirst.PlayerId
            AND drtFirst.Ordinal = 1
            AND drtFirst.ParticipantId = drt.ParticipantId
            AND drtFirst.PlayerId = drt.PlayerId
            AND drtFirst.RoundNumber = drt.RoundNumber
            GROUP BY zz.ParticipantId, drtFirst.RoundNumber
        """.trimIndent())

        val roundScores = database.createTempTable("GolfRoundScores", "ParticipantId VARCHAR(36), RoundNumber INT, Hit BOOLEAN") ?: return

        database.executeUpdate("""
            INSERT INTO $roundScores
            SELECT zz.ParticipantId, zz.RoundNumber, 
                CASE WHEN drt.Multiplier = 0 THEN FALSE
                WHEN drt.Score != drt.RoundNumber THEN FALSE
                ELSE TRUE END
            FROM $rounds zz, ${EntityName.Dart} drt
            WHERE zz.ParticipantId = drt.ParticipantId
            AND zz.RoundNumber = drt.RoundNumber
            AND zz.LastDartOrdinal = drt.Ordinal
        """.trimIndent())

        database.executeQuery("""
            SELECT pt.PlayerId, pt.GameId, pt.DtFinished AS DtAchieved, pt.FinalScore
            FROM $relevantParticipants pt
            WHERE NOT EXISTS (
                SELECT * FROM $roundScores rs
                WHERE pt.ParticipantId = rs.ParticipantId
                AND rs.Hit = FALSE
            )
        """.trimIndent()).use { rs ->
            bulkInsertFromResultSet(rs, database, achievementType, achievementDetailFn = { rs.getInt("FinalScore").toString() })
        }
    }

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_GOLF_IN_BOUNDS
}