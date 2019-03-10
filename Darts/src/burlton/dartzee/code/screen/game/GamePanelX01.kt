package burlton.dartzee.code.screen.game

import burlton.core.code.obj.HashMapCount
import burlton.core.code.obj.HashMapList
import burlton.core.code.util.Debug
import burlton.dartzee.code.`object`.CheckoutSuggester
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.achievements.*
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.DartEntity
import burlton.dartzee.code.utils.*

class GamePanelX01(parent: DartsGameScreen) : GamePanelPausable<DartsScorerX01>(parent)
{
    //Transient variables for each round
    private var startingScore = -1
    private var currentScore = -1

    private val hmPlayerNumberToBadLuckCount = HashMapCount<Int>()

    override fun initImpl(gameParams: String)
    {
        //Do nothing
    }

    override fun updateVariablesForNewRound()
    {
        startingScore = activeScorer.getLatestScoreRemaining()
        resetRoundVariables()
    }

    override fun resetRoundVariables()
    {
        currentScore = startingScore
        suggestCheckout()
    }

    override fun saveDartsAndProceed()
    {
        //Finalise the scorer
        val lastDart = dartsThrown.lastElement()
        val bust = isBust(currentScore, lastDart)

        updateNearMisses(dartsThrown, currentPlayerNumber)

        val count = hmPlayerNumberToBadLuckCount.getCount(currentPlayerNumber)
        if (count > 0)
        {
            AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_SUCH_BAD_LUCK, currentPlayerId, gameId, count)
        }

        if (!bust)
        {
            val totalScore = sumScore(dartsThrown)
            if (totalScore == 26)
            {
                dartboard.doFawlty()

                updateHotelInspector()
            }

            if (isShanghai(dartsThrown))
            {
                AchievementEntity.insertAchievement(ACHIEVEMENT_REF_X01_SHANGHAI, currentPlayerId, gameId)
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

    private fun updateNearMisses(darts: MutableList<Dart>, playerNumber: Int)
    {
        darts.forEach{
            if (isNearMissDouble(it))
            {
                hmPlayerNumberToBadLuckCount.incrementCount(playerNumber)
            }
        }
    }

    private fun updateHotelInspector()
    {
        //Need to have thrown 3 darts, all of which didn't miss.
        if (dartsThrown.any { d -> d.multiplier == 0 }
          || dartsThrown.size < 3)
        {
            return
        }

        val methodStr = getSortedDartStr(dartsThrown)
        val whereSql = "AchievementRef = $ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR " +
                       "AND PlayerId = $currentPlayerId " +
                       "AND AchievementDetail = '$methodStr'"

        val existingRow = AchievementEntity().retrieveEntity(whereSql)
        if (existingRow == null)
        {
            AchievementEntity.insertAchievement(ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR, currentPlayerId, gameId, methodStr)
        }
    }

    /**
     * Loop through the darts thrown, saving them to the database.
     */
    override fun saveDartsToDatabase(roundId: Long)
    {
        for (i in dartsThrown.indices)
        {
            val dart = dartsThrown[i]
            DartEntity.factoryAndSave(dart, roundId, i + 1, dart.startingScore)
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

            updateNearMisses(darts, playerNumber)
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

        val startingScore = scorer.getLatestScoreRemaining()

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
        dart.startingScore = currentScore

        val dartTotal = dart.getTotal()
        currentScore -= dartTotal

        if (isNearMissDouble(dart))
        {
            dartboard.doBadLuck()
        }

        suggestCheckout()
    }

    private fun suggestCheckout()
    {
        val dartsRemaining = 3 - dartsThrown.size
        val checkout = CheckoutSuggester.suggestCheckout(currentScore, dartsRemaining) ?: return

        checkout.forEach {
            activeScorer.addHint(it)
        }
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
