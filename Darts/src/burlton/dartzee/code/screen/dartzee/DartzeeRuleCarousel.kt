package burlton.dartzee.code.screen.dartzee

import burlton.core.code.util.ceilDiv
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.DartzeeRoundResult
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.db.DartzeeRoundResultEntity
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*
import javax.swing.border.EmptyBorder

class DartzeeRuleCarousel(val parent: IDartzeeCarouselListener, val dtos: List<DartzeeRuleDto>): JPanel(), ActionListener, MouseListener
{
    private val tilePanel = JPanel()
    private val tileScroller = JScrollPane()
    private val toggleButtonPanel = JPanel()
    private val toggleButtonPending = JToggleButton()
    private val toggleButtonComplete = JToggleButton()

    private val dartsThrown = mutableListOf<Dart>()
    private val pendingTiles = mutableListOf<DartzeeRuleTile>()
    private val completeTiles = mutableListOf<DartzeeRuleTile>()

    private var hoveredTile: DartzeeRuleTile? = null

    init
    {
        layout = BorderLayout(0, 0)
        add(tileScroller, BorderLayout.CENTER)
        add(toggleButtonPanel, BorderLayout.EAST)

        tileScroller.setViewportView(tilePanel)
        tileScroller.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        tileScroller.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER

        val bg = ButtonGroup()
        bg.add(toggleButtonPending)
        bg.add(toggleButtonComplete)

        toggleButtonPending.isSelected = true
        toggleButtonPending.toolTipText = "Show available rules"
        toggleButtonComplete.toolTipText = "Show completed rules"

        toggleButtonPending.addActionListener(this)
        toggleButtonComplete.addActionListener(this)

        toggleButtonPanel.border = EmptyBorder(5, 5, 5, 5)
        toggleButtonPending.preferredSize = Dimension(50, 50)
        toggleButtonComplete.preferredSize = Dimension(50, 50)

        toggleButtonPanel.layout = BorderLayout()
        toggleButtonPanel.add(toggleButtonPending, BorderLayout.NORTH)
        toggleButtonPanel.add(toggleButtonComplete, BorderLayout.SOUTH)
    }

    fun update(results: List<DartzeeRoundResultEntity>, darts: List<Dart>, currentScore: Int)
    {
        hoveredTile = null
        dartsThrown.clear()
        dartsThrown.addAll(darts)

        initialiseTiles(results, currentScore)
    }
    private fun initialiseTiles(results: List<DartzeeRoundResultEntity>, currentScore: Int)
    {
        completeTiles.clear()
        pendingTiles.clear()

        results.sortedBy { it.roundNumber }.forEach { result ->
            val dto = dtos[result.ruleNumber - 1]
            val completeRule = DartzeeRuleTileComplete(dto, getRuleNumber(dto), result.success)
            completeTiles.add(completeRule)
        }
        toggleButtonComplete.isEnabled = completeTiles.isNotEmpty()

        val incompleteRules = dtos.filterIndexed { ix, _ -> results.none { it.ruleNumber == ix + 1 }}
        pendingTiles.addAll(incompleteRules.map { rule -> DartzeeRuleTile(rule, getRuleNumber(rule)) })
        pendingTiles.forEach {
            it.addActionListener(this)
            it.addMouseListener(this)
            it.updateState(dartsThrown)
        }

        if (dartsThrown.size == 3)
        {
            val successfulRules = pendingTiles.filter { it.isVisible }
            successfulRules.forEach { it.setPendingResult(true, it.dto.getSuccessTotal(dartsThrown)) }
        }

        if (pendingTiles.none { it.isVisible })
        {
            val ruleToFail = getFirstIncompleteRule()
            if (ruleToFail != null)
            {
                val score = currentScore.ceilDiv(2) - currentScore
                ruleToFail.isVisible = true
                ruleToFail.setPendingResult(false, score)
            }
        }

        if (toggleButtonComplete.isSelected)
        {
            displayTiles(completeTiles)
        }
        else
        {
            displayTiles(pendingTiles)
        }
    }

    private fun getRuleNumber(dto: DartzeeRuleDto) = dtos.indexOf(dto) + 1

    private fun getFirstIncompleteRule(): DartzeeRuleTile? = pendingTiles.firstOrNull()

    fun getValidSegments(): List<DartboardSegment>
    {
        val tile = hoveredTile
        if (tile != null)
        {
            return tile.getValidSegments(dartsThrown)
        }

        val validSegments = HashSet<DartboardSegment>()
        pendingTiles.forEach {
            validSegments.addAll(it.getValidSegments(dartsThrown))
        }

        return validSegments.toList()
    }

    private fun displayTiles(tiles: List<DartzeeRuleTile>)
    {
        tilePanel.removeAll()
        tiles.forEach { tilePanel.add(it) }
        tilePanel.validate()
        tilePanel.repaint()
        tileScroller.validate()
        tileScroller.repaint()
    }

    fun gameFinished()
    {
        toggleButtonComplete.isSelected = true
        displayTiles(completeTiles)
        toggleButtonPanel.isVisible = false
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        val src = e?.source
        when (src)
        {
            toggleButtonPending -> displayTiles(pendingTiles)
            toggleButtonComplete -> displayTiles(completeTiles)
            is DartzeeRuleTile -> tilePressed(src)
        }
    }

    private fun tilePressed(tile: DartzeeRuleTile)
    {
        if (tile.pendingResult != null) {
            val result = DartzeeRoundResult(tile.ruleNumber, tile.pendingResult!!, tile.pendingScore!!)
            parent.tilePressed(result)
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