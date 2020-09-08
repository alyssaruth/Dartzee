package dartzee.screen.game.golf

import dartzee.`object`.Dart
import dartzee.achievements.ACHIEVEMENT_REF_GOLF_COURSE_MASTER
import dartzee.achievements.ACHIEVEMENT_REF_GOLF_POINTS_RISKED
import dartzee.achievements.retrieveAchievementForDetail
import dartzee.ai.DartsAiModel
import dartzee.core.obj.HashMapList
import dartzee.core.util.doGolfMiss
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.game.state.DefaultPlayerState
import dartzee.screen.Dartboard
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.GamePanelFixedLength
import dartzee.screen.game.scorer.DartsScorerGolf

open class GamePanelGolf(parent: AbstractDartsGameScreen, game: GameEntity, totalPlayers: Int) :
        GamePanelFixedLength<DartsScorerGolf, Dartboard, DefaultPlayerState<DartsScorerGolf>>(parent, game, totalPlayers)
{
    //Number of rounds - 9 holes or 18?
    override val totalRounds = Integer.parseInt(game.gameParams)

    override fun factoryDartboard() = Dartboard()
    override fun factoryState(pt: ParticipantEntity, scorer: DartsScorerGolf) = DefaultPlayerState(pt, scorer)

    private fun getScoreForMostRecentDart() : Int
    {
        val lastDart = getDartsThrown().last()

        val targetHole = currentRoundNumber
        return lastDart.getGolfScore(targetHole)
    }

    override fun doAiTurn(model: DartsAiModel)
    {
        val targetHole = currentRoundNumber
        val dartNo = dartsThrownCount() + 1
        model.throwGolfDart(targetHole, dartNo, dartboard)
    }

    override fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, totalRounds: Int)
    {
        val scorer = getScorer(playerNumber)
        for (i in 1..totalRounds)
        {
            val darts = hmRoundToDarts[i]!!
            scorer.addDarts(darts)
        }
    }

    override fun updateVariablesForNewRound() {}
    override fun resetRoundVariables() {}
    override fun updateVariablesForDartThrown(dart: Dart) {}

    override fun shouldStopAfterDartThrown(): Boolean
    {
        val dartsThrownCount = dartsThrownCount()
        if (dartsThrownCount == 3)
        {
            return true
        }

        val score = getScoreForMostRecentDart()
        if (activeScorer.human)
        {
            return score == 1
        }
        else
        {
            val model = getCurrentPlayerStrategy()
            val stopThreshold = model.getStopThresholdForDartNo(dartsThrownCount)

            return score <= stopThreshold
        }
    }

    override fun saveDartsAndProceed()
    {
        unlockAchievements()
        saveDartsToDatabase()

        activeScorer.finaliseRoundScore()

        finishRound()
    }

    fun unlockAchievements()
    {
        val lastDart = getDartsThrown().last()
        val dartsRisked = getDartsThrown() - lastDart
        val pointsRisked = dartsRisked.map{ 5 - it.getGolfScore(currentRoundNumber) }.sum()

        if (pointsRisked > 0)
        {
            AchievementEntity.incrementAchievement(ACHIEVEMENT_REF_GOLF_POINTS_RISKED, getCurrentPlayerId(), gameEntity.rowId, pointsRisked)
        }

        if (lastDart.getGolfScore(currentRoundNumber) == 1
         && retrieveAchievementForDetail(ACHIEVEMENT_REF_GOLF_COURSE_MASTER, getCurrentPlayerId(), "$currentRoundNumber") == null)
        {
            AchievementEntity.insertAchievement(ACHIEVEMENT_REF_GOLF_COURSE_MASTER, getCurrentPlayerId(), getGameId(), "$currentRoundNumber")
        }
    }

    override fun factoryScorer() = DartsScorerGolf()

    override fun shouldAIStop() = false

    override fun doMissAnimation()
    {
        dartboard.doGolfMiss()
    }

    override fun factoryStatsPanel(gameParams: String) = GameStatisticsPanelGolf()
}
