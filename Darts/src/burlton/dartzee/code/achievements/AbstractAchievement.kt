package burlton.dartzee.code.achievements

import burlton.core.code.util.Debug
import burlton.core.code.util.StringUtil
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.ResourceCache
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
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
          && !isLocked())
        {
            return col.darker()
        }

        return col
    }

    fun getAngle() : Double
    {
        return 360*attainedValue.toDouble() / maxValue
    }

    fun isLocked() : Boolean
    {
        return attainedValue < redThreshold
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

}
