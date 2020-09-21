package dartzee.screen.game.x01

import dartzee.`object`.Dart
import dartzee.achievements.*
import dartzee.ai.DartsAiModel
import dartzee.core.obj.HashMapList
import dartzee.core.util.doBadLuck
import dartzee.core.util.doFawlty
import dartzee.core.util.playDodgySound
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.X01FinishEntity
import dartzee.game.state.X01PlayerState
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.GamePanelPausable
import dartzee.screen.game.scorer.DartsScorerX01
import dartzee.utils.*

open class GamePanelX01(parent: AbstractDartsGameScreen, game: GameEntity, totalPlayers: Int) : GamePanelPausable<DartsScorerX01, X01PlayerState>(parent, game, totalPlayers)
{
    private val startingScore = Integer.parseInt(game.gameParams)

    //Transient variables for each round
    private var currentScore = -1

    override fun factoryState(pt: ParticipantEntity) = X01PlayerState(pt)

    override fun updateVariablesForNewRound()
    {
        resetRoundVariables()
    }

    override fun resetRoundVariables()
    {
        currentScore = getCurrentPlayerState().getRemainingScoreForRound(startingScore, currentRoundNumber - 1)
    }

    override fun saveDartsAndProceed()
    {
        //Finalise the scorer
        val lastDart = getDartsThrown().last()
        val bust = isBust(lastDart)

        val count = getCurrentPlayerState().getBadLuckCount()
        if (count > 0)
        {
            AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_SUCH_BAD_LUCK, getCurrentPlayerId(), getGameId(), count)
        }

        val startingScoreForRound = getCurrentPlayerState().getRemainingScore(startingScore)
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
            AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_HIGHEST_BUST, getCurrentPlayerId(), getGameId(), startingScoreForRound)
        }

        super.saveDartsAndProceed()
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

    override fun currentPlayerHasFinished() = getCurrentPlayerState().getRemainingScore(startingScore) == 0

    override fun updateAchievementsForFinish(playerId: String, finishingPosition: Int, score: Int)
    {
        super.updateAchievementsForFinish(playerId, finishingPosition, score)

        val finalRound = getCurrentPlayerState().getLastRound()

        val sum = sumScore(finalRound)
        AchievementEntity.updateAchievement(ACHIEVEMENT_REF_X01_BEST_FINISH, playerId, getGameId(), sum)

        //Insert into the X01Finishes table for the leaderboard
        X01FinishEntity.factoryAndSave(playerId, getGameId(), sum)

        val checkout = finalRound.last().score
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

    override fun shouldStopAfterDartThrown() = dartsThrownCount() == 3 || currentScore <= 1

    override fun doAiTurn(model: DartsAiModel)
    {
        val startOfRoundScore = getCurrentPlayerState().getRemainingScore(startingScore)
        if (shouldStopForMercyRule(model, startOfRoundScore, currentScore))
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
