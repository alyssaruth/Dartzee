package burlton.dartzee.code.achievements.x01

import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_CHECKOUT_COMPLETENESS
import burlton.dartzee.code.achievements.AbstractAchievementRowPerGame
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.ResourceCache
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.URL
import kotlin.streams.toList

class AchievementX01CheckoutCompleteness : AbstractAchievementRowPerGame()
{
    override val name = "Completionist"
    override val desc = "Total unique doubles checked out on in X01"
    override val achievementRef = ACHIEVEMENT_REF_X01_CHECKOUT_COMPLETENESS
    override val gameType = GAME_TYPE_X01

    override val redThreshold = 1
    override val orangeThreshold = 5
    override val yellowThreshold = 10
    override val greenThreshold = 15
    override val blueThreshold = 20
    override val pinkThreshold = 21
    override val maxValue = 21

    var hitDoubles = mutableListOf<Int>()

    override fun getIconURL(): URL = ResourceCache.URL_ACHIEVEMENT_CHECKOUT_COMPLETENESS

    override fun getBreakdownColumns() = listOf("Double", "Game", "Date Achieved")
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf(a.achievementCounter, a.localGameIdEarned, a.dtLastUpdate)
    override fun isUnbounded() = false

    override fun populateForConversion(playerIds: String)
    {
        val tempTable = DatabaseUtil.createTempTable("PlayerCheckouts", "PlayerId VARCHAR(36), Score INT, GameId VARCHAR(36), DtAchieved TIMESTAMP")
                      ?: return

        var sb = StringBuilder()

        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT pt.PlayerId, d.Score, g.RowId, d.DtCreation")
        sb.append(" FROM Dart d, Participant pt, Game g")
        sb.append(" WHERE d.Multiplier = 2")
        sb.append(" AND d.StartingScore = (d.Score * d.Multiplier)")
        sb.append(" AND d.ParticipantId = pt.RowId")
        sb.append(" AND d.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = $GAME_TYPE_X01")
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
        sb.append("     AND zz2.DtAchieved < zz1.DtAchieved")
        sb.append(")")

        DatabaseUtil.executeQuery(sb).use { rs ->
            while (rs.next())
            {
                val playerId = rs.getString("PlayerId")
                val score = rs.getInt("Score")
                val gameId = rs.getString("GameId")
                val dtAchieved = rs.getTimestamp("DtAchieved")

                AchievementEntity.factoryAndSave(ACHIEVEMENT_REF_X01_CHECKOUT_COMPLETENESS, playerId, gameId, score, "", dtAchieved)
            }
        }

        DatabaseUtil.dropTable(tempTable)
    }

    override fun initialiseFromDb(achievementRows: List<AchievementEntity>, player: PlayerEntity?)
    {
        super.initialiseFromDb(achievementRows, player)

        hitDoubles = achievementRows.stream().map{row -> row.achievementCounter}.toList().toMutableList()
    }

    override fun changeIconColor(img : BufferedImage, newColor: Color)
    {
        if (isLocked())
        {
            super.changeIconColor(img, newColor)
            return
        }

        for (x in 0 until img.width)
        {
            for (y in 0 until img.height)
            {
                if (Color(img.getRGB(x, y)) == Color.BLACK)
                {
                    img.setRGB(x, y, newColor.darker().rgb)
                }
                else
                {
                    val red = Color(img.getRGB(x, y)).red
                    if (hitDoubles.contains(red))
                    {
                        img.setRGB(x, y, newColor.rgb)
                    }
                    else
                    {
                        val transparent = Color(0, true)
                        img.setRGB(x, y, transparent.rgb)
                    }
                }
            }
        }
    }

}