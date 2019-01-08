package burlton.dartzee.code.screen.game

import burlton.core.code.obj.HashMapList
import burlton.core.code.util.Debug
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_BEST_FINISH
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_BEST_THREE_DART_SCORE
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_HIGHEST_BUST
import burlton.dartzee.code.achievements.insertForCheckoutCompleteness
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.DartEntity
import burlton.dartzee.code.utils.isBust
import burlton.dartzee.code.utils.isCheckoutScore
import burlton.dartzee.code.utils.shouldStopForMercyRule
import burlton.dartzee.code.utils.sumScore

class GamePanelX01(parent: DartsGameScreen) : GamePanelPausable<DartsScorerX01>(parent)
{
    //Transient variables for each round
    private var startingScore = -1
    private var currentScore = -1

    override fun initImpl(gameParams: String)
    {
        //Do nothing
    }

    override fun updateVariablesForNewRound()
    {
        startingScore = activeScorer.latestScoreRemaining
        currentScore = startingScore
    }

    override fun resetRoundVariables()
    {
        currentScore = startingScore
    }

    override fun saveDartsAndProceed()
    {
        //Finalise the scorer
        val lastDart = dartsThrown.lastElement()
        val bust = isBust(currentScore, lastDart)

        //Play a sound if we can...
        var totalScore = 0
        for (dart in dartsThrown)
        {
            totalScore += dart.getTotal()
        }

        if (!bust)
        {
            if (totalScore == 26)
            {
                dartboard.doFawlty()
            }

            dartboard.playDodgySound("" + totalScore)

            val total = sumScore(dartsThrown)
            AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_BEST_THREE_DART_SCORE, currentPlayerId, gameId, total)
        }
        else
        {
            val total = startingScore
            AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_HIGHEST_BUST, currentPlayerId, gameId, total)
        }

        activeScorer.finaliseRoundScore(startingScore, bust)

        super.saveDartsAndProceed()
    }

    /**
     * Loop through the darts thrown, saving them to the database.
     */
    override fun saveDartsToDatabase(roundId: Long)
    {
        var score = startingScore
        for (i in dartsThrown.indices)
        {
            val dart = dartsThrown[i]
            DartEntity.factoryAndSave(dart, roundId, i + 1, score)

            //Adjust the starting score for the next DartEntity
            val total = dart.getTotal()
            score -= total
        }
    }

    override fun currentPlayerHasFinished(): Boolean
    {
        val lastDart = dartsThrown.lastElement()
        return currentScore == 0 && lastDart.isDouble()
    }

    override fun updateAchievementsForFinish(playerId: Long, finishingPosition: Int, score: Int)
    {
        super.updateAchievementsForFinish(playerId, finishingPosition, score)

        val sum = sumScore(dartsThrown)
        AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_BEST_FINISH, playerId, gameId, sum)

        val checkout = dartsThrown.last().score
        insertForCheckoutCompleteness(playerId, gameId, checkout)
    }

    override fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, lastRound: Int)
    {
        val scorer = hmPlayerNumberToDartsScorer[playerNumber]
        for (i in 1..lastRound)
        {
            val darts = hmRoundToDarts[i]!!
            addDartsToScorer(darts, scorer)
        }

        val pt = hmPlayerNumberToParticipant[playerNumber]!!
        val finishPos = pt.finishingPosition
        if (finishPos > -1)
        {
            scorer?.finalisePlayerResult(finishPos)
        }
    }

    private fun addDartsToScorer(darts: MutableList<Dart>, scorer: DartsScorerX01?)
    {
        scorer ?: return

        val startingScore = scorer.latestScoreRemaining

        var score = startingScore
        for (dart in darts)
        {
            scorer.addDart(dart)

            score -= dart.getTotal()
        }

        val lastDart = darts.last()
        val bust = isBust(score, lastDart)
        scorer.finaliseRoundScore(startingScore, bust)
    }

    override fun updateVariablesForDartThrown(dart: Dart)
    {
        val dartTotal = dart.getTotal()
        currentScore -= dartTotal
    }

    override fun shouldStopAfterDartThrown(): Boolean
    {
        return if (dartsThrown.size == 3)
        {
            true
        }
        else currentScore <= 1

    }

    override fun doAiTurn(model: AbstractDartsModel)
    {
        if (shouldStopForMercyRule(model, startingScore, currentScore))
        {
            Debug.append("MERCY RULE", DartsGamePanel.VERBOSE_LOGGING)
            stopThrowing()
        }
        else
        {
            model.throwX01Dart(currentScore, dartboard)
        }

    }

    override fun factoryScorer(): DartsScorerX01
    {
        return DartsScorerX01.factory(this)
    }

    override fun factoryStatsPanel(): GameStatisticsPanel
    {
        return GameStatisticsPanelX01()
    }

    override fun shouldAnimateMiss(drt: Dart): Boolean
    {
        return !isCheckoutScore(currentScore)
    }
}
