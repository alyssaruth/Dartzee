package burlton.dartzee.code.screen.stats.player

import burlton.dartzee.code.bean.PlayerAvatar
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.stats.GameWrapper
import net.miginfocom.swing.MigLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.border.EtchedBorder

/**
 * Info panel to appear above the statistics detailing the player and the filters that are in use.
 */
class PlayerStatisticsFilterPanel : JPanel(), ActionListener
{
    private val lblAvatar = PlayerAvatar()
    private val lblFilterDesc = JLabel("No filters")
    private val btnFilters = JButton("Filters...")
    private val panel = JPanel()
    private val lblXGames = JLabel("X Games")
    private val label = JLabel("<Player Name>")
    private val verticalStrut = Box.createVerticalStrut(20)
    private val lblDateFilter = JLabel("")
    private val btnX = JButton("X")
    private val panelX = JPanel()

    private var dlg: PlayerStatisticsFilterDialog = PlayerStatisticsFilterDialog(GAME_TYPE_X01)

    init
    {
        val flowLayout = layout as FlowLayout
        flowLayout.hgap = 0
        add(lblAvatar)
        panel.preferredSize = Dimension(150, 150)
        panel.border = EtchedBorder(EtchedBorder.RAISED, null, null)
        add(panel)
        panel.layout = MigLayout("", "[grow]", "[][][][][][]")
        panel.add(verticalStrut, "cell 0 1,alignx center")
        panel.add(btnFilters, "cell 0 2,alignx center,aligny top")
        lblFilterDesc.horizontalAlignment = SwingConstants.CENTER
        panel.add(lblFilterDesc, "cell 0 3,growx,aligny top")
        label.font = Font("Tahoma", Font.PLAIN, 16)
        panel.add(label, "flowy,cell 0 0,alignx center")
        lblDateFilter.horizontalAlignment = SwingConstants.CENTER
        panel.add(lblDateFilter, "cell 0 4")
        lblXGames.font = Font("Tahoma", Font.BOLD, 11)
        lblXGames.horizontalAlignment = SwingConstants.CENTER
        panel.add(lblXGames, "cell 0 5,growx,aligny top")
        lblAvatar.readOnly = true
        panelX.preferredSize = Dimension(45, 150)
        add(panelX)
        panelX.add(btnX)
        btnX.preferredSize = Dimension(40, 40)

        btnFilters.addActionListener(this)
        btnX.addActionListener(this)
    }

    fun init(player: PlayerEntity, gameType: Int, comparison: Boolean)
    {
        dlg = PlayerStatisticsFilterDialog(gameType)

        lblAvatar.init(player, false)
        label.text = player.name

        panelX.isVisible = comparison
        if (comparison)
        {
            label.foreground = Color.RED
            lblFilterDesc.foreground = Color.RED
            lblDateFilter.foreground = Color.RED
        }

        //Reset the filters
        dlg.resetFilters()
    }

    fun update(filteredGames: List<GameWrapper>)
    {
        lblFilterDesc.text = dlg.getFiltersDesc()
        lblDateFilter.text = dlg.getDateDesc()
        lblXGames.text = "${filteredGames.size} Games"
    }

    fun includeGame(game: GameWrapper) = dlg.includeGameBasedOnFilters(game)

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnFilters -> {
                dlg.refresh()
                dlg.setLocationRelativeTo(this)
                dlg.isVisible = true
            }
            btnX -> ScreenCache.getScreen(PlayerStatisticsScreen::class.java).removeComparison()
        }
    }
}
