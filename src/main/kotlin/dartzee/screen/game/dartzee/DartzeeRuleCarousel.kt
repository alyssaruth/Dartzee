package dartzee.screen.game.dartzee

import dartzee.`object`.Dart
import dartzee.ai.DartsAiModel
import dartzee.ai.DartzeePlayStyle
import dartzee.core.util.ceilDiv
import dartzee.dartzee.DartzeeRoundResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.DartzeeRoundResultEntity
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*
import javax.swing.border.EmptyBorder

class DartzeeRuleCarousel(private val dtos: List<DartzeeRuleDto>): JPanel(), ActionListener, MouseListener
{
    val tilePanel = JPanel()
    private val tileScroller = JScrollPane()
    val toggleButtonPanel = JPanel()
    val toggleButtonPending = JToggleButton()
    val toggleButtonComplete = JToggleButton()

    val dartsThrown = mutableListOf<Dart>()
    val pendingTiles = mutableListOf<DartzeeRuleTilePending>()
    val completeTiles = mutableListOf<DartzeeRuleTile>()

    @Volatile var initialised = false

    var listener: IDartzeeCarouselListener? = null

    private var hoveredTile: DartzeeRuleTilePending? = null

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
        toggleButtonPending.toolTipText = "In progress"
        toggleButtonComplete.toolTipText = "Completed"

        toggleButtonPending.addActionListener(this)
        toggleButtonComplete.addActionListener(this)

        toggleButtonPanel.border = EmptyBorder(5, 5, 5, 5)
        toggleButtonPending.preferredSize = Dimension(50, 50)
        toggleButtonPending.icon = ImageIcon(javaClass.getResource("/buttons/inProgress.png"))
        toggleButtonComplete.preferredSize = Dimension(50, 50)
        toggleButtonComplete.icon = ImageIcon(javaClass.getResource("/buttons/completed.png"))

        toggleButtonPanel.layout = BorderLayout()
        toggleButtonPanel.add(toggleButtonPending, BorderLayout.NORTH)
        toggleButtonPanel.add(toggleButtonComplete, BorderLayout.SOUTH)
    }

    fun update(results: List<DartzeeRoundResultEntity>, darts: List<Dart>, currentScore: Int)
    {
        initialised = false

        hoveredTile = null
        dartsThrown.clear()
        dartsThrown.addAll(darts)

        initialiseTiles(results, currentScore)

        when
        {
            toggleButtonComplete.isSelected -> displayTiles(completeTiles)
            else -> displayTiles(pendingTiles)
        }

        initialised = true
    }
    private fun initialiseTiles(results: List<DartzeeRoundResultEntity>, currentScore: Int)
    {
        completeTiles.clear()
        pendingTiles.clear()

        populateCompleteTiles(results)
        populateIncompleteTiles(results)

        updateIncompleteTilesBasedOnDarts(currentScore)
    }

    private fun populateCompleteTiles(results: List<DartzeeRoundResultEntity>)
    {
        results.sortedBy { it.roundNumber }.forEach { result ->
            val dto = dtos[result.ruleNumber - 1]
            val completeRule = DartzeeRuleTileComplete(
                dto,
                getRuleNumber(dto),
                result.success,
                result.score
            )
            completeTiles.add(completeRule)
        }
        toggleButtonComplete.isEnabled = completeTiles.isNotEmpty()
    }

    private fun populateIncompleteTiles(results: List<DartzeeRoundResultEntity>)
    {
        val incompleteRules = dtos.filterIndexed { ix, _ -> results.none { it.ruleNumber == ix + 1 }}
        pendingTiles.addAll(incompleteRules.map { rule ->
            DartzeeRuleTilePending(
                rule,
                getRuleNumber(rule)
            )
        })
        pendingTiles.forEach {
            it.addActionListener(this)
            it.addMouseListener(this)
            it.updateState(dartsThrown)
        }
    }

    private fun updateIncompleteTilesBasedOnDarts(currentScore: Int)
    {
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
    }

    private fun getRuleNumber(dto: DartzeeRuleDto) = dtos.indexOf(dto) + 1

    private fun getFirstIncompleteRule(): DartzeeRuleTilePending? = pendingTiles.firstOrNull()

    fun getSegmentStatus(): SegmentStatus
    {
        val statuses = pendingTiles.map { it.getSegmentStatus(dartsThrown) }
        return SegmentStatus(statuses.flatMap { it.scoringSegments }, statuses.flatMap { it.validSegments })
    }

    fun getAvailableRuleTiles() = pendingTiles.filter { it.isVisible }

    fun selectRule(model: DartsAiModel)
    {
        val aggressive = model.dartzeePlayStyle == DartzeePlayStyle.AGGRESSIVE

        val availableTiles = getAvailableRuleTiles()
        val selectedTile = if (aggressive)
        {
            val sortedTiles = availableTiles.sortedWith(compareBy( { it.pendingScore }, { it.ruleNumber }))
            sortedTiles.last()
        }
        else
        {
            availableTiles.maxBy { it.ruleNumber }
        }

        selectedTile!!.doClick()
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
            is DartzeeRuleTilePending -> tilePressed(src)
        }
    }

    private fun tilePressed(tile: DartzeeRuleTilePending)
    {
        if (tile.pendingResult != null) {
            val result = DartzeeRoundResult(tile.ruleNumber, tile.pendingResult!!, tile.pendingScore!!)
            listener?.tilePressed(result)
        }
    }

    override fun mouseEntered(e: MouseEvent?)
    {
        val src = e?.source
        if (src is DartzeeRuleTilePending)
        {
            hoveredTile = src
            listener?.hoverChanged(src.getSegmentStatus(dartsThrown))
        }
    }

    override fun mouseExited(e: MouseEvent?)
    {
        hoveredTile = null

        listener?.hoverChanged(getSegmentStatus())
    }

    override fun mousePressed(e: MouseEvent?) {}
    override fun mouseClicked(e: MouseEvent?) {}
    override fun mouseReleased(e: MouseEvent?) {}
}