package dartzee.achievements.rtc

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.appendPlayerSql
import dartzee.core.obj.HashMapList
import dartzee.db.AchievementEntity
import dartzee.game.GameType
import dartzee.`object`.Dart
import dartzee.utils.Database
import dartzee.utils.ResourceCache.URL_ACHIEVEMENT_CLOCK_BEST_STREAK
import dartzee.utils.getLongestStreak

class AchievementClockBestStreak : AbstractAchievement() {
    override val achievementType = AchievementType.CLOCK_BEST_STREAK
    override val name = "Like Clockwork"
    override val desc = "Longest streak of hits in Round the Clock"
    override val gameType = GameType.ROUND_THE_CLOCK
    override val allowedForTeams = false

    override val redThreshold = 2
    override val orangeThreshold = 3
    override val yellowThreshold = 5
    override val greenThreshold = 7
    override val blueThreshold = 9
    override val pinkThreshold = 12
    override val maxValue = 20

    override fun getIconURL() = URL_ACHIEVEMENT_CLOCK_BEST_STREAK

    override fun populateForConversion(playerIds: List<String>, database: Database) {
        val sb = StringBuilder()
        sb.append(
            " SELECT pt.PlayerId, g.RowId AS GameId, pt.RowId AS ParticipantId, drt.Ordinal, drt.Score, drt.Multiplier, drt.StartingScore, drt.DtLastUpdate"
        )
        sb.append(" FROM Game g, Participant pt, Dart drt")
        sb.append(" WHERE g.GameType = '${GameType.ROUND_THE_CLOCK}'")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND pt.TeamId = ''")
        sb.append(" AND drt.ParticipantId = pt.RowId")
        sb.append(" AND drt.PlayerId = pt.PlayerId")
        appendPlayerSql(sb, playerIds)
        sb.append(" ORDER BY g.DtLastUpdate, pt.RowId, drt.RoundNumber, drt.Ordinal")

        val hmPlayerIdToDarts = HashMapList<String, Dart>()
        database.executeQuery(sb).use { rs ->
            while (rs.next()) {
                val playerId = rs.getString("PlayerId")
                val gameId = rs.getString("GameId")
                val participantId = rs.getString("ParticipantId")
                val ordinal = rs.getInt("Ordinal")
                val score = rs.getInt("Score")
                val multiplier = rs.getInt("Multiplier")
                val startingScore = rs.getInt("StartingScore")
                val dtThrown = rs.getTimestamp("DtLastUpdate")

                val drt = Dart(score, multiplier)
                drt.startingScore = startingScore
                drt.participantId = participantId
                drt.ordinal = ordinal
                drt.gameId = gameId
                drt.dtThrown = dtThrown

                hmPlayerIdToDarts.putInList(playerId, drt)
            }
        }

        hmPlayerIdToDarts.forEach { playerId, darts ->
            val streak = getLongestStreak(darts)
            val lastDart = streak.last()

            AchievementEntity.factoryAndSave(
                achievementType,
                playerId,
                lastDart.gameId,
                streak.size,
                "",
                lastDart.dtThrown,
                database
            )
        }
    }
}
