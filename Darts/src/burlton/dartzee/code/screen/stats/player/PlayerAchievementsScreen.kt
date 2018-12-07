package burlton.dartzee.code.screen.stats.player

import burlton.dartzee.code.achievements.*
import burlton.dartzee.code.bean.AchievementMedal
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.EmbeddedScreen
import burlton.dartzee.code.screen.ScreenCache
import burlton.desktopcore.code.bean.WrapLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.border.BevelBorder

class PlayerAchievementsScreen : EmbeddedScreen()
{
    private var player: PlayerEntity? = null

    private val panelGeneral = JPanel()
    private val panelAchievementDesc = JPanel()

    init
    {
        val centerPanel = JPanel()
        add(centerPanel, BorderLayout.CENTER)
        centerPanel.layout = BorderLayout()
        val tabbedPane = JTabbedPane(SwingConstants.TOP)
        centerPanel.add(tabbedPane, BorderLayout.CENTER)

        val fl = WrapLayout()
        fl.vgap = 25
        fl.hgap = 20
        fl.alignment = FlowLayout.LEFT
        panelGeneral.layout = fl


        centerPanel.add(panelAchievementDesc, BorderLayout.SOUTH)
        panelAchievementDesc.preferredSize = Dimension(200, 100)
        panelAchievementDesc.border = BevelBorder(5)

        val sp = JScrollPane()
        sp.setViewportView(panelGeneral)
        sp.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        tabbedPane.addTab("X01", null, sp, null)
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

        addAchievement(AchievementX01BestFinish(), achievementRows)
        addAchievement(AchievementX01BestThreeDarts(), achievementRows)
        addAchievement(AchievementX01CheckoutCompleteness(), achievementRows)
        addAchievement(AchievementX01HighestBust(), achievementRows)
    }

    private fun addAchievement(aa: AbstractAchievement, achievementRows: MutableList<AchievementEntity>)
    {
        val ref = aa.achievementRef
        var achievementRowsFiltered = achievementRows.filter { a -> a.achievementRef == ref }.toMutableList()

        aa.initialiseFromDb(achievementRowsFiltered)

        val medal = AchievementMedal(aa)
        panelGeneral.add(medal)
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
