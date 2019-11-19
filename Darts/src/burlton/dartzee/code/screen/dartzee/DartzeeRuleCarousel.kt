package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.DartzeeRoundResult
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.db.DartzeeRoundResultEntity
import burlton.dartzee.code.utils.getAllPossibleSegments
import burlton.dartzee.code.utils.sumScore
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants

class DartzeeRuleCarousel(val parent: IDartzeeCarouselHoverListener, dtos: List<DartzeeRuleDto>): JPanel(), ActionListener, MouseListener
{
    private val scrollPane = JScrollPane()
    private val tilePanel = JPanel()
    private val tiles = dtos.mapIndexed { ix, rule -> DartzeeRuleTile(rule, ix + 1) }
    private val dartsThrown = mutableListOf<Dart>()
    private val highScoreTile = DartzeeRuleTileHighScore()

    private var tileListener: IDartzeeTileListener? = null
    private var hoveredTile: DartzeeRuleTile? = null

    init
    {
        layout = BorderLayout(0, 0)
        add(scrollPane, BorderLayout.CENTER)

        preferredSize = Dimension(150, 120)


        scrollPane.setViewportView(tilePanel)
        scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER

        tilePanel.add(highScoreTile)
        tiles.forEach { it.addActionListener(this) }
        tiles.forEach { it.addMouseListener(this) }
    }

    fun highScoreRoundComplete()
    {
        highScoreTile.isVisible = false
        tilePanel.remove(highScoreTile)
        tiles.forEach { tilePanel.add(it) }
    }

    fun update(results: List<DartzeeRoundResultEntity>, darts: List<Dart>)
    {
        dartsThrown.clear()
        dartsThrown.addAll(darts)

        tiles.forEach { it.clearPendingResult() }

        results.forEach {
            if (it.ruleNumber > 0)
            {
                val tile = tiles[it.ruleNumber - 1]
                tile.setResult(it.success)
            }
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

    fun getRoundResult(): DartzeeRoundResult
    {
        if (highScoreTile.isVisible)
        {
            return DartzeeRoundResult(0, true, false, sumScore(dartsThrown))
        }

        val tiles = tiles.filter { it.isVisible }
        if (tiles.size > 1)
        {
            return DartzeeRoundResult(-1, false, true)
        }

        val rule = tiles.first()
        val success = rule.pendingResult!!

        return DartzeeRoundResult(rule.ruleNumber, success, successScore = rule.dto.getSuccessTotal(dartsThrown))
    }

    fun getValidSegments(): List<DartboardSegment>
    {
        if (highScoreTile.isVisible)
        {
            return getAllPossibleSegments()
        }

        val tile = hoveredTile
        if (tile != null)
        {
            return tile.getValidSegments(dartsThrown)
        }

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
            val result = DartzeeRoundResult(src.ruleNumber, true, false, src.dto.getSuccessTotal(dartsThrown))
            listener.tilePressed(result)
        }
    }

    override fun mouseEntered(e: MouseEvent?)
    {
        val src = e?.source
        if (src is DartzeeRuleTile)
        {
            hoveredTile = src
        }

        parent.hoverChanged(getValidSegments())
    }

    override fun mouseExited(e: MouseEvent?)
    {
        hoveredTile = null

        parent.hoverChanged(getValidSegments())
    }

    override fun mousePressed(e: MouseEvent?) {}
    override fun mouseClicked(e: MouseEvent?) {}
    override fun mouseReleased(e: MouseEvent?) {}
}