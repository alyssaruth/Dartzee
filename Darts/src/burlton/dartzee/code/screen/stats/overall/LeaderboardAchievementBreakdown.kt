package burlton.dartzee.code.screen.stats.overall

import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.achievements.getAllAchievements
import burlton.dartzee.code.bean.ScrollTableAchievements
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.desktopcore.code.util.TableUtil
import java.awt.BorderLayout
import java.awt.Color
import java.util.*
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JPanel

class LeaderboardAchievementBreakdown: AbstractLeaderboard()
{
    private val table = ScrollTableAchievements()
    private val panelFilters = JPanel()
    private val comboBox = JComboBox<AbstractAchievement>()

    init
    {
        layout = BorderLayout(0, 0)
        table.setRowHeight(23)
        add(table)

        add(panelFilters, BorderLayout.NORTH)
        panelFilters.add(comboBox)

        comboBox.addActionListener(this)

        initComboBox()
    }

    private fun initComboBox()
    {
        val achievements = Vector(getAllAchievements())
        comboBox.model = DefaultComboBoxModel(achievements)
    }

    override fun getTabName() = "Achievement Breakdown"

    override fun buildTable()
    {
        val achievement = comboBox.getItemAt(comboBox.selectedIndex)
        val achievementRows = AchievementEntity().retrieveEntities("AchievementRef = ${achievement.achievementRef}")

        val model = TableUtil.DefaultModel()
        model.addColumn("")
        model.addColumn("Player")
        model.addColumn("Score")

        val players = PlayerEntity.retrievePlayers("", false)
        players.forEach{ p ->
            val myRows = achievementRows.filter { it.playerId == p.rowId }

            val myAchievement = achievement.javaClass.newInstance()
            myAchievement.initialiseFromDb(myRows, p)

            if (myAchievement.isDecreasing() && myAchievement.attainedValue == -1)
            {
                myAchievement.attainedValue = Integer.MAX_VALUE
            }

            val row = arrayOf(p.getFlag(), p, myAchievement)
            model.addRow(row)
        }

        table.model = model
        table.setColumnWidths("25;200")
        val achievementComparator = {a1: AbstractAchievement, a2: AbstractAchievement -> a1.attainedValue.compareTo(a2.attainedValue)}
        table.setComparator(2, achievementComparator)
        table.sortBy(2, !achievement.isDecreasing())

        val renderer = AchievementProgressBarRenderer()
        if (achievement.isDecreasing())
        {
            renderer.minimum = 0
            renderer.maximum = achievement.redThreshold - achievement.maxValue
        }
        else
        {
            renderer.minimum = achievement.redThreshold
            renderer.maximum = achievement.maxValue
        }

        table.getColumn(2).cellRenderer = renderer
    }

    class AchievementProgressBarRenderer: AbstractProgressBarRenderer()
    {
        override fun getColorForValue(value: Any?): Color
        {
            return (value as AbstractAchievement).getColor(false)
        }

        override fun getScoreForValue(value: Any?): Int
        {
            val achievement = (value as AbstractAchievement)

            if (achievement.isDecreasing())
            {
                return Math.max(achievement.redThreshold - achievement.attainedValue, 0)
            }

            return achievement.attainedValue
        }

        override fun getScoreDescForValue(value: Any?): String
        {
            val achievement = value as AbstractAchievement
            val attainedValueDesc = if (achievement.attainedValue == -1 || achievement.attainedValue == Integer.MAX_VALUE) "-" else "${achievement.attainedValue}"
            return "$attainedValueDesc/${achievement.maxValue}"
        }
    }
}