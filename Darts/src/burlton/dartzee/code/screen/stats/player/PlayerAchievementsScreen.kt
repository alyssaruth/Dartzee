package burlton.dartzee.code.screen.stats.player

import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.achievements.getAllAchievements
import burlton.dartzee.code.bean.AchievementMedal
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.screen.ScreenCache
import burlton.desktopcore.code.bean.WrapLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants

class PlayerAchievementsScreen : EmbeddedScreen()
{
    private var player: PlayerEntity? = null

    private val panelGeneral = JPanel()
    private val panelAchievementDesc = JPanel()
    private val lblAchievementName = JLabel()

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
        lblAchievementName.font = Font("Trebuchet MS", Font.PLAIN, 20)


        sp.setViewportView(panelGeneral)
        sp.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
    }

    override fun getScreenName() : String
    {
        return "Achievements - " + player?.name
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
    }

    private fun addAchievement(aa: AbstractAchievement, achievementRows: MutableList<AchievementEntity>)
    {
        val ref = aa.achievementRef
        var achievementRowsFiltered = achievementRows.filter { a -> a.achievementRef == ref }.toMutableList()

        aa.initialiseFromDb(achievementRowsFiltered)

        val medal = AchievementMedal(aa)
        panelGeneral.add(medal)
    }

    fun toggleAchievementDesc(hovered: Boolean, achievement : AbstractAchievement)
    {
        if (hovered)
        {
            lblAchievementName.text = achievement.name
        }
        else
        {
            lblAchievementName.text = ""
        }
    }


    override fun getBackTarget(): EmbeddedScreen
    {
        return ScreenCache.getPlayerManagementScreen()
    }

    fun setPlayer(player: PlayerEntity)
    {
        this.player = player
    }
}
