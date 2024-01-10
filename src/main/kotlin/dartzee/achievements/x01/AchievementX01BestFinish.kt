package dartzee.achievements.x01

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.appendPlayerSql
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache

class AchievementX01BestFinish : AbstractAchievement() {
    override val name = "Finisher"
    override val desc = "Highest checkout in X01"
    override val achievementType = AchievementType.X01_BEST_FINISH
    override val gameType = GameType.X01
    override val allowedForTeams = true

    override val redThreshold = 2
    override val orangeThreshold = 41
    override val yellowThreshold = 61
    override val greenThreshold = 81
    override val blueThreshold = 121
    override val pinkThreshold = 170
    override val maxValue = 170

    override val usesTransactionalTablesForConversion = false

    override fun populateForConversion(playerIds: List<String>, database: Database) {
        val sb = StringBuilder()
        sb.append(" SELECT GameId, PlayerId, Finish, DtCreation AS DtAchieved")
        sb.append(" FROM X01Finish")
        appendPlayerSql(sb, playerIds, null, "WHERE")
        sb.append(" ORDER BY Finish DESC, DtCreation")

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(
                rs,
                database,
                achievementType,
                oneRowPerPlayer = true,
                achievementCounterFn = { rs.getInt("Finish") }
            )
        }
    }

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_BEST_FINISH
}
