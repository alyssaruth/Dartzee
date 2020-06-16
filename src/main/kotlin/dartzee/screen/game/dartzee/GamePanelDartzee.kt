package dartzee.screen.game.dartzee

import dartzee.`object`.Dart
import dartzee.ai.AbstractDartsModel
import dartzee.core.obj.HashMapList
import dartzee.dartzee.DartzeeRoundResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.game.state.DartzeePlayerState
import dartzee.screen.dartzee.DartzeeDartboard
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.GamePanelFixedLength
import dartzee.screen.game.scorer.DartsScorerDartzee
import dartzee.utils.factoryHighScoreResult
import java.awt.BorderLayout

class GamePanelDartzee(parent: AbstractDartsGameScreen,
                       game: GameEntity,
                       totalPlayers: Int,
                       val dtos: List<DartzeeRuleDto>,
                       private val summaryPanel: DartzeeRuleSummaryPanel
) : GamePanelFixedLength<DartsScorerDartzee, DartzeeDartboard, DartzeePlayerState>(parent, game, totalPlayers),
    IDartzeeCarouselListener
{
    override val totalRounds = dtos.size + 1

    //Transient things
    var lastRoundScore = -1

    init
    {
        add(summaryPanel, BorderLayout.NORTH)
        summaryPanel.setCarouselListener(this)
    }

    override fun factoryDartboard() = DartzeeDartboard()
    override fun factoryState(pt: ParticipantEntity, scorer: DartsScorerDartzee) = DartzeePlayerState(pt, scorer)

    override fun doAiTurn(model: AbstractDartsModel)
    {
        val segmentStatus = summaryPanel.getSegmentStatus()
        if (segmentStatus == null)
        {
            model.throwScoringDart(dartboard)
            return
        }

        // Past the scoring round
        model.throwDartzeeDart(dartsThrown.size, dartboard, segmentStatus)
    }

    override fun setGameReadOnly()
    {
        super.setGameReadOnly()

        summaryPanel.gameFinished()
        scorerSelected(scorersOrdered.first())
    }

    override fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, totalRounds: Int)
    {
        val pt = getParticipant(playerNumber)

        val roundResults = DartzeeRoundResultEntity().retrieveEntities("PlayerId = '${pt.playerId}' AND ParticipantId = '${pt.rowId}'")

        roundResults.forEach { getPlayerState(playerNumber).addRoundResult(it) }

        val scorer = getScorer(playerNumber)
        for (i in 1..totalRounds)
        {
            val darts = hmRoundToDarts[i]!!
            darts.forEach { scorer.addDart(it) }

            if (i == 1)
            {
                val result = factoryHighScoreResult(darts)
                scorer.setResult(result)
            }
            else
            {
                val result = roundResults.find { it.roundNumber == i }!!
                scorer.setResult(result.toDto())
            }
        }
    }

    override fun updateVariablesForNewRound()
    {
        lastRoundScore = activeScorer.getTotalScore()
    }

    override fun resetRoundVariables() {}

    override fun updateVariablesForDartThrown(dart: Dart)
    {
        updateCarouselAndDartboard()
    }

    override fun shouldStopAfterDartThrown(): Boolean
    {
        val segmentStatus = summaryPanel.getSegmentStatus()
        val failedAllRules = segmentStatus?.validSegments?.isEmpty() ?: false
        return dartsThrown.size == 3 || failedAllRules
    }

    override fun shouldAIStop() = shouldStopAfterDartThrown()

    override fun readyForThrow()
    {
        super.readyForThrow()

        updateCarouselAndDartboard()

        if (!isScoringRound())
        {
            btnConfirm.isVisible = false
        }
    }

    private fun updateCarouselAndDartboard()
    {
        updateCarousel()
        dartboard.refreshValidSegments(summaryPanel.getSegmentStatus())
    }
    private fun updateCarousel()
    {
        val ruleResults = getCurrentPlayerState().roundResults
        summaryPanel.update(ruleResults, dartsThrown, lastRoundScore, currentRoundNumber)
    }

    /**
     * Hook called from the confirm button, which is only available for the initial high score round
     */
    override fun saveDartsAndProceed()
    {
        if (isScoringRound())
        {
            completeRound(factoryHighScoreResult(dartsThrown))
        }
        else
        {
            //AI has finished a Dartzee round
            val pendingTiles = summaryPanel.getPendingTiles()

            //TODO - aggressive player should prioritise score first
            //val model = getCurrentPlayerStrategy()

            pendingTiles.maxBy { it.ruleNumber }!!.doClick()
        }
    }

    private fun isScoringRound() = currentRoundNumber == 1

    private fun completeRound(result: DartzeeRoundResult)
    {
        val pt = getCurrentParticipant()

        activeScorer.setResult(result)
        if (!isScoringRound())
        {
            val entity = DartzeeRoundResultEntity.factoryAndSave(result, pt, currentRoundNumber)
            getCurrentPlayerState().addRoundResult(entity)
        }

        disableInputButtons()
        dartboard.clearDarts()

        saveDartsToDatabase()

        finishRound()

        if (gameEntity.isFinished())
        {
            gameFinished()
        }
    }

    private fun gameFinished()
    {
        summaryPanel.gameFinished()
        dartboard.refreshValidSegments(null)

        updateCarousel()
    }

    override fun factoryStatsPanel(gameParams: String) = GameStatisticsPanelDartzee()
    override fun factoryScorer() = DartsScorerDartzee(this)

    override fun hoverChanged(segmentStatus: SegmentStatus)
    {
        if (dartsThrown.size < 3)
        {
            dartboard.refreshValidSegments(segmentStatus)
        }
    }

    override fun tilePressed(dartzeeRoundResult: DartzeeRoundResult)
    {
        completeRound(dartzeeRoundResult)
    }

    fun scorerSelected(scorer: DartsScorerDartzee)
    {
        scorersOrdered.forEach { it.setSelected(false) }
        scorer.setSelected(true)

        currentPlayerNumber = getPlayerNumberForScorer(scorer)
        updateCarousel()
    }
}