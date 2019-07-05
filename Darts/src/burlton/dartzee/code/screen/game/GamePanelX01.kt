package burlton.dartzee.code.screen.game

import burlton.core.code.obj.HashMapCount
import burlton.core.code.obj.HashMapList
import burlton.core.code.util.Debug
import burlton.dartzee.code.`object`.CheckoutSuggester
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.achievements.*
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.utils.*

open class GamePanelX01(parent: AbstractDartsGameScreen) : GamePanelPausable<DartsScorerX01>(parent)
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
        startingScore = activeScorer!!.getLatestScoreRemaining()
        resetRoundVariables()
    }

    override fun resetRoundVariables()
    {
        currentScore = startingScore
    }

    override fun readyForThrow()
    {
        super.readyForThrow()

        suggestCheckout()
    }

    override fun saveDartsAndProceed()
    {
        //Finalise the scorer
        val lastDart = dartsThrown.last()
        val bust = isBust(currentScore, lastDart)

        updateNearMisses(dartsThrown, currentPlayerNumber)

        val count = hmPlayerNumberToBadLuckCount.getCount(currentPlayerNumber)
        if (count > 0)
        {
            AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_SUCH_BAD_LUCK, getCurrentPlayerId(), getGameId(), count)
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
                AchievementEntity.insertAchievement(ACHIEVEMENT_REF_X01_SHANGHAI, getCurrentPlayerId(), getGameId())
            }

            dartboard.playDodgySound("" + totalScore)

            val total = sumScore(dartsThrown)
            AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_BEST_THREE_DART_SCORE, getCurrentPlayerId(), getGameId(), total)
        }
        else
        {
            val total = startingScore
            AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_HIGHEST_BUST, getCurrentPlayerId(), getGameId(), total)
        }

        activeScorer!!.finaliseRoundScore(startingScore, bust)

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
                       "AND PlayerId = '${getCurrentPlayerId()}' " +
                       "AND AchievementDetail = '$methodStr'"

        val existingRow = AchievementEntity().retrieveEntity(whereSql)
        if (existingRow == null)
        {
            AchievementEntity.insertAchievement(ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR, getCurrentPlayerId(), getGameId(), methodStr)
        }
    }

    override fun currentPlayerHasFinished(): Boolean
    {
        val lastDart = dartsThrown.last()
        return currentScore == 0 && lastDart.isDouble()
    }

    override fun updateAchievementsForFinish(playerId: String, finishingPosition: Int, score: Int)
    {
        super.updateAchievementsForFinish(playerId, finishingPosition, score)

        val sum = sumScore(dartsThrown)
        AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_BEST_FINISH, playerId, getGameId(), sum)

        val checkout = dartsThrown.last().score
        insertForCheckoutCompleteness(playerId, getGameId(), checkout)

        if (checkout == 1)
        {
            AchievementEntity.insertAchievement(ACHIEVEMENT_REF_X01_BTBF, getCurrentPlayerId(), getGameId())
        }
    }

    override fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, totalRounds: Int)
    {
        val scorer = hmPlayerNumberToDartsScorer[playerNumber]
        for (i in 1..totalRounds)
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
    }

    private fun suggestCheckout()
    {
        val dartsRemaining = 3 - dartsThrown.size
        val checkout = CheckoutSuggester.suggestCheckout(currentScore, dartsRemaining) ?: return

        checkout.forEach {
            activeScorer!!.addHint(it)
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
            Debug.append("MERCY RULE", VERBOSE_LOGGING)
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

    override fun shouldAnimateMiss(dart: Dart): Boolean
    {
        return !isCheckoutScore(currentScore)
    }
}
