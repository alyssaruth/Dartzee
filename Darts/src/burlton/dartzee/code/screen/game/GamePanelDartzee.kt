package burlton.dartzee.code.screen.game

import burlton.core.code.obj.HashMapList
import burlton.core.code.util.ceilDiv
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.achievements.getWinAchievementRef
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.dartzee.DartzeeRoundResult
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.DartzeeRoundResultEntity
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.screen.dartzee.DartboardRuleVerifier
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCarousel
import burlton.dartzee.code.screen.dartzee.IDartzeeCarouselHoverListener
import burlton.dartzee.code.screen.dartzee.IDartzeeTileListener
import burlton.dartzee.code.screen.game.scorer.DartsScorerDartzee
import burlton.dartzee.code.utils.sumScore
import java.awt.BorderLayout

class GamePanelDartzee(parent: AbstractDartsGameScreen, game: GameEntity) :
        DartsGamePanel<DartsScorerDartzee, DartboardRuleVerifier>(parent, game),
        IDartzeeCarouselHoverListener,
        IDartzeeTileListener
{
    val dtos = DartzeeRuleEntity().retrieveForGame(game.rowId).map { it.toDto() }
    val totalRounds = dtos.size + 1

    val carousel = DartzeeRuleCarousel(this, dtos)

    //Transient things
    private var lastRoundScore = -1
    private val hmPlayerNumberToRoundResults = HashMapList<Int, DartzeeRoundResultEntity>()

    init
    {
        add(carousel, BorderLayout.NORTH)
    }

    override fun factoryDartboard() = DartboardRuleVerifier()

    override fun doAiTurn(model: AbstractDartsModel)
    {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, totalRounds: Int)
    {
        val pt = hmPlayerNumberToParticipant[playerNumber]!!

        val roundResults = DartzeeRoundResultEntity().retrieveEntities("PlayerId = ${pt.playerId} AND ParticipantId = ${pt.rowId}")
        hmPlayerNumberToRoundResults[playerNumber] = roundResults

        val scorer = hmPlayerNumberToDartsScorer[playerNumber]!!
        for (i in 1..totalRounds)
        {
            val darts = hmRoundToDarts[i]!!
            darts.forEach { scorer.addDart(it) }

            val result = if (i == 1) DartzeeRoundResult(0, true, false, sumScore(darts)) else roundResults.find { it.roundNumber == i }!!.toDto()
            scorer.setResult(result)
        }
    }

    override fun updateVariablesForNewRound()
    {
        lastRoundScore = activeScorer.getTotalScore()
    }

    override fun resetRoundVariables() {}

    override fun updateVariablesForDartThrown(dart: Dart)
    {
        val ruleResults = hmPlayerNumberToRoundResults[currentPlayerNumber]!!
        carousel.update(ruleResults, dartsThrown)
        dartboard.refreshValidSegments(carousel.getValidSegments())
    }

    override fun shouldStopAfterDartThrown(): Boolean
    {
        val validSegments = carousel.getValidSegments()
        return dartsThrown.size == 3 || validSegments.isEmpty()
    }

    override fun shouldAIStop() = false

    override fun saveDartsAndProceed()
    {
        val result = carousel.getRoundResult()
        activeScorer.setResult(result)

        if (!result.userInputNeeded)
        {
            completeRound(result)
        }
        else
        {
            disableInputButtons()
            carousel.addTileListener(this)
        }
    }

    private fun completeRound(result: DartzeeRoundResult)
    {
        val roundScore = if (result.success) lastRoundScore + result.successScore else lastRoundScore.ceilDiv(2)

        val pt = hmPlayerNumberToParticipant[currentPlayerNumber]!!

        activeScorer.setResult(result, roundScore)
        if (currentRoundNumber > 1)
        {
            DartzeeRoundResultEntity.factoryAndSave(result, pt, currentRoundNumber)
        }
        else
        {
            carousel.highScoreRoundComplete()
        }

        saveDartsToDatabase()

        if (currentRoundNumber == totalRounds)
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

    private fun finishGame()
    {
        //Get the participants sorted by score so we can assign finishing positions
        setFinishingPositions()

        updateScorersWithFinishingPositions()

        allPlayersFinished()

        parentWindow?.startNextGameIfNecessary()
    }

    private fun setFinishingPositions()
    {
        //If there's only one player, it's already set to -1 which is correct
        if (totalPlayers == 1)
        {
            return
        }

        val participants = hmPlayerNumberToParticipant.values.sortedBy{ it.finalScore }

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

    override fun initImpl(game: GameEntity)
    {

    }

    override fun factoryStatsPanel(): GameStatisticsPanel? = null
    override fun factoryScorer() = DartsScorerDartzee()

    override fun hoverChanged(validSegments: List<DartboardSegment>)
    {
        if (dartsThrown.size < 3)
        {
            dartboard.refreshValidSegments(validSegments)
        }
    }

    override fun tilePressed(dartzeeRoundResult: DartzeeRoundResult)
    {
        carousel.clearTileListener()
        completeRound(dartzeeRoundResult)
    }
}