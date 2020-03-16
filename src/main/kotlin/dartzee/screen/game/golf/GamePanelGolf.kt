package dartzee.screen.game.golf

import dartzee.`object`.Dart
import dartzee.achievements.ACHIEVEMENT_REF_GOLF_COURSE_MASTER
import dartzee.achievements.ACHIEVEMENT_REF_GOLF_POINTS_RISKED
import dartzee.achievements.retrieveAchievementForDetail
import dartzee.ai.AbstractDartsModel
import dartzee.core.obj.HashMapList
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.screen.Dartboard
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.GamePanelFixedLength
import dartzee.screen.game.GameStatisticsPanel
import dartzee.screen.game.scorer.DartsScorerGolf

open class GamePanelGolf(parent: AbstractDartsGameScreen, game: GameEntity) : GamePanelFixedLength<DartsScorerGolf, Dartboard>(parent, game)
{
    //Number of rounds - 9 holes or 18?
    override val totalRounds = Integer.parseInt(game.gameParams)

    override fun factoryDartboard() = Dartboard()

    private fun getScoreForMostRecentDart() : Int
    {
        val lastDart = dartsThrown.last()

        val targetHole = currentRoundNumber
        return lastDart.getGolfScore(targetHole)
    }

    override fun doAiTurn(model: AbstractDartsModel)
    {
        val targetHole = currentRoundNumber
        val dartNo = dartsThrown.size + 1
        model.throwGolfDart(targetHole, dartNo, dartboard)
    }

    override fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, totalRounds: Int)
    {
        val scorer = getScorer(playerNumber)
        for (i in 1..totalRounds)
        {
            val darts = hmRoundToDarts[i]!!
            darts.forEach { it.setGolfHole(i) }
            scorer.addDarts(darts)
        }
    }

    override fun updateVariablesForNewRound() {}
    override fun resetRoundVariables() {}
    override fun updateVariablesForDartThrown(dart: Dart) {}

    override fun shouldStopAfterDartThrown(): Boolean
    {
        if (dartsThrown.size == 3)
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
            val noDarts = dartsThrown.size

            val model = getCurrentPlayerStrategy()
            val stopThreshold = model!!.getStopThresholdForDartNo(noDarts)

            return score <= stopThreshold
        }
    }

    override fun saveDartsAndProceed()
    {
        saveDartsToDatabase()

        activeScorer.finaliseRoundScore()

        unlockAchievements()

        finishRound()
    }

    fun unlockAchievements()
    {
        val dartsRisked = dartsThrown - dartsThrown.last()
        val pointsRisked = dartsRisked.map{ 5 - it.getGolfScore(currentRoundNumber) }.sum()

        if (pointsRisked > 0)
        {
            AchievementEntity.incrementAchievement(ACHIEVEMENT_REF_GOLF_POINTS_RISKED, getCurrentPlayerId(), gameEntity.rowId, pointsRisked)
        }

        val lastDart = dartsThrown.last()
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

    override fun factoryStatsPanel(): GameStatisticsPanel
    {
        return GameStatisticsPanelGolf()
    }
}
