package dartzee.screen.stats.overall

import dartzee.bean.PlayerTypeFilterPanel
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JPanel

abstract class AbstractLeaderboard : JPanel(), ActionListener {
    val panelPlayerFilters = PlayerTypeFilterPanel()

    private var builtTable = false

    abstract fun buildTable()

    abstract fun getTabName(): String

    override fun actionPerformed(e: ActionEvent?) = buildTable()

    fun buildTableFirstTime() {
        if (!builtTable) {
            buildTable()
            builtTable = true
        }
    }
}
