package dartzee.screen.stats.player

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.AchievementType
import dartzee.achievements.MAX_ACHIEVEMENT_SCORE
import dartzee.achievements.getAchievementMaximum
import dartzee.achievements.getAchievementsForGameType
import dartzee.achievements.getPlayerAchievementScore
import dartzee.bean.AchievementMedal
import dartzee.core.bean.WrapLayout
import dartzee.core.util.getAllChildComponentsForType
import dartzee.core.util.setMargins
import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.screen.EmbeddedScreen
import dartzee.screen.ScreenCache
import dartzee.screen.player.PlayerManagementScreen
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants
import javax.swing.border.TitledBorder

class PlayerAchievementsScreen(val player: PlayerEntity) : EmbeddedScreen()
{
    var previousScrn: EmbeddedScreen = ScreenCache.get<PlayerManagementScreen>()

    private var progressDesc = ""

    private val scrollPane = JScrollPane()
    private val centerPanel = JPanel()
    private val achievementsPanel = JPanel()
    private val panelAchievementDesc = JPanel()
    val lblAchievementName = JLabel()
    val lblAchievementDesc = JLabel()
    val lblAchievementExtraDetails = JLabel()

    init
    {
        add(centerPanel, BorderLayout.CENTER)
        centerPanel.layout = BorderLayout(0, 0)
        centerPanel.add(scrollPane, BorderLayout.CENTER)

        scrollPane.setViewportView(achievementsPanel)
        scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        scrollPane.verticalScrollBar.unitIncrement = 16
        achievementsPanel.layout = MigLayout("al center center", "[grow]", "[][][][]")

        centerPanel.add(panelAchievementDesc, BorderLayout.SOUTH)
        panelAchievementDesc.preferredSize = Dimension(200, 100)
        panelAchievementDesc.layout = BorderLayout()

        panelAchievementDesc.add(lblAchievementName, BorderLayout.NORTH)
        lblAchievementName.horizontalAlignment = JLabel.CENTER
        lblAchievementName.font = Font("Trebuchet MS", Font.BOLD, 24)
        lblAchievementDesc.horizontalAlignment = JLabel.CENTER
        lblAchievementDesc.font = Font("Trebuchet MS", Font.PLAIN, 20)
        lblAchievementExtraDetails.horizontalAlignment = JLabel.CENTER
        lblAchievementExtraDetails.font = Font("Trebuchet MS", Font.ITALIC, 18)

        panelAchievementDesc.add(lblAchievementDesc, BorderLayout.CENTER)
        panelAchievementDesc.add(lblAchievementExtraDetails, BorderLayout.SOUTH)

        panelAchievementDesc.setMargins(5)
    }

    override fun getScreenName() = "Achievements - ${player.name} - $progressDesc"

    override fun initialise()
    {
        val achievementRows = AchievementEntity.retrieveAchievements(player.rowId)
        GameType.values().forEachIndexed { ix, it ->
            addAchievementTab(it, ix, achievementRows)
        }

        val score = getPlayerAchievementScore(achievementRows, player)
        progressDesc = "$score/${getAchievementMaximum()}"
    }

    private fun addAchievementTab(gameType: GameType, index: Int, achievementRows: List<AchievementEntity>)
    {
        val achievementTypes = getAchievementsForGameType(gameType).map { it.achievementType }
        val max = achievementTypes.size * MAX_ACHIEVEMENT_SCORE
        val filteredRows = achievementRows.filter { achievementTypes.contains(it.achievementType) }
        val score = getPlayerAchievementScore(filteredRows, player)

        val title = "${gameType.getDescription()} - $score / $max"

        val panel = JPanel()
        panel.border = TitledBorder(title)

        val fl = WrapLayout()
        fl.vgap = 10
        fl.hgap = 15
        fl.alignment = FlowLayout.LEFT
        panel.layout = fl

        achievementsPanel.add(panel, "cell 0 $index, growx")

        getAchievementsForGameType(gameType).forEach {
            addAchievement(it, filteredRows, panel)
        }
    }

    private fun addAchievement(aa: AbstractAchievement, achievementRows: List<AchievementEntity>, panel: JPanel)
    {
        val type = aa.achievementType
        val achievementRowsFiltered = achievementRows.filter { a -> a.achievementType == type }

        aa.initialiseFromDb(achievementRowsFiltered, player)

        val medal = AchievementMedal(aa)
        panel.add(medal)
    }

    fun scrollIntoView(achievementType: AchievementType)
    {
        val medal = getAllChildComponentsForType<AchievementMedal>().first { it.achievement.achievementType == achievementType }
        val bounds = medal.parent.bounds
        achievementsPanel.scrollRectToVisible(bounds)

    }

    fun toggleAchievementDesc(hovered: Boolean, achievement: AbstractAchievement)
    {
        if (hovered)
        {
            val color = achievement.getColor(false)
            setPanelColors(color, color.darker().darker())

            lblAchievementName.text = achievement.name

            if (!achievement.isLocked())
            {
                lblAchievementDesc.text = achievement.desc
                lblAchievementExtraDetails.text = achievement.getExtraDetails()
            }
        }
        else
        {
            setPanelColors(null, null)
            lblAchievementName.text = ""
            lblAchievementDesc.text = ""
            lblAchievementExtraDetails.text = ""
        }
    }

    private fun setPanelColors(bgColor: Color?, fgColor: Color?)
    {
        panelNavigation.background = bgColor
        panelAchievementDesc.background = bgColor
        panelNext.background = bgColor
        panelBack.background = bgColor

        lblAchievementName.background = bgColor
        lblAchievementDesc.background = bgColor
        lblAchievementExtraDetails.background = bgColor

        lblAchievementName.foreground = fgColor
        lblAchievementDesc.foreground = fgColor
        lblAchievementExtraDetails.foreground = fgColor
    }


    override fun getBackTarget() = previousScrn
    override fun getDesiredSize() = Dimension(1240, 700)
}
