package dartzee.screen.game.rtc

import dartzee.`object`.Dart
import dartzee.achievements.ACHIEVEMENT_REF_CLOCK_BEST_STREAK
import dartzee.achievements.ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES
import dartzee.ai.DartsAiModel
import dartzee.core.obj.HashMapCount
import dartzee.core.obj.HashMapList
import dartzee.core.util.doBadLuck
import dartzee.core.util.doForsyth
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.game.RoundTheClockConfig
import dartzee.game.state.ClockPlayerState
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.GamePanelPausable
import dartzee.screen.game.scorer.DartsScorerRoundTheClock

open class GamePanelRoundTheClock(parent: AbstractDartsGameScreen, game: GameEntity, totalPlayers: Int) : GamePanelPausable<DartsScorerRoundTheClock, ClockPlayerState>(parent, game, totalPlayers)
{
    private val config = RoundTheClockConfig.fromJson(game.gameParams)
    val hmPlayerNumberToCurrentStreak = HashMapCount<Int>()

    override fun factoryState(pt: ParticipantEntity) = ClockPlayerState(pt)

    override fun doAiTurn(model: DartsAiModel)
    {
        val currentTarget = getCurrentPlayerState().getCurrentTarget(config.clockType)
        model.throwClockDart(currentTarget, config.clockType, dartboard)
    }

    override fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, totalRounds: Int)
    {
        val scorer = getScorer(playerNumber)
        for (i in 1..totalRounds)
        {
            val darts = hmRoundToDarts[i]!!
            addDartsToScorer(darts, scorer)
        }

        val pt = getParticipant(playerNumber)
        val finishPos = pt.finishingPosition
        if (finishPos > -1)
        {
            scorer.finalisePlayerResult(finishPos)
        }

        loadCurrentStreak(playerNumber, hmRoundToDarts)
    }

    private fun loadCurrentStreak(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>)
    {
        var currentStreak = 0

        val dartsLatestFirst = hmRoundToDarts.getFlattenedValuesSortedByKey().reversed()
        for (drt in dartsLatestFirst)
        {
            if (!drt.hitClockTarget(config.clockType)) { break }

            currentStreak++
        }

        hmPlayerNumberToCurrentStreak[playerNumber] = currentStreak
    }

    private fun addDartsToScorer(darts: MutableList<Dart>, scorer: DartsScorerRoundTheClock)
    {
        var clockTarget = 1

        for (dart in darts)
        {
            dart.startingScore = clockTarget
            scorer.addDart(dart)

            if (dart.hitClockTarget(config.clockType))
            {
                scorer.incrementCurrentClockTarget()
                clockTarget++
            }
        }

        //Need to take brucey into account
        if (darts.size < 4)
        {
            scorer.disableBrucey()
        }

        scorer.confirmCurrentRound()
    }

    override fun updateVariablesForNewRound() {}

    override fun resetRoundVariables() {}

    override fun dartThrown(dart: Dart)
    {
        val currentClockTarget = getCurrentPlayerState().getCurrentTarget(config.clockType)
        dart.startingScore = currentClockTarget

        super.dartThrown(dart)
    }

    override fun updateVariablesForDartThrown(dart: Dart)
    {
        if (dart.hitClockTarget(config.clockType))
        {
            activeScorer.incrementCurrentClockTarget()

            if (dartsThrownCount() == 4)
            {
                dartboard.doForsyth()
            }
        }
        else if (dartsThrownCount() == 4)
        {
            dartboard.doBadLuck()
        }
        else
        {
            activeScorer.disableBrucey()
        }
    }

    override fun shouldAnimateMiss(dart: Dart): Boolean
    {
        return dartsThrownCount() < 4
    }

    override fun shouldStopAfterDartThrown(): Boolean
    {
        if (dartsThrownCount() == 4)
        {
            return true
        }

        if (getCurrentPlayerState().getCurrentTarget(config.clockType) > 20)
        {
            //Finished.
            return true
        }

        val allHits = getDartsThrown().all { it.hitClockTarget(config.clockType) }
        return dartsThrownCount() == 3 && !allHits

    }

    override fun mustContinueThrowing(): Boolean
    {
        return !shouldStopAfterDartThrown()
    }

    override fun saveDartsAndProceed()
    {
        if (dartsThrownCount() == 4 && getDartsThrown().last().hitClockTarget(config.clockType))
        {
            AchievementEntity.incrementAchievement(ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES, getCurrentPlayerId(), getGameId())
        }

        updateBestStreakAchievement()

        super.saveDartsAndProceed()
    }

    fun updateBestStreakAchievement()
    {
        var currentStreak = hmPlayerNumberToCurrentStreak.getCount(currentPlayerNumber)
        getDartsThrown().forEach {
            if (it.hitClockTarget(config.clockType))
            {
                currentStreak++
            }
            else
            {
                if (currentStreak > 1)
                {
                    AchievementEntity.updateAchievement(ACHIEVEMENT_REF_CLOCK_BEST_STREAK, getCurrentPlayerId(), getGameId(), currentStreak)
                }

                currentStreak = 0
            }
        }

        if (currentStreak > 1)
        {
            AchievementEntity.updateAchievement(ACHIEVEMENT_REF_CLOCK_BEST_STREAK, getCurrentPlayerId(), getGameId(), currentStreak)
        }

        hmPlayerNumberToCurrentStreak[currentPlayerNumber] = currentStreak
    }


    override fun currentPlayerHasFinished() = getCurrentPlayerState().getCurrentTarget(config.clockType) > 20

    override fun factoryScorer() = DartsScorerRoundTheClock(this, RoundTheClockConfig.fromJson(gameEntity.gameParams).clockType)

    override fun factoryStatsPanel(gameParams: String) = GameStatisticsPanelRoundTheClock(gameParams)

}
