package dartzee.screen.stats.overall

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.DummyAchievementTotal
import dartzee.achievements.getAllAchievements
import dartzee.bean.ScrollTableAchievements
import dartzee.core.util.TableUtil
import dartzee.core.util.sortedBy
import dartzee.db.PlayerEntity
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.ActionEvent
import java.util.*
import javax.swing.Box
import javax.swing.DefaultComboBoxModel
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JPanel

const val ACHIEVEMENT_COLUMN_IX = 3

class LeaderboardAchievements: AbstractLeaderboard()
{
    val table = ScrollTableAchievements()
    private val panelFilters = JPanel()

    val cbSpecificAchievement = JCheckBox("Achievement")
    val comboBox = JComboBox<AbstractAchievement>()

    init
    {
        layout = BorderLayout(0, 0)
        table.setRowHeight(23)
        add(table)
        add(panelFilters, BorderLayout.NORTH)
        panelFilters.add(cbSpecificAchievement)
        panelFilters.add(comboBox)
        panelFilters.add(Box.createHorizontalStrut(20))
        panelFilters.add(panelPlayerFilters)
        comboBox.isEnabled = false

        initComboBox()

        comboBox.addActionListener(this)
        panelPlayerFilters.addActionListener(this)
        cbSpecificAchievement.addActionListener(this)
    }

    private fun initComboBox()
    {
        val achievements = Vector(getAllAchievements())
        comboBox.model = DefaultComboBoxModel(achievements)
    }

    override fun getTabName() = "Achievements"

    override fun buildTable()
    {
        val achievement = getSelectedAchievement()
        val achievementRows = achievement.retrieveAllRows()

        val model = TableUtil.DefaultModel()
        model.addColumn("#")
        model.addColumn("")
        model.addColumn("Player")
        model.addColumn("Score")

        val players = PlayerEntity.retrievePlayers(panelPlayerFilters.getWhereSql())
        val entries = players.map { p ->
            val myRows = achievementRows.filter { it.playerId == p.rowId }

            val myAchievement = achievement.javaClass.getDeclaredConstructor().newInstance()
            myAchievement.initialiseFromDb(myRows, p)

            if (myAchievement.isDecreasing() && myAchievement.attainedValue == -1)
            {
                myAchievement.attainedValue = Integer.MAX_VALUE
            }

            val row = listOf(p.getFlag(), p, myAchievement)
            LeaderboardEntry(myAchievement.attainedValue, row)
        }.sortedBy(!achievement.isDecreasing()) { it.score }

        val modelRows = getRankedRowsForTable(entries)
        model.addRows(modelRows)

        table.model = model
        table.setColumnWidths("35;25;200")
        table.sortBy(0, false)

        table.setComparator(ACHIEVEMENT_COLUMN_IX, compareBy<AbstractAchievement>{ it.attainedValue })
        val renderer = AchievementProgressBarRenderer()
        renderer.minimum = 0
        if (achievement.isDecreasing())
        {
            renderer.maximum = achievement.redThreshold - achievement.maxValue
        }
        else
        {
            renderer.maximum = achievement.maxValue
        }

        table.getColumn(ACHIEVEMENT_COLUMN_IX).cellRenderer = renderer
    }

    private fun getSelectedAchievement(): AbstractAchievement = if (cbSpecificAchievement.isSelected) comboBox.getItemAt(comboBox.selectedIndex) else DummyAchievementTotal()

    override fun actionPerformed(e: ActionEvent?)
    {
        super.actionPerformed(e)

        comboBox.isEnabled = cbSpecificAchievement.isSelected
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