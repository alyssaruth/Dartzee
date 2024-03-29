package dartzee.achievements.dartzee

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.appendPlayerSql
import dartzee.achievements.buildQualifyingDartzeeGamesTable
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.db.EntityName
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache

const val DARTZEE_ACHIEVEMENT_MIN_ROUNDS = 5

/** Measured as average-per-round, to prevent "gaming" it by having a massive set of easy rules */
class AchievementDartzeeBestGame : AbstractAchievement() {
    override val name = "Yahtzee!"
    override val desc = "Best round average in Dartzee (at least 5 rounds)"
    override val achievementType = AchievementType.DARTZEE_BEST_GAME
    override val redThreshold = 10
    override val orangeThreshold = 20
    override val yellowThreshold = 30
    override val greenThreshold = 40
    override val blueThreshold = 50
    override val pinkThreshold = 60
    override val maxValue = 180
    override val gameType = GameType.DARTZEE
    override val allowedForTeams = false

    override fun populateForConversion(playerIds: List<String>, database: Database) {
        val dartzeeGames = buildQualifyingDartzeeGamesTable(database) ?: return

        val allScores =
            database.createTempTable(
                "DartzeeScores",
                "PlayerId VARCHAR(36), DtAchieved TIMESTAMP, GameId VARCHAR(36), ComputedScore INT"
            )

        var sb = StringBuilder()
        sb.append(" INSERT INTO $allScores")
        sb.append(" SELECT pt.PlayerId, pt.DtFinished, zz.GameId, pt.FinalScore / zz.RoundCount")
        sb.append(" FROM ${EntityName.Participant} pt, $dartzeeGames zz")
        sb.append(" WHERE pt.GameId = zz.GameId")
        sb.append(" AND pt.FinalScore > -1")
        sb.append(" AND pt.TeamId = ''")
        appendPlayerSql(sb, playerIds)

        if (!database.executeUpdate(sb)) return

        sb = StringBuilder()
        sb.append(" SELECT *")
        sb.append(" FROM $allScores")
        sb.append(" ORDER BY ComputedScore DESC, DtAchieved")

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(
                rs,
                database,
                achievementType,
                oneRowPerPlayer = true,
                achievementCounterFn = { rs.getInt("ComputedScore") }
            )
        }
    }

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_DARTZEE_BEST_GAME
}
