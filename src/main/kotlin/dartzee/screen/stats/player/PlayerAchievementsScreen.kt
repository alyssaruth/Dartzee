package dartzee.screen.stats.player

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.MAX_ACHIEVEMENT_SCORE
import dartzee.achievements.getAchievementMaximum
import dartzee.achievements.getAchievementsForGameType
import dartzee.achievements.getPlayerAchievementScore
import dartzee.bean.AchievementMedal
import dartzee.core.bean.WrapLayout
import dartzee.core.util.setMargins
import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.screen.EmbeddedScreen
import dartzee.screen.ScreenCache
import dartzee.screen.player.PlayerManagementScreen
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants
import javax.swing.border.TitledBorder

class PlayerAchievementsScreen(val player: PlayerEntity) : EmbeddedScreen()
{
    var previousScrn: EmbeddedScreen = ScreenCache.get<PlayerManagementScreen>()

    private var progressDesc = ""

    private val gridBag = GridBagLayout()
    private val scrollPane = JScrollPane()
    private val centerPanel = JPanel()
    private val achievementsPanel = JPanel()
    private val panelAchievementDesc = JPanel()
    private val panelAchievementDescNorth = JPanel()
    private val lblAchievementName = JLabel()
    private val lblAchievementDesc = JLabel()
    private val lblAchievementExtraDetails = JLabel()
    private val lblIndividualIndicator = JLabel()
    private val lblTeamIndicator = JLabel()

    init
    {
        add(centerPanel, BorderLayout.CENTER)
        centerPanel.layout = BorderLayout(0, 0)
        centerPanel.add(scrollPane, BorderLayout.CENTER)

        scrollPane.setViewportView(achievementsPanel)
        scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        scrollPane.verticalScrollBar.unitIncrement = 16
        achievementsPanel.layout = gridBag

        centerPanel.add(panelAchievementDesc, BorderLayout.SOUTH)
        panelAchievementDesc.preferredSize = Dimension(200, 100)
        panelAchievementDesc.layout = BorderLayout()

        panelAchievementDesc.add(panelAchievementDescNorth, BorderLayout.NORTH)
        lblIndividualIndicator.preferredSize = Dimension(48, 48)
        lblIndividualIndicator.name = "individualIndicator"
        lblTeamIndicator.preferredSize = Dimension(54, 48)
        lblTeamIndicator.name = "teamIndicator"

        lblAchievementDesc.horizontalAlignment = JLabel.CENTER
        lblAchievementDesc.font = Font("Trebuchet MS", Font.PLAIN, 20)
        lblAchievementDesc.name = "description"
        lblAchievementExtraDetails.horizontalAlignment = JLabel.CENTER
        lblAchievementExtraDetails.font = Font("Trebuchet MS", Font.ITALIC, 18)
        lblAchievementExtraDetails.name = "extraDetails"

        lblAchievementName.horizontalAlignment = JLabel.CENTER
        lblAchievementName.font = Font("Trebuchet MS", Font.BOLD, 24)
        lblAchievementName.name = "name"

        panelAchievementDescNorth.add(lblIndividualIndicator)
        panelAchievementDescNorth.add(lblAchievementName)
        panelAchievementDescNorth.add(lblTeamIndicator)

        panelAchievementDesc.add(lblAchievementDesc, BorderLayout.CENTER)
        panelAchievementDesc.setMargins(5)
    }

    override fun getScreenName() = "Achievements - ${player.name} - $progressDesc"

    override fun initialise()
    {
        val achievementRows = AchievementEntity.retrieveAchievements(player.rowId)
        GameType.values().forEachIndexed { ix, gameType ->
            addAchievementTab(gameType, ix, achievementRows)
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
        fl.vgap = 15
        fl.hgap = 15
        fl.alignment = FlowLayout.LEFT
        panel.layout = fl

        val col = index % 2
        val row = index / 2

        val constraints = GridBagConstraints()
        constraints.fill = GridBagConstraints.BOTH
        constraints.gridx = row
        constraints.gridy = col
        constraints.gridwidth = GridBagConstraints.RELATIVE
        constraints.weightx = 1.0
        gridBag.setConstraints(panel, constraints)
        achievementsPanel.add(panel)

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

    fun toggleAchievementDesc(hovered: Boolean, achievement: AbstractAchievement)
    {
        if (hovered)
        {
            val color = achievement.getColor(false)
            setPanelColors(color, color.darker().darker())

            lblAchievementName.text = achievement.name
            lblAchievementExtraDetails.setMargins(0, -btnBack.width, 0, 0)
            panelNavigation.add(lblAchievementExtraDetails)

            val singlePlayerIcon = if (achievement.allowedForIndividuals) "singlePlayerEnabled" else "singlePlayerDisabled"
            lblIndividualIndicator.isVisible = true
            lblIndividualIndicator.icon = ImageIcon(javaClass.getResource("/achievements/$singlePlayerIcon.png"))

            val teamIcon = if (achievement.allowedForTeams) "multiPlayerEnabled" else "multiPlayerDisabled"
            lblTeamIndicator.isVisible = true
            lblTeamIndicator.icon = ImageIcon(javaClass.getResource("/achievements/$teamIcon.png"))

            if (!achievement.isLocked())
            {
                lblAchievementDesc.text = achievement.desc
                lblAchievementExtraDetails.text = achievement.getExtraDetails()
            }
        }
        else
        {
            setPanelColors(null, null)
            lblIndividualIndicator.isVisible = false
            lblTeamIndicator.isVisible = false
            lblAchievementName.text = ""
            lblAchievementDesc.text = ""
            lblAchievementExtraDetails.text = ""
            panelNavigation.remove(lblAchievementExtraDetails)
        }
    }

    private fun setPanelColors(bgColor: Color?, fgColor: Color?)
    {
        panelNavigation.background = bgColor
        panelAchievementDesc.background = bgColor
        panelNext.background = bgColor
        panelBack.background = bgColor
        panelAchievementDescNorth.background = bgColor

        lblAchievementName.background = bgColor
        lblAchievementDesc.background = bgColor
        lblAchievementExtraDetails.background = bgColor

        lblAchievementName.foreground = fgColor
        lblAchievementDesc.foreground = fgColor
        lblAchievementExtraDetails.foreground = fgColor
    }

    override fun getBackTarget() = previousScrn
}
