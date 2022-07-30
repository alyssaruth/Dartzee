package dartzee.achievements.dartzee

import dartzee.achievements.AbstractMultiRowAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.appendPlayerSql
import dartzee.achievements.buildQualifyingDartzeeGamesTable
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.db.AchievementEntity
import dartzee.db.EntityName
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache

class AchievementDartzeeBingo: AbstractMultiRowAchievement()
{
    override val name = "Bingo!"
    override val desc = "Total unique last 2 digits of Dartzee scores"
    override val achievementType = AchievementType.DARTZEE_BINGO
    override val redThreshold = 10
    override val orangeThreshold = 25
    override val yellowThreshold = 40
    override val greenThreshold = 60
    override val blueThreshold = 75
    override val pinkThreshold = 100
    override val maxValue = 100
    override val gameType = GameType.DARTZEE
    override val allowedForTeams = false

    override fun populateForConversion(playerIds: List<String>, database: Database)
    {
        val dartzeeGames = buildQualifyingDartzeeGamesTable(database) ?: return

        val playerBingos = database.createTempTable("PlayerBingos", "PlayerId VARCHAR(36), BingoScore INT, Score INT, GameId VARCHAR(36), DtAchieved TIMESTAMP")
            ?: return

        var sb = StringBuilder()
        sb.append(" INSERT INTO $playerBingos")
        sb.append(" SELECT pt.PlayerId, mod(pt.FinalScore, 100), pt.FinalScore, pt.GameId, pt.DtFinished")
        sb.append(" FROM ${EntityName.Participant} pt, $dartzeeGames zz")
        sb.append(" WHERE pt.GameId = zz.GameId")
        appendPlayerSql(sb, playerIds)
        sb.append(" AND pt.FinalScore > -1")
        sb.append(" AND pt.TeamId = ''")

        if (!database.executeUpdate(sb)) return

        sb = StringBuilder()
        sb.append(" SELECT PlayerId, BingoScore, Score, GameId, DtAchieved")
        sb.append(" FROM $playerBingos zz1")
        sb.append(" WHERE NOT EXISTS (")
        sb.append("     SELECT 1")
        sb.append("     FROM $playerBingos zz2")
        sb.append("     WHERE zz1.PlayerId = zz2.PlayerId")
        sb.append("     AND zz1.BingoScore = zz2.BingoScore")
        sb.append("     AND zz2.DtAchieved < zz1.DtAchieved")
        sb.append(")")

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(rs, database, achievementType,
                achievementCounterFn = { rs.getInt("BingoScore") },
                achievementDetailFn = { rs.getInt("Score").toString() }
            )
        }
    }

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_DARTZEE_BINGO

    override fun getBreakdownColumns() = listOf("Bingo Score", "Game", "Full Score", "Date Achieved")
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf<Any>(a.achievementCounter, a.localGameIdEarned, a.achievementDetail, a.dtAchieved)
    override fun isUnbounded() = false
}