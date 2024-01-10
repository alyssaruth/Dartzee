package dartzee.achievements.x01

import dartzee.achievements.AbstractMultiRowAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.X01_ROUNDS_TABLE
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.achievements.ensureX01RoundsTableExists
import dartzee.db.AchievementEntity
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache.URL_ACHIEVEMENT_X01_SHANGHAI

class AchievementX01Shanghai : AbstractMultiRowAchievement() {
    override val name = "Shanghai"
    override val desc = "Total number of times player has scored T20, D20, 20 (in any order)"
    override val achievementType = AchievementType.X01_SHANGHAI
    override val gameType = GameType.X01
    override val allowedForTeams = true

    override val redThreshold = 1
    override val orangeThreshold = 2
    override val yellowThreshold = 3
    override val greenThreshold = 5
    override val blueThreshold = 7
    override val pinkThreshold = 10
    override val maxValue = 10

    override fun getIconURL() = URL_ACHIEVEMENT_X01_SHANGHAI

    override fun getBreakdownColumns() = listOf("Game", "Date Achieved")

    override fun getBreakdownRow(a: AchievementEntity) =
        arrayOf<Any>(a.localGameIdEarned, a.dtAchieved)

    override fun populateForConversion(playerIds: List<String>, database: Database) {
        ensureX01RoundsTableExists(playerIds, database)

        val tempTable =
            database.createTempTable(
                "Shanghai",
                "RoundNumber INT, ParticipantId VARCHAR(36), PlayerId VARCHAR(36), GameId VARCHAR(36)"
            )

        var sb = StringBuilder()
        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT RoundNumber, ParticipantId, PlayerId, GameId")
        sb.append(" FROM $X01_ROUNDS_TABLE")
        sb.append(" WHERE TotalDartsThrown = 3")
        sb.append(" AND StartingScore - RemainingScore = 120")

        if (!database.executeUpdate("" + sb)) return

        // Cut down to where there is precisely 1 double, 1 treble and 1 single. Get the date
        // achieved too.
        sb = StringBuilder()
        sb.append(" SELECT zz.PlayerId, zz.GameId, drtDouble.DtCreation AS DtAchieved")
        sb.append(" FROM $tempTable zz, Dart drtDouble, Dart drtTreble, Dart drtSingle")
        sb.append(" WHERE zz.ParticipantId = drtDouble.ParticipantId")
        sb.append(" AND zz.ParticipantId = drtTreble.ParticipantId")
        sb.append(" AND zz.ParticipantId = drtSingle.ParticipantId")
        sb.append(" AND zz.PlayerId = drtDouble.PlayerId")
        sb.append(" AND zz.PlayerId = drtTreble.PlayerId")
        sb.append(" AND zz.PlayerId = drtSingle.PlayerId")
        sb.append(" AND zz.RoundNumber = drtDouble.RoundNumber")
        sb.append(" AND zz.RoundNumber = drtTreble.RoundNumber")
        sb.append(" AND zz.RoundNumber = drtSingle.RoundNumber")
        sb.append(" AND drtDouble.Multiplier = 2")
        sb.append(" AND drtDouble.Score = 20")
        sb.append(" AND drtTreble.Multiplier = 3")
        sb.append(" AND drtTreble.Score = 20")
        sb.append(" AND drtSingle.Multiplier = 1")
        sb.append(" AND drtSingle.Score = 20")

        database.executeQuery(sb).use { bulkInsertFromResultSet(it, database, achievementType) }
    }
}
