package dartzee.achievements

import dartzee.core.bean.paint
import dartzee.core.util.DateStatics
import dartzee.core.util.formatAsDate
import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.utils.DartsColour
import dartzee.utils.Database
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.ResourceCache
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.table.DefaultTableModel

abstract class AbstractAchievement {
    abstract val name: String
    abstract val desc: String
    abstract val achievementType: AchievementType
    abstract val redThreshold: Int
    abstract val orangeThreshold: Int
    abstract val yellowThreshold: Int
    abstract val greenThreshold: Int
    abstract val blueThreshold: Int
    abstract val pinkThreshold: Int
    abstract val maxValue: Int
    abstract val gameType: GameType?
    abstract val allowedForTeams: Boolean

    open val allowedForIndividuals = true

    open val usesTransactionalTablesForConversion = true

    var attainedValue = -1
    var gameIdEarned = ""
    var localGameIdEarned = -1L
    var dtLatestUpdate = DateStatics.START_OF_TIME
    var player: PlayerEntity? = null

    var tmBreakdown: DefaultTableModel? = null

    fun runConversion(playerIds: List<String>, database: Database = mainDatabase) {
        val sb = StringBuilder()
        sb.append(" DELETE FROM Achievement")
        sb.append(" WHERE AchievementType = '$achievementType'")
        appendPlayerSql(sb, playerIds, null)

        if (!database.executeUpdate("" + sb)) {
            return
        }

        populateForConversion(playerIds, database)
    }

    abstract fun populateForConversion(playerIds: List<String>, database: Database = mainDatabase)

    abstract fun getIconURL(): URL?

    /** Basic init will be the same for most achievements - get the value from the single row */
    open fun initialiseFromDb(achievementRows: List<AchievementEntity>, player: PlayerEntity?) {
        if (achievementRows.isEmpty()) {
            return
        }

        if (achievementRows.size > 1) {
            logger.error(
                CODE_SQL_EXCEPTION,
                "Got ${achievementRows.size} rows (expected 1) for achievement $achievementType and player ${achievementRows.first().playerId}",
            )
        }

        val achievementRow = achievementRows.first()
        attainedValue = achievementRow.achievementCounter
        gameIdEarned = achievementRow.gameIdEarned
        localGameIdEarned = achievementRow.localGameIdEarned
        dtLatestUpdate = achievementRow.dtAchieved

        this.player = player
    }

    fun getScore(): Int {
        val color = getColor(false)
        return when (color) {
            Color.MAGENTA -> 6
            Color.CYAN -> 5
            Color.GREEN -> 4
            Color.YELLOW -> 3
            DartsColour.COLOUR_ACHIEVEMENT_ORANGE -> 2
            Color.RED -> 1
            else -> 0
        }
    }

    fun getColor(highlighted: Boolean): Color {
        val col =
            if (isDecreasing()) {
                when (attainedValue) {
                    -1 -> Color.GRAY
                    in redThreshold + 1..Int.MAX_VALUE -> Color.GRAY
                    in orangeThreshold + 1 until redThreshold + 1 -> Color.RED
                    in yellowThreshold + 1 until orangeThreshold + 1 ->
                        DartsColour.COLOUR_ACHIEVEMENT_ORANGE
                    in greenThreshold + 1 until yellowThreshold + 1 -> Color.YELLOW
                    in blueThreshold + 1 until greenThreshold + 1 -> Color.GREEN
                    in pinkThreshold + 1 until blueThreshold + 1 -> Color.CYAN
                    else -> Color.MAGENTA
                }
            } else {
                when (attainedValue) {
                    in Int.MIN_VALUE until redThreshold -> Color.GRAY
                    in redThreshold until orangeThreshold -> Color.RED
                    in orangeThreshold until yellowThreshold ->
                        DartsColour.COLOUR_ACHIEVEMENT_ORANGE
                    in yellowThreshold until greenThreshold -> Color.YELLOW
                    in greenThreshold until blueThreshold -> Color.GREEN
                    in blueThreshold until pinkThreshold -> Color.CYAN
                    else -> Color.MAGENTA
                }
            }

        if (highlighted && !isLocked()) {
            return col.darker()
        }

        return col
    }

    fun getAngle() = getAngle(attainedValue)

    fun getAngle(attainedValue: Int): Double {
        if (attainedValue == -1) {
            return 0.0
        }

        return if (!isDecreasing()) {
            360 * attainedValue.toDouble() / maxValue
        } else {
            val denom = redThreshold - maxValue + 1
            val num = Math.max(redThreshold - attainedValue + 1, 0)

            360 * num / denom.toDouble()
        }
    }

    fun isLocked(): Boolean {
        if (attainedValue == -1) {
            return true
        }

        return if (isDecreasing()) {
            attainedValue > redThreshold
        } else {
            attainedValue < redThreshold
        }
    }

    fun isClickable() = gameIdEarned.isNotEmpty() || tmBreakdown != null

    fun getIcon(): BufferedImage? {
        var iconURL = getIconURL()
        if (isLocked()) {
            iconURL = ResourceCache.URL_ACHIEVEMENT_LOCKED
        }

        val bufferedImage = ImageIO.read(iconURL)
        changeIconColor(bufferedImage, getColor(false).darker())

        return bufferedImage
    }

    protected open fun changeIconColor(img: BufferedImage, newColor: Color) {
        img.paint { pt ->
            val current = Color(img.getRGB(pt.x, pt.y), true)
            if (current.red == current.blue && current.blue == current.green && current.red < 255) {
                val alpha = if (current.alpha == 255) 255 - current.red else current.alpha
                Color(newColor.red, newColor.green, newColor.blue, alpha)
            } else current
        }
    }

    override fun toString() = name

    open fun isUnbounded() = false

    fun getProgressDesc(): String {
        var progressStr = "$attainedValue"
        if (!isUnbounded()) {
            progressStr += "/$maxValue"
        }

        return progressStr
    }

    open fun isDecreasing() = false

    fun getExtraDetails(): String {
        var ret =
            if (this is AbstractMultiRowAchievement) {
                "Last updated on ${dtLatestUpdate.formatAsDate()}"
            } else {
                "Earned on ${dtLatestUpdate.formatAsDate()}"
            }

        if (!gameIdEarned.isEmpty()) {
            ret += " in Game #$localGameIdEarned"
        }

        return ret
    }

    open fun retrieveAllRows() =
        AchievementEntity().retrieveEntities("AchievementType = '$achievementType'")
}
