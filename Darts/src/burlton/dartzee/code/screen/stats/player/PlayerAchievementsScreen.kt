package burlton.dartzee.code.screen.stats.player

import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.achievements.getAchievementMaximum
import burlton.dartzee.code.achievements.getAllAchievements
import burlton.dartzee.code.achievements.getPlayerAchievementScore
import burlton.dartzee.code.bean.AchievementMedal
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.screen.ScreenCache
import burlton.desktopcore.code.bean.WrapLayout
import java.awt.*
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants
import javax.swing.border.EmptyBorder

class PlayerAchievementsScreen : EmbeddedScreen()
{
    var previousScrn: EmbeddedScreen = ScreenCache.getPlayerManagementScreen()
    private var player: PlayerEntity? = null
    private var progressDesc = ""

    private val panelGeneral = JPanel()
    private val panelAchievementDesc = JPanel()
    private val lblAchievementName = JLabel()
    private val lblAchievementDesc = JLabel()
    private val lblAchievementExtraDetails = JLabel()

    init
    {
        val centerPanel = JPanel()
        add(centerPanel, BorderLayout.CENTER)
        centerPanel.layout = BorderLayout()
        val sp = JScrollPane()
        centerPanel.add(sp, BorderLayout.CENTER)

        val fl = WrapLayout()
        fl.vgap = 25
        fl.hgap = 20
        fl.alignment = FlowLayout.LEFT
        panelGeneral.layout = fl

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

        panelAchievementDesc.border = EmptyBorder(5, 5, 5, 5)

        sp.setViewportView(panelGeneral)
        sp.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        sp.verticalScrollBar.unitIncrement = 16
    }

    override fun getScreenName() : String
    {
        return "Achievements - ${player?.name} - $progressDesc"
    }


    override fun initialise()
    {
        panelGeneral.removeAll()

        val playerId = player?.rowId

        val achievementRows = AchievementEntity().retrieveEntities("PlayerId = $playerId")
        for (achievement in getAllAchievements())
        {
            addAchievement(achievement, achievementRows)
        }

        val score = getPlayerAchievementScore(achievementRows, player!!)
        progressDesc = "$score/${getAchievementMaximum()}"
    }

    private fun addAchievement(aa: AbstractAchievement, achievementRows: MutableList<AchievementEntity>)
    {
        val ref = aa.achievementRef
        val achievementRowsFiltered = achievementRows.filter { a -> a.achievementRef == ref }.toMutableList()

        aa.initialiseFromDb(achievementRowsFiltered)

        val medal = AchievementMedal(aa)
        panelGeneral.add(medal)
    }

    fun toggleAchievementDesc(hovered: Boolean, achievement : AbstractAchievement)
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


    override fun getBackTarget(): EmbeddedScreen
    {
        return previousScrn
    }

    fun setPlayer(player: PlayerEntity)
    {
        this.player = player
    }

    override fun getDesiredSize(): Dimension?
    {
        return Dimension(840, 600)
    }
}
