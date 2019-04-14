package burlton.dartzee.code.screen.stats.overall

import burlton.dartzee.code.achievements.getAchievementMaximum
import burlton.dartzee.code.achievements.getPlayerAchievementScore
import burlton.dartzee.code.bean.ScrollTableAchievements
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.utils.DartsColour
import burlton.desktopcore.code.util.TableUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.JProgressBar
import javax.swing.JTable
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.MatteBorder
import javax.swing.plaf.basic.BasicProgressBarUI
import javax.swing.table.TableCellRenderer

class LeaderboardAchievements : AbstractLeaderboard()
{
    private val table = ScrollTableAchievements()

    init
    {
        layout = BorderLayout(0, 0)
        table.setRowHeight(23)
        add(table)
    }

    override fun getTabName() = "Achievements"

    override fun buildTable()
    {
        val achievementRows = AchievementEntity().retrieveEntities("")

        val model = TableUtil.DefaultModel()
        model.addColumn("")
        model.addColumn("Player")
        model.addColumn("Achievements")

        val players = PlayerEntity.retrievePlayers("", false)
        players.forEach{

            val score = getPlayerAchievementScore(achievementRows, it)
            val row = arrayOf(it.getFlag(), it, score)

            model.addRow(row)
        }

        table.model = model
        table.setColumnWidths("25;200")
        table.sortBy(2, true)

        val renderer = ProgressBarRenderer()
        renderer.minimum = 0
        renderer.maximum = getAchievementMaximum()
        renderer.isStringPainted = true
        renderer.font = Font("Tahoma", Font.BOLD, 12)
        renderer.ui = AchievementProgressUI()
        val borderMargin = EmptyBorder(1, 1, 1, 1)
        val lineBorder = MatteBorder(1, 1, 1, 1, Color.BLACK)
        renderer.border = CompoundBorder(borderMargin, lineBorder)
        table.getColumn(2).cellRenderer = renderer
    }


    private inner class ProgressBarRenderer: JProgressBar(), TableCellRenderer
    {
        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component
        {
            string = "$value/$maximum"

            val score = value as Int
            setValue(score)

            var col = getColorForScore(score, maximum)
            if (isSelected)
            {
                col = col.darker()
            }


            foreground = col.darker()
            background = col

            return this
        }

        fun getColorForScore(score: Int, maximum: Int) : Color
        {
            return when(score)
            {
                in 5*maximum/6 until Int.MAX_VALUE -> Color.MAGENTA
                in 4*maximum/6 until 5*maximum/6 -> Color.CYAN
                in 3*maximum/6 until 4*maximum/6 -> Color.GREEN
                in 2*maximum/6 until 3*maximum/6 -> Color.YELLOW
                in maximum/6 until 2*maximum/6 -> DartsColour.COLOUR_ACHIEVEMENT_ORANGE
                in 1 until maximum/6 -> Color.RED
                else -> Color.GRAY
            }

        }

    }

    private class AchievementProgressUI : BasicProgressBarUI()
    {
        override fun getSelectionBackground(): Color
        {
            return progressBar.foreground
        }

        override fun getSelectionForeground(): Color
        {
            return progressBar.foreground.darker()
        }
    }

}
