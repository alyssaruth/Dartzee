package burlton.dartzee.code.achievements

import burlton.core.code.util.StringUtil
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.utils.DatabaseUtil
import kotlin.streams.toList

abstract class AbstractAchievement
{
    abstract val name : String
    abstract val achievementRef : Int

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

    override fun toString(): String
    {
        return name
    }
}
