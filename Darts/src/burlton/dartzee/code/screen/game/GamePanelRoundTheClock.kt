package burlton.dartzee.code.screen.game

import burlton.core.code.obj.HashMapCount
import burlton.core.code.obj.HashMapList
import burlton.core.code.util.Debug
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_CLOCK_BEST_STREAK
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GameEntity

open class GamePanelRoundTheClock(parent: AbstractDartsGameScreen, game: GameEntity) : GamePanelPausable<DartsScorerRoundTheClock>(parent, game)
{
    private var clockType = ""
    val hmPlayerNumberToCurrentStreak = HashMapCount<Int>()

    override fun doAiTurn(model: AbstractDartsModel)
    {
        val currentTarget = activeScorer!!.currentClockTarget
        model.throwClockDart(currentTarget, clockType, dartboard)
    }

    override fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, totalRounds: Int)
    {
        val scorer = hmPlayerNumberToDartsScorer[playerNumber]!!
        for (i in 1..totalRounds)
        {
            val darts = hmRoundToDarts[i]!!
            addDartsToScorer(darts, scorer)
        }

        val pt = hmPlayerNumberToParticipant[playerNumber]
        val finishPos = pt?.finishingPosition ?: -1
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
        Debug.append("" + dartsLatestFirst)
        for (drt in dartsLatestFirst)
        {
            if (!drt.hitClockTarget(clockType)) { break }

            currentStreak++
        }

        hmPlayerNumberToCurrentStreak[playerNumber] = currentStreak

        Debug.append("Player #$playerNumber: $currentStreak")
    }

    private fun addDartsToScorer(darts: MutableList<Dart>, scorer: DartsScorerRoundTheClock)
    {
        var clockTarget = scorer.currentClockTarget

        for (dart in darts)
        {
            dart.startingScore = clockTarget
            scorer.addDart(dart)

            if (dart.hitClockTarget(clockType))
            {
                scorer.incrementCurrentClockTarget()
                clockTarget = scorer.currentClockTarget
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

    override fun updateVariablesForDartThrown(dart: Dart)
    {
        val currentClockTarget = activeScorer!!.currentClockTarget
        dart.startingScore = currentClockTarget

        if (dart.hitClockTarget(clockType))
        {
            activeScorer!!.incrementCurrentClockTarget()

            if (dartsThrown.size == 4)
            {
                dartboard.doForsyth()
            }
        }
        else if (dartsThrown.size != 4)
        {
            activeScorer!!.disableBrucey()
        }
    }

    override fun shouldStopAfterDartThrown(): Boolean
    {
        if (dartsThrown.size == 4)
        {
            return true
        }

        if (activeScorer!!.currentClockTarget > 20)
        {
            //Finished.
            return true
        }

        var allHits = true
        for (dart in dartsThrown)
        {
            allHits = allHits and dart.hitClockTarget(clockType)
        }

        return dartsThrown.size == 3 && !allHits

    }

    override fun mustContinueThrowing(): Boolean
    {
        return !shouldStopAfterDartThrown()
    }

    override fun saveDartsAndProceed()
    {
        if (dartsThrown.size == 4
                && dartsThrown.last().hitClockTarget(clockType))
        {
            AchievementEntity.incrementAchievement(ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES, getCurrentPlayerId(), getGameId())
        }

        updateBestStreakAchievement()

        super.saveDartsAndProceed()
    }

    fun updateBestStreakAchievement()
    {
        var currentStreak = hmPlayerNumberToCurrentStreak.getCount(currentPlayerNumber)
        dartsThrown.forEach {
            if (it.hitClockTarget(clockType))
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


    override fun currentPlayerHasFinished(): Boolean
    {
        return activeScorer!!.currentClockTarget > 20
    }

    override fun initImpl(gameParams: String)
    {
        this.clockType = gameParams
    }

    override fun factoryScorer(): DartsScorerRoundTheClock
    {
        val scorer = DartsScorerRoundTheClock()
        scorer.setParent(this)
        return scorer
    }

    override fun factoryStatsPanel(): GameStatisticsPanel
    {
        return GameStatisticsPanelRoundTheClock()
    }

}
