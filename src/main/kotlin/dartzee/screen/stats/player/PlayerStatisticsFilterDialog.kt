package dartzee.screen.stats.player

import dartzee.bean.GameParamFilterPanel
import dartzee.core.bean.DateFilterPanel
import dartzee.core.screen.SimpleDialog
import dartzee.core.util.enableChildren
import dartzee.game.GameType
import dartzee.screen.ScreenCache
import dartzee.stats.GameWrapper
import dartzee.utils.getFilterPanel
import java.awt.BorderLayout
import java.awt.Component
import java.awt.GridLayout
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class PlayerStatisticsFilterDialog(gameType: GameType) : SimpleDialog(), ChangeListener {
    private var gameParams = ""
    private var filterByDate = false

    private val filterPanel: GameParamFilterPanel = getFilterPanel(gameType)

    private val cbType = JCheckBox("Type")
    private val panelGameType = JPanel()
    private val panelDate = JPanel()
    private val chckbxDatePlayed = JCheckBox("Date")
    private val dateFilter = DateFilterPanel()

    init {
        title = "Filters"
        setSize(473, 200)
        isModal = true

        val panelFilters = JPanel()
        contentPane.add(panelFilters, BorderLayout.CENTER)
        panelFilters.layout = GridLayout(0, 1, 0, 0)

        panelFilters.add(panelGameType)
        panelGameType.add(cbType)
        panelGameType.add(filterPanel)

        panelFilters.add(panelDate)

        panelDate.add(chckbxDatePlayed)

        panelDate.add(dateFilter)

        cbType.addChangeListener(this)
        chckbxDatePlayed.addChangeListener(this)
    }

    fun getFiltersDesc(): String {
        var desc = "Showing "
        if (gameParams.isEmpty()) {
            desc += "all games"
        } else {
            desc += filterPanel.getFilterDesc()
        }

        return desc
    }

    fun getDateDesc() = if (filterByDate) dateFilter.getFilterDesc() else ""

    fun resetFilters() {
        gameParams = ""
        filterByDate = false
    }

    private fun saveState() {
        if (cbType.isSelected) {
            gameParams = filterPanel.getGameParams()
        } else {
            gameParams = ""
        }

        filterByDate = chckbxDatePlayed.isSelected
    }

    fun refresh() {
        cbType.isSelected = !gameParams.isEmpty()
        filterPanel.isEnabled = cbType.isSelected

        if (!gameParams.isEmpty()) {
            filterPanel.setGameParams(gameParams)
        }

        chckbxDatePlayed.isSelected = filterByDate
        dateFilter.enableChildren(chckbxDatePlayed.isSelected)
    }

    private fun valid() = dateFilter.valid()

    fun includeGameBasedOnFilters(game: GameWrapper): Boolean {
        val gameParamsToCheck = game.gameParams
        if (gameParams.isNotEmpty() && gameParamsToCheck != gameParams) {
            return false
        }

        // Date filter
        if (filterByDate) {
            val dtStart = game.dtStart
            if (!dateFilter.filterSqlDate(dtStart)) {
                return false
            }
        }

        return true
    }

    override fun okPressed() {
        if (valid()) {
            saveState()
            dispose()

            val scrn = ScreenCache.get<PlayerStatisticsScreen>()
            scrn.buildTabs()
        }
    }

    override fun stateChanged(arg0: ChangeEvent) {
        val src = arg0.source as Component
        if (src === cbType) {
            filterPanel.isEnabled = cbType.isSelected
        } else if (src === chckbxDatePlayed) {
            dateFilter.enableChildren(chckbxDatePlayed.isSelected)
        }
    }
}
