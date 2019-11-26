package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.DartzeeRoundResult
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.db.DartzeeRoundResultEntity
import burlton.dartzee.code.utils.factoryHighScoreResult
import burlton.dartzee.code.utils.getAllPossibleSegments
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*
import javax.swing.border.EmptyBorder

class DartzeeRuleCarousel(val parent: IDartzeeCarouselHoverListener, val dtos: List<DartzeeRuleDto>): JPanel(), ActionListener, MouseListener
{
    private val tilePanel = JPanel()
    private val tileScroller = JScrollPane()
    private val highScoreTile = DartzeeRuleTileHighScore()
    private val toggleButtonPanel = JPanel()
    private val toggleButtonPending = JToggleButton()
    private val toggleButtonComplete = JToggleButton()

    private val dartsThrown = mutableListOf<Dart>()
    private val pendingTiles = mutableListOf<DartzeeRuleTile>()
    private val completeTiles = mutableListOf<DartzeeRuleTile>()

    private var tileListener: IDartzeeTileListener? = null
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

        preferredSize = Dimension(150, 120)
    }

    fun update(results: List<DartzeeRoundResultEntity>, darts: List<Dart>, roundNumber: Int)
    {
        hoveredTile = null
        dartsThrown.clear()
        dartsThrown.addAll(darts)

        if (roundNumber == 1)
        {
            highScoreTile.isVisible = true
            displayTiles(listOf(highScoreTile))
        }
        else
        {
            initialiseTiles(results, darts)
        }
    }
    private fun initialiseTiles(results: List<DartzeeRoundResultEntity>, darts: List<Dart>)
    {
        highScoreTile.isVisible = false

        completeTiles.clear()
        pendingTiles.clear()

        results.sortedBy { it.roundNumber }.forEach { result ->
            val dto = dtos[result.ruleNumber - 1]
            val completeRule = DartzeeRuleTileComplete(dto, getRuleNumber(dto), result.success)
            completeRule.isVisible = true
            completeTiles.add(completeRule)
        }
        toggleButtonComplete.isEnabled = completeTiles.isNotEmpty()

        val incompleteRules = dtos.filterIndexed { ix, _ -> results.none { it.ruleNumber == ix + 1 }}
        pendingTiles.addAll(incompleteRules.map { rule -> DartzeeRuleTile(rule, getRuleNumber(rule)) })
        pendingTiles.forEach {
            it.addActionListener(this)
            it.addMouseListener(this)
            it.updateState(darts)
        }

        if (darts.size == 3)
        {
            val successfulRules = pendingTiles.filter { it.isVisible }
            if (successfulRules.size == 1)
            {
                successfulRules.first().setPendingResult(true)
            }
        }

        if (pendingTiles.none { it.isVisible })
        {
            val ruleToFail = getFirstIncompleteRule()
            if (ruleToFail != null)
            {
                ruleToFail.isVisible = true
                ruleToFail.setPendingResult(false)
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

    fun getRoundResult(): DartzeeRoundResult
    {
        if (highScoreTile.isVisible)
        {
            return factoryHighScoreResult(dartsThrown)
        }

        val tiles = pendingTiles.filter { it.isVisible }
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
        pendingTiles.forEach {
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

    private fun displayTiles(tiles: List<DartzeeRuleTile>)
    {
        tilePanel.removeAll()
        tiles.forEach { tilePanel.add(it) }
        tilePanel.repaint()
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
        val listener = tileListener ?: return
        val result = DartzeeRoundResult(tile.ruleNumber, true, false, tile.dto.getSuccessTotal(dartsThrown))
        listener.tilePressed(result)
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