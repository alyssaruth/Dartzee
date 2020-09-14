package dartzee.screen.game.x01

import dartzee.`object`.CheckoutSuggester
import dartzee.`object`.Dart
import dartzee.achievements.*
import dartzee.ai.DartsAiModel
import dartzee.core.obj.HashMapCount
import dartzee.core.obj.HashMapList
import dartzee.core.util.doBadLuck
import dartzee.core.util.doFawlty
import dartzee.core.util.playDodgySound
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.X01FinishEntity
import dartzee.game.state.DefaultPlayerState
import dartzee.game.state.X01PlayerState
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.GamePanelPausable
import dartzee.screen.game.scorer.DartsScorerX01
import dartzee.utils.*

open class GamePanelX01(parent: AbstractDartsGameScreen, game: GameEntity, totalPlayers: Int) : GamePanelPausable<DartsScorerX01, X01PlayerState>(parent, game, totalPlayers)
{
    //Transient variables for each round
    private var startingScore = -1
    private var currentScore = -1

    private val hmPlayerNumberToBadLuckCount = HashMapCount<Int>()

    override fun factoryState(pt: ParticipantEntity) = X01PlayerState(pt)

    override fun updateVariablesForNewRound()
    {
        startingScore = activeScorer.getLatestScoreRemaining()
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
        val lastDart = getDartsThrown().last()
        val bust = isBust(currentScore, lastDart)

        updateNearMisses(getDartsThrown(), currentPlayerNumber)

        val count = hmPlayerNumberToBadLuckCount.getCount(currentPlayerNumber)
        if (count > 0)
        {
            AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_SUCH_BAD_LUCK, getCurrentPlayerId(), getGameId(), count)
        }

        if (!bust)
        {
            val totalScore = sumScore(getDartsThrown())
            if (totalScore == 26)
            {
                dartboard.doFawlty()

                updateHotelInspector()
            }

            if (isShanghai(getDartsThrown()))
            {
                AchievementEntity.insertAchievement(ACHIEVEMENT_REF_X01_SHANGHAI, getCurrentPlayerId(), getGameId())
            }

            dartboard.playDodgySound("" + totalScore)

            val total = sumScore(getDartsThrown())
            AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_BEST_THREE_DART_SCORE, getCurrentPlayerId(), getGameId(), total)
        }
        else
        {
            val total = startingScore
            AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_HIGHEST_BUST, getCurrentPlayerId(), getGameId(), total)
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
        if (getDartsThrown().any { d -> d.multiplier == 0 }
          || dartsThrownCount() < 3)
        {
            return
        }

        val methodStr = getSortedDartStr(getDartsThrown())
        val existingRow = retrieveAchievementForDetail(ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR, getCurrentPlayerId(), methodStr)
        if (existingRow == null)
        {
            AchievementEntity.insertAchievement(ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR, getCurrentPlayerId(), getGameId(), methodStr)
        }
    }

    override fun currentPlayerHasFinished(): Boolean
    {
        val lastDart = getDartsThrown().last()
        return currentScore == 0 && lastDart.isDouble()
    }

    override fun updateAchievementsForFinish(playerId: String, finishingPosition: Int, score: Int)
    {
        super.updateAchievementsForFinish(playerId, finishingPosition, score)

        val sum = sumScore(getDartsThrown())
        AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_BEST_FINISH, playerId, getGameId(), sum)

        //Insert into the X01Finishes table for the leaderboard
        X01FinishEntity.factoryAndSave(playerId, getGameId(), sum)

        val checkout = getDartsThrown().last().score
        insertForCheckoutCompleteness(playerId, getGameId(), checkout)

        if (sum in listOf(3, 5, 7, 9))
        {
            AchievementEntity.insertAchievement(ACHIEVEMENT_REF_X01_NO_MERCY, playerId, getGameId(), "$sum")
        }

        if (checkout == 1)
        {
            AchievementEntity.insertAchievement(ACHIEVEMENT_REF_X01_BTBF, getCurrentPlayerId(), getGameId())
        }
    }

    override fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, totalRounds: Int)
    {
        val scorer = getScorer(playerNumber)
        for (i in 1..totalRounds)
        {
            val darts = hmRoundToDarts[i]!!
            addDartsToScorer(darts, scorer)

            updateNearMisses(darts, playerNumber)
        }

        val pt = getParticipant(playerNumber)
        val finishPos = pt.finishingPosition
        if (finishPos > -1)
        {
            scorer.finalisePlayerResult(finishPos)
        }
    }

    private fun addDartsToScorer(darts: MutableList<Dart>, scorer: DartsScorerX01)
    {
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
        val dartsRemaining = 3 - dartsThrownCount()
        val checkout = CheckoutSuggester.suggestCheckout(currentScore, dartsRemaining) ?: return

        checkout.forEach {
            activeScorer.addHint(it)
        }
    }

    override fun shouldStopAfterDartThrown() = dartsThrownCount() == 3 || currentScore <= 1

    override fun doAiTurn(model: DartsAiModel)
    {
        if (shouldStopForMercyRule(model, startingScore, currentScore))
        {
            stopThrowing()
        }
        else
        {
            model.throwX01Dart(currentScore, dartboard)
        }

    }

    override fun factoryScorer() = DartsScorerX01(this, gameEntity.gameParams)

    override fun factoryStatsPanel(gameParams: String) = GameStatisticsPanelX01(gameParams)

    override fun shouldAnimateMiss(dart: Dart): Boolean
    {
        return !isCheckoutScore(currentScore)
    }
}
