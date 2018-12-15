package burlton.dartzee.code.screen.game

import burlton.core.code.obj.HandyArrayList
import burlton.core.code.obj.HashMapList
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.DartEntity

class GamePanelRoundTheClock(parent: DartsGameScreen) : GamePanelPausable<DartsScorerRoundTheClock>(parent)
{
    private var clockType = ""

    override fun doAiTurn(model: AbstractDartsModel)
    {
        val currentTarget = activeScorer.currentClockTarget
        model.throwClockDart(currentTarget, clockType, dartboard)
    }

    override fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, lastRound: Int)
    {
        val scorer = hmPlayerNumberToDartsScorer[playerNumber]!!
        for (i in 1..lastRound)
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
    }

    private fun addDartsToScorer(darts: HandyArrayList<Dart>, scorer: DartsScorerRoundTheClock)
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
        val currentClockTarget = activeScorer.currentClockTarget
        dart.startingScore = currentClockTarget

        if (dart.hitClockTarget(clockType))
        {
            activeScorer.incrementCurrentClockTarget()

            if (dartsThrown.size == 4)
            {
                dartboard.doForsyth()
            }
        }
        else if (dartsThrown.size != 4)
        {
            activeScorer.disableBrucey()
        }
    }

    override fun shouldStopAfterDartThrown(): Boolean
    {
        if (dartsThrown.size == 4)
        {
            return true
        }

        if (activeScorer.currentClockTarget > 20)
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

    override fun saveDartsToDatabase(roundId: Long)
    {
        for (i in dartsThrown.indices)
        {
            val dart = dartsThrown[i]
            val target = dart.startingScore
            DartEntity.factoryAndSave(dart, roundId, i + 1, target)
        }

        if (dartsThrown.size == 4
          && dartsThrown.last().hitClockTarget(clockType))
        {
            AchievementEntity.incrementAchievement(ACHIEVEMENT_REF_CLOCK_BRUCEY_BONUSES, currentPlayerId, gameEntity.rowId, 1)
        }
    }

    override fun currentPlayerHasFinished(): Boolean
    {
        return activeScorer.currentClockTarget > 20
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
