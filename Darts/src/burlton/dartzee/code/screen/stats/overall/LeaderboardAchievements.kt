package burlton.dartzee.code.screen.stats.overall

import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.achievements.DummyAchievementTotal
import burlton.dartzee.code.achievements.getAllAchievements
import burlton.dartzee.code.bean.PlayerTypeFilterPanel
import burlton.dartzee.code.bean.ScrollTableAchievements
import burlton.dartzee.code.db.PlayerEntity
import burlton.desktopcore.code.util.TableUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.ActionEvent
import java.util.*
import javax.swing.*

class LeaderboardAchievements: AbstractLeaderboard()
{
    val table = ScrollTableAchievements()
    private val panelFilters = JPanel()

    val cbSpecificAchievement = JCheckBox("Achievement")
    val comboBox = JComboBox<AbstractAchievement>()
    val playerFilterPanel = PlayerTypeFilterPanel()

    init
    {
        layout = BorderLayout(0, 0)
        table.setRowHeight(23)
        add(table)
        add(panelFilters, BorderLayout.NORTH)
        panelFilters.add(cbSpecificAchievement)
        panelFilters.add(comboBox)
        panelFilters.add(Box.createHorizontalStrut(20))
        panelFilters.add(playerFilterPanel)
        comboBox.isEnabled = false

        initComboBox()

        comboBox.addActionListener(this)
        playerFilterPanel.addActionListener(this)
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
        model.addColumn("")
        model.addColumn("Player")
        model.addColumn("Score")

        val players = PlayerEntity.retrievePlayers(playerFilterPanel.getWhereSql(), false)
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
        table.setComparator(2, compareBy<AbstractAchievement>{ it.attainedValue })
        table.sortBy(2, !achievement.isDecreasing())

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

        table.getColumn(2).cellRenderer = renderer
    }

    private fun getSelectedAchievement() = if (cbSpecificAchievement.isSelected) comboBox.getItemAt(comboBox.selectedIndex) else DummyAchievementTotal()

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