package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.db.DartzeeRoundResult
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants

class DartzeeRuleCarousel(dtos: List<DartzeeRuleDto>): JPanel()
{
    private val scrollPane = JScrollPane()
    private val tiles = dtos.mapIndexed { ix, rule -> DartzeeRuleTile(rule, ix + 1) }

    init
    {
        layout = BorderLayout(0, 0)
        add(scrollPane, BorderLayout.CENTER)

        preferredSize = Dimension(150, 120)

        val tilePanel = JPanel()
        scrollPane.setViewportView(tilePanel)
        scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER

        tiles.forEach { tilePanel.add(it) }
    }

    fun update(results: List<DartzeeRoundResult>)
    {
        results.forEach {
            val tile = tiles[it.ruleNumber - 1]
            tile.setResult(it.success)
        }
    }

    fun getValidSegments(): List<DartboardSegment>
    {
        val validSegments = HashSet<DartboardSegment>()
        tiles.forEach {
            validSegments.addAll(it.getValidSegments())
        }

        return validSegments.toList()
    }
}