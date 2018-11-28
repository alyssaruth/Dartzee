package burlton.dartzee.code.achievements

import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.utils.DatabaseUtil

class AchievementX01CheckoutCompleteness : AbstractAchievement()
{
    override val name = "Checkout Completeness"
    override val achievementRef = ACHIEVEMENT_REF_X01_CHECKOUT_COMPLETENESS

    override fun populateForConversion(playerIds: String)
    {
        val tempTable = DatabaseUtil.createTempTable("PlayerCheckouts", "PlayerId INT, Score INT, GameId INT, DtAchieved TIMESTAMP")
                      ?: return


        var sb = StringBuilder()

        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT pt.PlayerId, d.Score, g.RowId, d.DtCreation")
        sb.append(" FROM Dart d, Round rnd, Participant pt, Game g")
        sb.append(" WHERE d.Multiplier = 2")
        sb.append(" AND d.StartingScore = (d.Score * d.Multiplier)")
        sb.append(" AND d.RoundId = rnd.RowId")
        sb.append(" AND rnd.ParticipantId = pt.RowId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = ${GameEntity.GAME_TYPE_X01}")
        if (!playerIds.isEmpty())
        {
            sb.append(" AND pt.PlayerId IN ($playerIds)")
        }

        if (!DatabaseUtil.executeUpdate("" + sb))
            return

        sb = StringBuilder()
        sb.append(" SELECT PlayerId, Score, GameId, DtAchieved")
        sb.append(" FROM $tempTable zz1")
        sb.append(" WHERE NOT EXISTS (")
        sb.append("     SELECT 1")
        sb.append("     FROM $tempTable zz2")
        sb.append("     WHERE zz1.PlayerId = zz2.PlayerId")
        sb.append("     AND zz1.Score = zz2.Score")
        sb.append("     AND (zz2.DtAchieved < zz1.DtAchieved OR (zz1.DtAchieved = zz2.DtAchieved AND zz2.GameId < zz1.GameId))")
        sb.append(")")

        DatabaseUtil.executeQuery(sb).use { rs ->
            while (rs.next())
            {
                val playerId = rs.getLong("PlayerId")
                val score = rs.getInt("Score")
                val gameId = rs.getLong("GameId")
                val dtAchieved = rs.getTimestamp("DtAchieved")

                AchievementEntity.factoryAndSave(ACHIEVEMENT_REF_X01_CHECKOUT_COMPLETENESS, playerId, gameId, score, dtAchieved)
            }
        }
    }
}