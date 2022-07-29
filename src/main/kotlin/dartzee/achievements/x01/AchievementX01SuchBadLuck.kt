package dartzee.achievements.x01

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.appendPlayerSql
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache.URL_ACHIEVEMENT_X01_SUCH_BAD_LUCK
import dartzee.utils.getAdjacentNumbers
import dartzee.utils.getCheckoutScores

class AchievementX01SuchBadLuck: AbstractAchievement()
{
    override val name = "Such Bad Luck"
    override val desc = "Most adjacent doubles hit when on a checkout in a game of X01"
    override val achievementType = AchievementType.X01_SUCH_BAD_LUCK
    override val gameType = GameType.X01
    override val allowedForTeams = true

    override val redThreshold = 1
    override val orangeThreshold = 2
    override val yellowThreshold = 3
    override val greenThreshold = 5
    override val blueThreshold = 7
    override val pinkThreshold = 10
    override val maxValue = 10

    override fun populateForConversion(playerIds: List<String>, database: Database)
    {
        val cols = "PlayerId VARCHAR(36), GameId VARCHAR(36), Score INT, Multiplier INT, StartingScore INT, DtLastUpdate TIMESTAMP"
        val tempTable = database.createTempTable("CheckoutDarts", cols) ?: return

        val checkoutsStr = getCheckoutScores().joinToString()

        var sb = StringBuilder()
        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT pt.PlayerId, pt.GameId, d.Score, d.Multiplier, d.StartingScore, d.DtLastUpdate")
        sb.append(" FROM Dart d, Participant pt, Game g")
        sb.append(" WHERE d.ParticipantId = pt.RowId")
        sb.append(" AND d.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = '${GameType.X01}'")
        sb.append(" AND d.StartingScore IN ($checkoutsStr)")
        appendPlayerSql(sb, playerIds)

        if (!database.executeUpdate(sb)) return

        sb = StringBuilder()
        sb.append(" SELECT PlayerId, GameId, COUNT(1) AS GameTotal, MAX(DtLastUpdate) AS DtAchieved")
        sb.append(" FROM $tempTable")
        sb.append(" WHERE StartingScore = 50 AND Multiplier = 1 AND Score = 25")

        for (i in 1..20)
        {
            val adjacents = getAdjacentNumbers(i).joinToString()
            sb.append(" OR StartingScore = ${2*i} AND Multiplier = 2 AND Score IN ($adjacents)")
        }

        sb.append(" GROUP BY PlayerId, GameId")
        sb.append(" ORDER BY COUNT(1) DESC, DtAchieved")

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(rs, database, achievementType, achievementCounterFn = { rs.getInt("GameTotal") }, oneRowPerPlayer = true)
        }
    }

    override fun getIconURL() = URL_ACHIEVEMENT_X01_SUCH_BAD_LUCK
    override fun isUnbounded() = true
}