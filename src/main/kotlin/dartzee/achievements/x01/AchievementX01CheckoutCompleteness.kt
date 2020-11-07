package dartzee.achievements.x01

import dartzee.achievements.*
import dartzee.core.bean.paint
import dartzee.db.AchievementEntity
import dartzee.game.GameType
import dartzee.db.PlayerEntity
import dartzee.utils.DartsColour
import dartzee.utils.Database
import dartzee.utils.ResourceCache
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.URL

class AchievementX01CheckoutCompleteness : AbstractMultiRowAchievement()
{
    override val name = "Completionist"
    override val desc = "Total unique doubles checked out on in X01"
    override val achievementRef = ACHIEVEMENT_REF_X01_CHECKOUT_COMPLETENESS
    override val gameType = GameType.X01

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
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf(a.achievementCounter, a.localGameIdEarned, a.dtAchieved)
    override fun isUnbounded() = false

    override fun populateForConversion(playerIds: List<String>, database: Database)
    {
        ensureX01RoundsTableExists(playerIds, database)

        val tempTable = database.createTempTable("PlayerCheckouts", "PlayerId VARCHAR(36), Score INT, GameId VARCHAR(36), DtAchieved TIMESTAMP")
                      ?: return

        var sb = StringBuilder()

        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT PlayerId, LastDartScore, GameId, DtRoundFinished")
        sb.append(" FROM $X01_ROUNDS_TABLE")
        sb.append(" WHERE LastDartMultiplier = 2")
        sb.append(" AND RemainingScore = 0")

        if (!database.executeUpdate("" + sb))
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

        database.executeQuery(sb).use { rs ->
            bulkInsertFromResultSet(rs, database, achievementRef, achievementCounterFn = { rs.getInt("Score") })
        }
    }

    override fun initialiseFromDb(achievementRows: List<AchievementEntity>, player: PlayerEntity?)
    {
        super.initialiseFromDb(achievementRows, player)

        hitDoubles = achievementRows.map { it.achievementCounter }.toMutableList()
    }

    override fun changeIconColor(img : BufferedImage, newColor: Color)
    {
        if (isLocked())
        {
            super.changeIconColor(img, newColor)
            return
        }

        img.paint {
            val current = Color(img.getRGB(it.x, it.y), true)
            when
            {
                current == Color.BLACK -> newColor.darker()
                hitDoubles.contains(current.red) -> newColor
                else -> DartsColour.TRANSPARENT
            }
        }
    }

}