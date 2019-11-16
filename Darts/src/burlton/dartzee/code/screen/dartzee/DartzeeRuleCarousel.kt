package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.db.DartzeeRoundResultEntity
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants

class DartzeeRuleCarousel(dtos: List<DartzeeRuleDto>): JPanel(), ActionListener
{
    private val scrollPane = JScrollPane()
    private val tiles = dtos.mapIndexed { ix, rule -> DartzeeRuleTile(rule, ix + 1) }

    private var tileListener: IDartzeeTileListener? = null

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
        tiles.forEach { it.addActionListener(this) }
    }

    fun update(results: List<DartzeeRoundResultEntity>, darts: List<Dart>)
    {
        tiles.forEach { it.clearPendingResult() }

        results.forEach {
            val tile = tiles[it.ruleNumber - 1]
            tile.setResult(it.success)
        }

        tiles.forEach {
            it.updateState(darts)
        }

        if (darts.size == 3)
        {
            val successfulRules = tiles.filter { it.isVisible }
            if (successfulRules.size == 1)
            {
                successfulRules.first().setPendingResult(true)
            }
        }

        if (tiles.none { it.isVisible })
        {
            val ruleToFail = getFirstIncompleteRule()
            if (ruleToFail != null)
            {
                ruleToFail.isVisible = true
                ruleToFail.setPendingResult(false)
            }
            else
            {
                tiles.forEach { it.isVisible = true }
            }
        }
    }
    private fun getFirstIncompleteRule(): DartzeeRuleTile? = tiles.firstOrNull { it.result == null }

    data class RoundResult(val ruleNumber: Int, val success: Boolean, val userInputNeeded: Boolean = false)
    fun getRoundResult(): RoundResult
    {
        val tiles = tiles.filter { it.isVisible }
        if (tiles.size > 1)
        {
            return RoundResult(-1, false, true)
        }

        val rule = tiles.first()
        return RoundResult(rule.ruleNumber, rule.pendingResult!!)
    }

    fun getValidSegments(dartsThrown: List<Dart>): List<DartboardSegment>
    {
        val validSegments = HashSet<DartboardSegment>()
        tiles.forEach {
            validSegments.addAll(it.getValidSegments(dartsThrown))
        }

        return validSegments.toList()
    }

    fun addTileListener(listener: IDartzeeTileListener)
    {
        this.tileListener = listener
    }
    fun clearTileListener()
    {
        this.tileListener = null
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        val src = e?.source
        val listener = tileListener ?: return
        if (src is DartzeeRuleTile)
        {
            listener.tilePressed(src.ruleNumber, true)
        }
    }
}