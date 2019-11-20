package burlton.dartzee.code.screen.game

import burlton.core.code.obj.HashMapList
import burlton.core.code.util.ceilDiv
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.ai.AbstractDartsModel
import burlton.dartzee.code.dartzee.DartzeeRoundResult
import burlton.dartzee.code.db.DartzeeRoundResultEntity
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.screen.dartzee.DartboardRuleVerifier
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCarousel
import burlton.dartzee.code.screen.dartzee.IDartzeeCarouselHoverListener
import burlton.dartzee.code.screen.dartzee.IDartzeeTileListener
import burlton.dartzee.code.screen.game.scorer.DartsScorerDartzee
import burlton.dartzee.code.utils.factoryHighScoreResult
import java.awt.BorderLayout

class GamePanelDartzee(parent: AbstractDartsGameScreen, game: GameEntity) :
        GamePanelFixedLength<DartsScorerDartzee, DartboardRuleVerifier>(parent, game),
        IDartzeeCarouselHoverListener,
        IDartzeeTileListener
{
    private val dtos = DartzeeRuleEntity().retrieveForGame(game.rowId).map { it.toDto() }
    override val totalRounds = dtos.size + 1

    private val carousel = DartzeeRuleCarousel(this, dtos)

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

            val result = if (i == 1) factoryHighScoreResult(darts) else roundResults.find { it.roundNumber == i }!!.toDto()
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
        updateCarouselAndDartboard()
    }

    override fun shouldStopAfterDartThrown(): Boolean
    {
        val validSegments = carousel.getValidSegments()
        return dartsThrown.size == 3 || validSegments.isEmpty()
    }

    override fun shouldAIStop() = false

    override fun readyForThrow()
    {
        super.readyForThrow()

        updateCarouselAndDartboard()
    }

    private fun updateCarouselAndDartboard()
    {
        val ruleResults = hmPlayerNumberToRoundResults.getOrDefault(currentPlayerNumber, mutableListOf())
        carousel.update(ruleResults, dartsThrown, currentRoundNumber)
        dartboard.refreshValidSegments(carousel.getValidSegments())
    }

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
            val entity = DartzeeRoundResultEntity.factoryAndSave(result, pt, currentRoundNumber)
            hmPlayerNumberToRoundResults.putInList(currentPlayerNumber, entity)
        }

        saveDartsToDatabase()

        finishRound()
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