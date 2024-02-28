package dartzee.screen.game.dartzee

import dartzee.achievements.AchievementType
import dartzee.achievements.dartzee.DARTZEE_ACHIEVEMENT_MIN_ROUNDS
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
import dartzee.`object`.DEFAULT_COLOUR_WRAPPER
import dartzee.`object`.Dart
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.GamePanelFixedLength
import dartzee.screen.game.SegmentStatuses
import dartzee.screen.game.scorer.DartsScorerDartzee
import dartzee.utils.factoryHighScoreResult
import dartzee.utils.generateDartzeeTemplateFromGame
import dartzee.utils.getQuotedIdStr
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.ImageIcon
import javax.swing.JButton

class GamePanelDartzee(
    parent: AbstractDartsGameScreen,
    game: GameEntity,
    totalPlayers: Int,
    private val dtos: List<DartzeeRuleDto>,
    private val summaryPanel: DartzeeRuleSummaryPanel
) :
    GamePanelFixedLength<DartsScorerDartzee, DartzeePlayerState>(parent, game, totalPlayers),
    IDartzeeCarouselListener {
    override val totalRounds = dtos.size + 1
    private val btnConvertToTemplate = JButton("")

    init {
        btnConvertToTemplate.icon =
            ImageIcon(javaClass.getResource("/buttons/dartzeeTemplates.png"))
        btnConvertToTemplate.name = "convertToTemplate"
        btnConvertToTemplate.preferredSize = Dimension(80, 80)
        btnConvertToTemplate.toolTipText = "Generate template from game"
        btnConvertToTemplate.isVisible = game.gameParams.isEmpty()
        panelButtons.add(btnConvertToTemplate)

        add(summaryPanel, BorderLayout.NORTH)
        summaryPanel.setCarouselListener(this)
        btnConvertToTemplate.addActionListener(this)
    }

    override fun factoryState(pt: IWrappedParticipant) = DartzeePlayerState(pt)

    override fun computeAiDart(model: DartsAiModel) =
        if (isScoringRound()) {
            model.throwScoringDart()
        } else {
            summaryPanel.ensureReady()

            val segmentStatus = summaryPanel.getSegmentStatus()
            model.throwDartzeeDart(dartsThrownCount(), segmentStatus)
        }

    override fun colourWrapper() = DEFAULT_COLOUR_WRAPPER

    override fun setGameReadOnly() {
        super.setGameReadOnly()

        summaryPanel.gameFinished()
        scorerSelected(scorersOrdered.first())
    }

    override fun loadAdditionalEntities(state: DartzeePlayerState) {
        val individuals = state.wrappedParticipant.individuals
        val playerIds = individuals.getQuotedIdStr { it.playerId }
        val ptIds = individuals.getQuotedIdStr { it.rowId }

        val roundResults =
            DartzeeRoundResultEntity()
                .retrieveEntities("PlayerId IN $playerIds AND ParticipantId IN $ptIds")
                .sortedBy { it.roundNumber }
        roundResults.forEach { state.addRoundResult(it) }
    }

    override fun updateVariablesForDartThrown(dart: Dart) {
        updateCarouselAndDartboard()
    }

    override fun showConfirmButton() = isScoringRound()

    override fun shouldStopAfterDartThrown(): Boolean {
        val segmentStatus = summaryPanel.getSegmentStatus()
        val failedAllRules = segmentStatus.validSegments.isEmpty()
        return dartsThrownCount() == 3 || failedAllRules
    }

    override fun shouldAIStop() = false

    override fun readyForThrow() {
        super.readyForThrow()

        updateCarouselAndDartboard()
    }

    private fun updateCarouselAndDartboard() {
        runOnEventThreadBlocking {
            updateCarousel()
            dartboard.refreshValidSegments(summaryPanel.getSegmentStatus())
        }
    }

    private fun updateCarousel() {
        val ruleResults = getCurrentPlayerState().roundResults
        val lastRoundScore = getCurrentPlayerState().getCumulativeScore(currentRoundNumber - 1)
        summaryPanel.update(ruleResults, getDartsThrown(), lastRoundScore, currentRoundNumber)
    }

    /**
     * Hook called from the confirm button, which is only available for the initial high score round
     */
    override fun saveDartsAndProceed() {
        if (isScoringRound()) {
            completeRound(factoryHighScoreResult(getDartsThrown()))
        } else {
            // AI has finished a Dartzee round
            summaryPanel.selectRule(getCurrentPlayerStrategy())
        }
    }

    private fun isScoringRound() = currentRoundNumber == 1

    private fun completeRound(result: DartzeeRoundResult) {
        if (!isScoringRound()) {
            getCurrentPlayerState().saveRoundResult(result)
        }

        disableInputButtons()
        dartboard.clearDarts()

        commitRound()

        finishRound()

        if (gameEntity.isFinished()) {
            gameFinished()
        }
    }

    private fun gameFinished() {
        summaryPanel.gameFinished()
        dartboard.refreshValidSegments(null)

        updateCarousel()
    }

    override fun updateAchievementsForFinish(
        playerState: DartzeePlayerState,
        finishingPosition: Int,
        score: Int
    ) {
        super.updateAchievementsForFinish(playerState, finishingPosition, score)
        if (totalRounds < DARTZEE_ACHIEVEMENT_MIN_ROUNDS) {
            return
        }

        val playerId = playerState.lastIndividual().playerId
        if (!playerState.hasMultiplePlayers()) {
            val scorePerRound = score / totalRounds
            AchievementEntity.updateAchievement(
                AchievementType.DARTZEE_BEST_GAME,
                playerId,
                gameEntity.rowId,
                scorePerRound
            )

            if (playerState.roundResults.all { it.success }) {
                val templateName = GameType.DARTZEE.getParamsDescription(gameEntity.gameParams)
                AchievementEntity.insertAchievement(
                    AchievementType.DARTZEE_FLAWLESS,
                    playerId,
                    gameEntity.rowId,
                    templateName,
                    score
                )
            }

            AchievementEntity.insertForUniqueCounter(
                AchievementType.DARTZEE_BINGO,
                playerId,
                gameEntity.rowId,
                score % 100,
                "$score"
            )
        }

        val lastRoundResult = playerState.roundResults.last()
        if (lastRoundResult.success && lastRoundResult.ruleNumber == dtos.size) {
            val ruleDescription = dtos.last().getDisplayName()
            AchievementEntity.insertAchievement(
                AchievementType.DARTZEE_UNDER_PRESSURE,
                playerId,
                gameEntity.rowId,
                ruleDescription,
                lastRoundResult.score
            )
        }
    }

    override fun factoryStatsPanel(gameParams: String) = GameStatisticsPanelDartzee()

    override fun factoryScorer(participant: IWrappedParticipant) =
        DartsScorerDartzee(this, participant)

    override fun hoverChanged(segmentStatuses: SegmentStatuses) {
        if (dartsThrownCount() < 3) {
            dartboard.refreshValidSegments(segmentStatuses)
        }
    }

    override fun tilePressed(dartzeeRoundResult: DartzeeRoundResult) {
        completeRound(dartzeeRoundResult)
    }

    fun scorerSelected(selectedScorer: DartsScorerDartzee) {
        currentPlayerNumber = getPlayerNumberForScorer(selectedScorer)

        scorersOrdered.forEach { scorer -> scorer.togglePostGame(scorer == selectedScorer) }

        updateCarousel()
    }

    private fun generateTemplate() {
        generateDartzeeTemplateFromGame(gameEntity, dtos) ?: return

        btnConvertToTemplate.isVisible = false
        parentWindow.title = makeGameTitle()
    }

    override fun actionPerformed(arg0: ActionEvent) {
        when (arg0.source) {
            btnConvertToTemplate -> generateTemplate()
            else -> super.actionPerformed(arg0)
        }
    }
}
