package burlton.dartzee.code.screen.game

import burlton.core.code.obj.HashMapList
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_GOLF_COURSE_MASTER
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_GOLF_POINTS_RISKED
import burlton.dartzee.code.achievements.getWinAchievementRef
import burlton.dartzee.code.achievements.retrieveAchievementForDetail
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.screen.game.scorer.DartsScorerGolf

open class GamePanelGolf(parent: AbstractDartsGameScreen, game: GameEntity) : DartsGamePanel<DartsScorerGolf>(parent, game)
{
    //Number of rounds - 9 holes or 18?
    private var numberOfRounds = -1

    private fun getScoreForMostRecentDart() : Int
    {
        val lastDart = dartsThrown.last()

        val targetHole = currentRoundNumber
        return lastDart.getGolfScore(targetHole)
    }

    override fun initImpl(game: GameEntity)
    {
        //The params tell us how many holes
        numberOfRounds = Integer.parseInt(game.gameParams)
    }

    override fun doAiTurn(model: AbstractDartsModel)
    {
        val targetHole = currentRoundNumber
        val dartNo = dartsThrown.size + 1
        model.throwGolfDart(targetHole, dartNo, dartboard)
    }

    override fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, totalRounds: Int)
    {
        val scorer = hmPlayerNumberToDartsScorer[playerNumber]
        for (i in 1..totalRounds)
        {
            val darts = hmRoundToDarts[i]!!
            scorer?.addDarts(darts)
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
        if (activeScorer!!.getHuman())
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

        activeScorer?.finaliseRoundScore()

        unlockAchievements()

        if (currentRoundNumber == numberOfRounds)
        {
            handlePlayerFinish()
        }

        currentPlayerNumber = getNextPlayerNumber(currentPlayerNumber)
        if (getActiveCount() > 0)
        {
            nextTurn()
        }
        else
        {
            finishGame()
        }
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


    override fun getFinishingPositionFromPlayersRemaining(): Int
    {
        //Finishing positions are determined at the end
        return -1
    }

    private fun finishGame()
    {
        //Get the participants sorted by score so we can assign finishing positions
        setFinishingPositions()

        updateScorersWithFinishingPositions()

        parentWindow?.startNextGameIfNecessary()

        allPlayersFinished()
    }

    private fun setFinishingPositions()
    {
        //If there's only one player, it's already set to -1 which is correct
        if (totalPlayers == 1)
        {
            return
        }

        val participants = hmPlayerNumberToParticipant.values.sortedBy{it.finalScore}

        var previousScore = Integer.MAX_VALUE
        var finishPos = 1
        for (i in participants.indices)
        {
            val pt = participants[i]
            val ptScore = pt.finalScore
            if (ptScore > previousScore)
            {
                finishPos++
            }

            pt.finishingPosition = finishPos
            pt.saveToDatabase()

            if (finishPos == 1)
            {
                val achievementRef = getWinAchievementRef(gameEntity.gameType)
                AchievementEntity.incrementAchievement(achievementRef, pt.playerId, getGameId())
            }

            previousScore = pt.finalScore
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
