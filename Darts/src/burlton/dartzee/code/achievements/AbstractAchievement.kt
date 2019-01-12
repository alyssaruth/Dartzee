package burlton.dartzee.code.achievements

import burlton.core.code.util.Debug
import burlton.core.code.util.StringUtil
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.ResourceCache
import burlton.desktopcore.code.util.DateStatics.Companion.START_OF_TIME
import burlton.desktopcore.code.util.formatAsDate
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import kotlin.streams.toList

abstract class AbstractAchievement
{
    abstract val name : String
    abstract val desc : String
    abstract val achievementRef : Int
    abstract val redThreshold : Int
    abstract val orangeThreshold : Int
    abstract val yellowThreshold : Int
    abstract val greenThreshold : Int
    abstract val blueThreshold : Int
    abstract val pinkThreshold : Int
    abstract val maxValue : Int

    var attainedValue = -1
    var gameIdEarned = -1L
    var dtLatestUpdate = START_OF_TIME

    var breakdownRows = mutableListOf<AchievementEntity>()

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
    abstract fun getIconURL() : URL?

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
            Debug.stackTrace("Got ${achievementRows.size} rows (expected 1) for achievement $achievementRef and player ${achievementRows.first().playerId}")
        }

        val achievementRow = achievementRows.first()
        attainedValue = achievementRow.achievementCounter
        gameIdEarned = achievementRow.gameIdEarned
        dtLatestUpdate = achievementRow.dtLastUpdate
    }

    fun getScore() : Int
    {
        val color = getColor(false)
        return when (color)
        {
            Color.MAGENTA -> 6
            Color.CYAN -> 5
            Color.GREEN -> 4
            Color.YELLOW -> 3
            Color.ORANGE -> 2
            Color.RED -> 1
            else -> 0
        }
    }

    fun getColor(highlighted : Boolean) : Color
    {
        val col = if (isDecreasing())
        {
            when (attainedValue)
            {
                -1 -> Color.GRAY
                in redThreshold+1 until Int.MAX_VALUE -> Color.GRAY
                in orangeThreshold+1 until redThreshold+1 -> Color.RED
                in yellowThreshold+1 until orangeThreshold+1 -> Color.ORANGE
                in greenThreshold+1 until yellowThreshold+1 -> Color.YELLOW
                in blueThreshold+1 until greenThreshold+1 -> Color.GREEN
                in pinkThreshold+1 until blueThreshold+1 -> Color.CYAN
                else -> Color.MAGENTA
            }
        }
        else
        {
            when (attainedValue)
            {
                in Int.MIN_VALUE until redThreshold -> Color.GRAY
                in redThreshold until orangeThreshold -> Color.RED
                in orangeThreshold until yellowThreshold -> Color.ORANGE
                in yellowThreshold until greenThreshold -> Color.YELLOW
                in greenThreshold until blueThreshold -> Color.GREEN
                in blueThreshold until pinkThreshold -> Color.CYAN
                else -> Color.MAGENTA
            }
        }

        if (highlighted
          && !isLocked())
        {
            return col.darker()
        }

        return col
    }

    fun getAngle() : Double
    {
        return getAngle(attainedValue)
    }
    fun getAngle(attainedValue : Int) : Double
    {
        if (attainedValue == -1)
        {
            return 0.0
        }

        return if (!isDecreasing())
        {
            360 * attainedValue.toDouble() / maxValue
        }
        else
        {
            val denom = redThreshold - maxValue + 1
            val num = Math.max(redThreshold - attainedValue + 1, 0)

            360 * num / denom.toDouble()
        }
    }

    fun isLocked() : Boolean
    {
        if (attainedValue == -1)
        {
            return true
        }

        return if (isDecreasing())
        {
            attainedValue > redThreshold
        }
        else
        {
            attainedValue < redThreshold
        }
    }

    fun getIcon(highlighted : Boolean) : BufferedImage?
    {
        var iconURL = getIconURL()
        if (iconURL == null)
        {
            Debug.stackTrace("Icon URL is null for achievement [$name]")
            return null
        }

        if (isLocked())
        {
            iconURL = ResourceCache.URL_ACHIEVEMENT_LOCKED
        }

        val bufferedImage = ImageIO.read(iconURL)
        changeIconColor(bufferedImage, getColor(highlighted).darker())

        return bufferedImage
    }
    protected open fun changeIconColor(img : BufferedImage, newColor: Color)
    {
        for (x in 0 until img.width)
        {
            for (y in 0 until img.height)
            {
                if (Color(img.getRGB(x, y)) == Color.BLACK)
                {
                    img.setRGB(x, y, newColor.rgb)
                }
            }
        }
    }

    override fun toString(): String
    {
        return name
    }

    open fun isUnbounded() : Boolean
    {
        return false
    }

    fun getProgressDesc() : String
    {
        var progressStr = "$attainedValue"
        if (!isUnbounded())
        {
            progressStr += "/$maxValue"
        }

        return progressStr
    }

    open fun isDecreasing() : Boolean
    {
        return false
    }

    fun getExtraDetails() : String
    {
        var ret = if (isUnbounded())
        {
            "Last updated on ${dtLatestUpdate.formatAsDate()}"
        }
        else
        {
            "Earned on ${dtLatestUpdate.formatAsDate()}"
        }

        if (gameIdEarned > -1)
        {
            ret += " in Game #$gameIdEarned"
        }

        return ret
    }

}
