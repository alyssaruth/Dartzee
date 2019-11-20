package burlton.dartzee.code.screen

import burlton.core.code.util.ceilDiv
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.bean.GameParamFilterPanelDartzee
import burlton.dartzee.code.dartzee.DartzeeRoundResult
import burlton.dartzee.code.db.DartzeeRoundResultEntity
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.listener.DartboardListener
import burlton.dartzee.code.screen.dartzee.DartboardRuleVerifier
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCarousel
import burlton.dartzee.code.screen.dartzee.IDartzeeCarouselHoverListener
import burlton.dartzee.code.screen.dartzee.IDartzeeTileListener
import burlton.dartzee.code.screen.game.scorer.DartsScorerDartzee
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel

class TestScreen: EmbeddedScreen(), DartboardListener, IDartzeeTileListener, IDartzeeCarouselHoverListener
{
    var carousel: DartzeeRuleCarousel = DartzeeRuleCarousel(this, listOf())
    val dartzeeSelector = GameParamFilterPanelDartzee()
    val panelNorth = JPanel()
    val panelCenter = JPanel()
    private val btnReset = JButton()
    private val btnConfirm = JButton()
    val dartboard = DartboardRuleVerifier()
    val scorer = DartsScorerDartzee()

    //Transient stuff
    val dartsThrown = mutableListOf<Dart>()
    val ruleResults = mutableListOf<DartzeeRoundResultEntity>()
    var roundNumber = 1
    var currentScore = 0

    init
    {
        layout = BorderLayout(0, 0)

        add(dartzeeSelector, BorderLayout.SOUTH)
        add(panelCenter, BorderLayout.CENTER)
        add(panelNorth, BorderLayout.NORTH)
        add(scorer, BorderLayout.WEST)

        panelNorth.layout = BorderLayout(0, 0)

        panelCenter.layout = BorderLayout(0, 0)
        panelCenter.add(dartboard, BorderLayout.CENTER)

        val btnPanel = JPanel()
        panelCenter.add(btnPanel, BorderLayout.SOUTH)
        btnPanel.add(btnReset)
        btnPanel.add(btnConfirm)

        btnReset.preferredSize = Dimension(60, 60)
        btnReset.icon = ImageIcon(javaClass.getResource("/buttons/Reset.png"))
        btnReset.toolTipText = "Reset darts"
        btnConfirm.isEnabled = false
        btnConfirm.preferredSize = Dimension(60, 60)
        btnConfirm.icon = ImageIcon(javaClass.getResource("/buttons/Confirm.png"))
        btnConfirm.toolTipText = "Confirm round"

        btnReset.addActionListener(this)
        btnConfirm.addActionListener(this)

        dartboard.renderScoreLabels = true
        dartboard.paintDartboard()

        dartboard.addDartboardListener(this)
        dartzeeSelector.addActionListener(this)
    }

    override fun actionPerformed(arg0: ActionEvent) {
        when (arg0.source)
        {
            btnNext, btnBack -> super.actionPerformed(arg0)
            btnReset -> clearDarts()
            btnConfirm -> confirmDarts()
            else -> startNewGame()
        }

    }

    private fun startNewGame()
    {
        ruleResults.clear()
        roundNumber = 1
        currentScore = 0

        val template = dartzeeSelector.getSelectedTemplate() ?: return
        val rules = DartzeeRuleEntity().retrieveForTemplate(template.rowId).map { it.toDto() }
        carousel = DartzeeRuleCarousel(this, rules)
        panelNorth.removeAll()
        panelNorth.add(carousel, BorderLayout.CENTER)

        dartboard.refreshValidSegments(carousel.getValidSegments())

        ScreenCache.getMainScreen().pack()
        panelNorth.repaint()
        repaint()
    }

    override fun initialise()
    {
        val player = PlayerEntity().retrieveEntities().first()
        scorer.init(player, "")
    }

    override fun getScreenName() = "Test Screen"

    private fun clearDarts()
    {
        dartsThrown.clear()

        scorer.clearRound(roundNumber)

        btnConfirm.isEnabled = false
        carousel.update(ruleResults, dartsThrown, roundNumber)

        dartboard.clearDarts()
        dartboard.ensureListening()
        dartboard.refreshValidSegments(carousel.getValidSegments())
    }

    private fun confirmDarts()
    {
        val result = carousel.getRoundResult()
        scorer.setResult(result)

        if (!result.userInputNeeded)
        {
            completeRound(result)
        }
        else
        {
            btnConfirm.isEnabled = false
            btnReset.isEnabled = false
            carousel.addTileListener(this)
        }
    }

    private fun completeRound(result: DartzeeRoundResult)
    {
        if (result.success)
        {
            currentScore += result.successScore
        }
        else
        {
            currentScore = currentScore.ceilDiv(2)
        }

        val entity = DartzeeRoundResultEntity()
        entity.ruleNumber = result.ruleNumber
        entity.success = result.success

        scorer.setResult(result, currentScore)
        if (roundNumber > 1)
        {
            ruleResults.add(entity)
        }

        roundNumber++

        clearDarts()
    }

    override fun dartThrown(dart: Dart)
    {
        dartsThrown.add(dart)
        btnReset.isEnabled = true

        carousel.update(ruleResults, dartsThrown, roundNumber)

        val validSegments = carousel.getValidSegments()
        if (validSegments.isEmpty() || dartsThrown.size == 3)
        {
            dartboard.stopListening()
            btnConfirm.isEnabled = true
        }

        scorer.addDart(dart)

        dartboard.refreshValidSegments(validSegments)
    }

    override fun tilePressed(dartzeeRoundResult: DartzeeRoundResult)
    {
        carousel.clearTileListener()

        completeRound(dartzeeRoundResult)
    }

    override fun hoverChanged(validSegments: List<DartboardSegment>)
    {
        if (dartsThrown.size < 3)
        {
            dartboard.refreshValidSegments(validSegments)
        }
    }
}