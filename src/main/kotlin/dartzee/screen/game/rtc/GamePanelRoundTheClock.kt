package dartzee.screen.game.rtc

import dartzee.achievements.AchievementType
import dartzee.ai.DartsAiModel
import dartzee.core.util.doBadLuck
import dartzee.core.util.doForsyth
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.game.RoundTheClockConfig
import dartzee.game.state.ClockPlayerState
import dartzee.game.state.IWrappedParticipant
import dartzee.`object`.ComputedPoint
import dartzee.`object`.Dart
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.GamePanelPausable
import dartzee.screen.game.scorer.DartsScorerRoundTheClock

class GamePanelRoundTheClock(parent: AbstractDartsGameScreen, game: GameEntity, totalPlayers: Int) :
    GamePanelPausable<DartsScorerRoundTheClock, ClockPlayerState>(parent, game, totalPlayers) {
    private val config = RoundTheClockConfig.fromJson(game.gameParams)

    override fun factoryState(pt: IWrappedParticipant) = ClockPlayerState(config, pt)

    override fun computeAiDart(model: DartsAiModel): ComputedPoint {
        val currentTarget = getCurrentPlayerState().getCurrentTarget()
        return model.throwClockDart(currentTarget, config.clockType)
    }

    override fun updateVariablesForDartThrown(dart: Dart) {
        if (dart.hitClockTarget(config.clockType) && dartsThrownCount() == 4) {
            dartboard.doForsyth()
        } else if (dartsThrownCount() == 4) {
            dartboard.doBadLuck()
        }

        updateDartboard()
    }

    override fun readyForThrow() {
        super.readyForThrow()
        updateDartboard()
    }

    override fun shouldAnimateMiss(dart: Dart) = dartsThrownCount() < 4

    override fun shouldStopAfterDartThrown(): Boolean {
        if (dartsThrownCount() == 4) {
            return true
        }

        if (getCurrentPlayerState().findCurrentTarget() == null) {
            // Finished.
            return true
        }

        val allHits = getDartsThrown().all { it.hitClockTarget(config.clockType) }
        return dartsThrownCount() == 3 && !allHits
    }

    override fun showConfirmButton() = shouldStopAfterDartThrown()

    override fun saveDartsAndProceed() {
        if (dartsThrownCount() == 4 && getDartsThrown().last().hitClockTarget(config.clockType)) {
            AchievementEntity.insertAchievement(
                AchievementType.CLOCK_BRUCEY_BONUSES,
                getCurrentPlayerId(),
                getGameId(),
                "$currentRoundNumber"
            )
        }

        updateBestStreakAchievement()

        super.saveDartsAndProceed()
    }

    private fun updateBestStreakAchievement() {
        if (getCurrentPlayerState().hasMultiplePlayers()) {
            return
        }

        val longestStreakThisGame = getCurrentPlayerState().getLongestStreak()
        if (longestStreakThisGame > 1) {
            AchievementEntity.updateAchievement(
                AchievementType.CLOCK_BEST_STREAK,
                getCurrentPlayerId(),
                getGameId(),
                longestStreakThisGame
            )
        }
    }

    private fun updateDartboard() {
        val state = getCurrentPlayerState()
        val segmentStatus = state.getSegmentStatus()
        dartboard.refreshValidSegments(segmentStatus)
    }

    override fun currentPlayerHasFinished() = getCurrentPlayerState().findCurrentTarget() == null

    override fun factoryScorer(participant: IWrappedParticipant) =
        DartsScorerRoundTheClock(
            this,
            RoundTheClockConfig.fromJson(gameEntity.gameParams),
            participant
        )

    override fun factoryStatsPanel(gameParams: String) =
        GameStatisticsPanelRoundTheClock(gameParams)
}
