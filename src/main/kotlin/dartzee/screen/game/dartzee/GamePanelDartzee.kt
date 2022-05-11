package dartzee.screen.game.dartzee

import dartzee.achievements.AchievementType
import dartzee.achievements.dartzee.DARTZEE_ACHIEVEMENT_MIN_RULES
import dartzee.ai.DartsAiModel
import dartzee.core.util.runOnEventThreadBlocking
import dartzee.dartzee.DartzeeRoundResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.AchievementEntity
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.GameEntity
import dartzee.game.GameType
import dartzee.game.state.DartzeePlayerState
import dartzee.game.state.IWrappedParticipant
import dartzee.`object`.Dart
import dartzee.screen.dartzee.DartzeeDartboard
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.GamePanelFixedLength
import dartzee.screen.game.scorer.DartsScorerDartzee
import dartzee.utils.factoryHighScoreResult
import dartzee.utils.getQuotedIdStr
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

    init
    {
        add(summaryPanel, BorderLayout.NORTH)
        summaryPanel.setCarouselListener(this)
    }

    override fun factoryDartboard() = DartzeeDartboard()
    override fun factoryState(pt: IWrappedParticipant) = DartzeePlayerState(pt)

    override fun doAiTurn(model: DartsAiModel)
    {
        if (isScoringRound())
        {
            val pt = model.throwScoringDart(dartboard)
            dartboard.dartThrown(pt)
        }
        else
        {
            summaryPanel.ensureReady()

            val segmentStatus = summaryPanel.getSegmentStatus()
            model.throwDartzeeDart(dartsThrownCount(), dartboard, segmentStatus)
        }
    }

    override fun setGameReadOnly()
    {
        super.setGameReadOnly()

        summaryPanel.gameFinished()
        scorerSelected(scorersOrdered.first())
    }

    override fun loadAdditionalEntities(state: DartzeePlayerState)
    {
        val individuals = state.wrappedParticipant.individuals
        val playerIds = individuals.getQuotedIdStr { it.playerId }
        val ptIds = individuals.getQuotedIdStr { it.rowId }

        val roundResults = DartzeeRoundResultEntity().retrieveEntities("PlayerId IN $playerIds AND ParticipantId IN $ptIds").sortedBy { it.roundNumber }
        roundResults.forEach { state.addRoundResult(it) }
    }

    override fun updateVariablesForDartThrown(dart: Dart)
    {
        updateCarouselAndDartboard()
    }

    override fun shouldStopAfterDartThrown(): Boolean
    {
        val segmentStatus = summaryPanel.getSegmentStatus()
        val failedAllRules = segmentStatus.validSegments.isEmpty()
        return dartsThrownCount() == 3 || failedAllRules
    }

    override fun shouldAIStop() = false

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
        runOnEventThreadBlocking {
            updateCarousel()
            dartboard.refreshValidSegments(summaryPanel.getSegmentStatus())
        }
    }
    private fun updateCarousel()
    {
        val ruleResults = getCurrentPlayerState().roundResults
        val lastRoundScore = getCurrentPlayerState().getCumulativeScore(currentRoundNumber - 1)
        summaryPanel.update(ruleResults, getDartsThrown(), lastRoundScore, currentRoundNumber)
    }

    /**
     * Hook called from the confirm button, which is only available for the initial high score round
     */
    override fun saveDartsAndProceed()
    {
        if (isScoringRound())
        {
            completeRound(factoryHighScoreResult(getDartsThrown()))
        }
        else
        {
            //AI has finished a Dartzee round
            summaryPanel.selectRule(getCurrentPlayerStrategy())
        }
    }

    private fun isScoringRound() = currentRoundNumber == 1

    private fun completeRound(result: DartzeeRoundResult)
    {
        if (!isScoringRound())
        {
            getCurrentPlayerState().saveRoundResult(result)
        }

        disableInputButtons()
        dartboard.clearDarts()

        commitRound()

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

    override fun updateAchievementsForFinish(playerId: String, finishingPosition: Int, score: Int)
    {
        super.updateAchievementsForFinish(playerId, finishingPosition, score)

        if (totalRounds >= DARTZEE_ACHIEVEMENT_MIN_RULES)
        {
            val scorePerRound = score / totalRounds
            AchievementEntity.updateAchievement(AchievementType.DARTZEE_BEST_GAME, playerId, gameEntity.rowId, scorePerRound)

            val playerState = getCurrentPlayerState()
            if (playerState.roundResults.all { it.success })
            {
                val templateName = GameType.DARTZEE.getParamsDescription(gameEntity.gameParams)
                AchievementEntity.insertAchievement(AchievementType.DARTZEE_FLAWLESS, playerId, gameEntity.rowId, templateName, score)
            }

            val lastRoundResult = playerState.roundResults.last()
            if (lastRoundResult.success && lastRoundResult.ruleNumber == dtos.size)
            {
                val ruleDescription = dtos.last().getDisplayName()
                AchievementEntity.insertAchievement(AchievementType.DARTZEE_UNDER_PRESSURE, playerId, gameEntity.rowId, ruleDescription, lastRoundResult.score)
            }

            AchievementEntity.insertForUniqueCounter(AchievementType.DARTZEE_BINGO, playerId, gameEntity.rowId, score % 100, "$score")
        }
    }

    override fun factoryStatsPanel(gameParams: String) = GameStatisticsPanelDartzee()
    override fun factoryScorer(participant: IWrappedParticipant) = DartsScorerDartzee(this, participant)

    override fun hoverChanged(segmentStatus: SegmentStatus)
    {
        if (dartsThrownCount() < 3)
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
        currentPlayerNumber = getPlayerNumberForScorer(scorer)

        updateActivePlayer()
        updateCarousel()
    }
}