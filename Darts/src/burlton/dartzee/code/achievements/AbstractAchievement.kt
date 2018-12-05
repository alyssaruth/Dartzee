package burlton.dartzee.code.achievements

import burlton.core.code.util.Debug
import burlton.core.code.util.StringUtil
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.utils.DatabaseUtil
import java.awt.Color
import kotlin.streams.toList

abstract class AbstractAchievement
{
    abstract val name : String
    abstract val achievementRef : Int
    abstract val redThreshold : Int
    abstract val orangeThreshold : Int
    abstract val yellowThreshold : Int
    abstract val greenThreshold : Int
    abstract val blueThreshold : Int
    abstract val pinkThreshold : Int
    abstract val maxValue : Int

    var attainedValue = 0
    var gameIdEarned = -1L

    fun runConversion(players : MutableList<PlayerEntity>)
    {
        val playerIds = players.stream().map{p -> "" + p.rowId}.toList()
        val keys = StringUtil.toDelims(playerIds, ",")

        val sb = StringBuilder()
        sb.append(" DELETE FROM Achievement")
        sb.append(" WHERE AchievementRef = $achievementRef")
        if (!keys.isEmpty())
        {
            sb.append(" AND PlayerId IN ($keys)" )
        }

        if (!DatabaseUtil.executeUpdate("" + sb))
        {
            return
        }

        populateForConversion(keys)
    }

    abstract fun populateForConversion(playerIds : String)

    /**
     * Basic init will be the same for most achievements - get the value from the single row
     */
    open fun initialiseFromDb(achievementRows : MutableList<AchievementEntity>)
    {
        if (achievementRows.size == 0)
        {
            return
        }

        if (achievementRows.size > 1)
        {
            Debug.stackTrace("Got ${achievementRows.size} rows - only expected 1")
        }

        val achievementRow = achievementRows.first()
        attainedValue = achievementRow.achievementCounter
        gameIdEarned = achievementRow.gameIdEarned
    }

    fun getColor(highlighted : Boolean) : Color
    {
        val col = when (attainedValue)
        {
            in Int.MIN_VALUE until redThreshold -> Color.GRAY
            in redThreshold until orangeThreshold -> Color.RED
            in orangeThreshold until yellowThreshold -> Color.ORANGE
            in yellowThreshold until greenThreshold -> Color.YELLOW
            in greenThreshold until blueThreshold -> Color.GREEN
            in blueThreshold until pinkThreshold -> Color.CYAN
            else -> Color.MAGENTA
        }

        if (highlighted
          && attainedValue >= redThreshold)
        {
            return col.darker()
        }

        return col
    }

    fun getAngle() : Double
    {
        return 360*attainedValue.toDouble() / maxValue
    }


    override fun toString(): String
    {
        return name
    }
}
