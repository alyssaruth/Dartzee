package dartzee.achievements.dartzee

import dartzee.achievements.AbstractMultiRowAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.appendPlayerSql
import dartzee.achievements.buildQualifyingDartzeeGamesTable
import dartzee.achievements.bulkInsertFromResultSet
import dartzee.db.AchievementEntity
import dartzee.db.DartzeeRuleEntity
import dartzee.db.EntityName
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache
import java.sql.ResultSet

class AchievementDartzeeUnderPressure : AbstractMultiRowAchievement() {
    override val name = "Under Pressure"
    override val desc = "Games finished by passing the hardest rule (at least 5 rounds)"
    override val achievementType = AchievementType.DARTZEE_UNDER_PRESSURE
    override val redThreshold = 1
    override val orangeThreshold = 2
    override val yellowThreshold = 5
    override val greenThreshold = 10
    override val blueThreshold = 20
    override val pinkThreshold = 40
    override val maxValue = 40
    override val gameType = GameType.DARTZEE
    override val allowedForTeams = true

    override fun populateForConversion(playerIds: List<String>, database: Database) {
        val dartzeeGames = buildQualifyingDartzeeGamesTable(database) ?: return

        val dartzeeGamesHardestRule =
            database.createTempTable(
                "DartzeeGamesHardestRule",
                "GameId VARCHAR(36), RuleId VARCHAR(36), RuleNumber INT"
            )

        var sb = StringBuilder()
        sb.append(" INSERT INTO $dartzeeGamesHardestRule")
        sb.append(" SELECT zz.GameId, dr.RowId, dr.Ordinal")
        sb.append(" FROM $dartzeeGames zz, ${EntityName.DartzeeRule} dr")
        sb.append(" WHERE dr.EntityId = zz.GameId")
        sb.append(" AND dr.EntityName = '${EntityName.Game}'")
        sb.append(" AND NOT EXISTS (")
        sb.append("     SELECT 1")
        sb.append("     FROM ${EntityName.DartzeeRule} dr2")
        sb.append("     WHERE dr2.EntityId = dr.EntityId")
        sb.append("     AND dr2.EntityName = dr.EntityName")
        sb.append("     AND dr2.Ordinal > dr.Ordinal")
        sb.append(" )")

        if (!database.executeUpdate(sb)) return

        sb = StringBuilder()
        sb.append(
            " SELECT pt.PlayerId, drr.DtCreation AS DtAchieved, zz.GameId, zz.RuleId, drr.Score"
        )
        sb.append(
            " FROM $dartzeeGamesHardestRule zz, ${EntityName.DartzeeRoundResult} drr, ${EntityName.Participant} pt"
        )
        sb.append(" LEFT OUTER JOIN ${EntityName.Team} t ON (pt.TeamId = t.RowId)")
        sb.append(" WHERE pt.GameId = zz.GameId")
        sb.append(" AND (pt.FinalScore > -1 OR t.FinalScore > -1)")
        appendPlayerSql(sb, playerIds)
        sb.append(" AND drr.ParticipantId = pt.RowId")
        sb.append(" AND drr.PlayerId = pt.PlayerId")
        sb.append(" AND drr.RuleNumber = zz.RuleNumber")
        sb.append(" AND drr.Success = true")
        sb.append(" AND drr.RoundNumber = zz.RuleNumber + 1")

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(
                rs,
                database,
                achievementType,
                achievementCounterFn = { rs.getInt("Score") },
                achievementDetailFn = { extractAchievementDetail(database, rs) }
            )
        }
    }

    private fun extractAchievementDetail(database: Database, rs: ResultSet): String {
        val ruleId = rs.getString("RuleId")
        val rule = DartzeeRuleEntity(database).retrieveForId(ruleId) ?: return ""

        val dto = rule.toDto()
        return dto.getDisplayName()
    }

    override fun getIconURL() = ResourceCache.URL_ACHIEVEMENT_DARTZEE_UNDER_PRESSURE

    override fun getBreakdownColumns() = listOf("Game", "Score", "Rule", "Date Achieved")

    override fun getBreakdownRow(a: AchievementEntity) =
        arrayOf<Any>(a.localGameIdEarned, a.achievementCounter, a.achievementDetail, a.dtAchieved)
}
