package dartzee.achievements.dartzee

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.appendPlayerSql
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.db.EntityName
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache
import java.net.URL

const val DARTZEE_BEST_GAME_MIN_ROUNDS = 5

/**
 * Measured as average-per-round, to prevent "gaming" it by having a massive set of easy rules
 */
class AchievementDartzeeBestGame: AbstractAchievement()
{
    override val name = "Yahtzee!"
    override val desc = "Best round average in Dartzee (at least 5 rules)"
    override val achievementType = AchievementType.DARTZEE_BEST_GAME
    override val redThreshold = 10
    override val orangeThreshold = 20
    override val yellowThreshold = 30
    override val greenThreshold = 40
    override val blueThreshold = 50
    override val pinkThreshold = 60
    override val maxValue = 180
    override val gameType = GameType.DARTZEE

    override fun populateForConversion(playerIds: List<String>, database: Database)
    {
        val dartzeeGames = database.createTempTable("DartzeeGames", "GameId VARCHAR(36), RoundCount INT")

        var sb = StringBuilder()
        sb.append(" INSERT INTO $dartzeeGames")
        sb.append(" SELECT g.RowId, COUNT(1) + 1")
        sb.append(" FROM ${EntityName.Game} g, ${EntityName.DartzeeRule} dr")
        sb.append(" WHERE dr.EntityId = g.RowId")
        sb.append(" AND dr.EntityName = '${EntityName.Game}'")
        sb.append(" AND g.GameType = '${GameType.DARTZEE}'")
        sb.append(" GROUP BY g.RowId")
        sb.append(" HAVING COUNT(1) >= $DARTZEE_BEST_GAME_MIN_ROUNDS")

        if (!database.executeUpdate(sb)) return

        val allScores = database.createTempTable("DartzeeScores", "PlayerId VARCHAR(36), DtAchieved TIMESTAMP, GameId VARCHAR(36), ComputedScore INT")

        sb = StringBuilder()
        sb.append(" INSERT INTO $allScores")
        sb.append(" SELECT pt.PlayerId, pt.DtFinished, zz.GameId, pt.FinalScore / zz.RoundCount")
        sb.append(" FROM ${EntityName.Participant} pt, $dartzeeGames zz")
        sb.append(" WHERE pt.GameId = zz.GameId")
        sb.append(" AND pt.FinalScore > -1")
        appendPlayerSql(sb, playerIds)

        if (!database.executeUpdate(sb)) return

        sb = StringBuilder()
        sb.append(" SELECT *")
        sb.append(" FROM $allScores")
        sb.append(" ORDER BY ComputedScore DESC, DtAchieved")

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(rs, database, achievementType, oneRowPerPlayer = true, achievementCounterFn = { rs.getInt("ComputedScore") })
        }
    }

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_DARTZEE_BEST_GAME
}