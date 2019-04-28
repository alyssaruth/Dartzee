package burlton.dartzee.code.screen.stats.overall

import burlton.dartzee.code.achievements.getAchievementMaximum
import burlton.dartzee.code.achievements.getPlayerAchievementScore
import burlton.dartzee.code.bean.PlayerTypeFilterPanel
import burlton.dartzee.code.bean.ScrollTableAchievements
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.utils.DartsColour
import burlton.desktopcore.code.util.TableUtil
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JPanel

class LeaderboardAchievements : AbstractLeaderboard()
{
    private val table = ScrollTableAchievements()
    private val panelFilters = JPanel()
    private val playerFilterPanel = PlayerTypeFilterPanel()

    init
    {
        layout = BorderLayout(0, 0)
        table.setRowHeight(23)
        add(table)

        add(panelFilters, BorderLayout.NORTH)
        panelFilters.add(playerFilterPanel)

        playerFilterPanel.addActionListener(this)
    }

    override fun getTabName() = "Achievements"

    override fun buildTable()
    {
        val achievementRows = AchievementEntity().retrieveEntities("")

        val model = TableUtil.DefaultModel()
        model.addColumn("")
        model.addColumn("Player")
        model.addColumn("Achievements")

        val players = PlayerEntity.retrievePlayers(playerFilterPanel.whereSql, false)
        players.forEach{
            val score = getPlayerAchievementScore(achievementRows, it)
            val row = arrayOf(it.getFlag(), it, score)

            model.addRow(row)
        }

        table.model = model
        table.setColumnWidths("25;200")
        table.sortBy(2, true)

        val renderer = OverallAchievementRenderer()
        renderer.maximum = getAchievementMaximum()
        table.getColumn(2).cellRenderer = renderer
    }

    class OverallAchievementRenderer: AbstractProgressBarRenderer()
    {
        override fun getScoreForValue(value: Any?) = value as Int

        override fun getColorForValue(value: Any?): Color
        {
            val score = value as Int
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
}
