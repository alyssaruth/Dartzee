package burlton.dartzee.test.screen.game

import burlton.dartzee.code.dartzee.DartzeeRoundResult
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.db.DartzeeRoundResultEntity
import burlton.dartzee.code.db.GAME_TYPE_DARTZEE
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCarousel
import burlton.dartzee.code.screen.dartzee.DartzeeRuleSummaryPanel
import burlton.dartzee.code.screen.game.GamePanelDartzee
import burlton.dartzee.code.screen.game.scorer.DartsScorerDartzee
import burlton.dartzee.code.utils.getAllPossibleSegments
import burlton.dartzee.test.helper.*
import burlton.desktopcore.code.util.getSqlDateNow
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestGamePanelDartzee: AbstractDartsTest()
{
    private val rules = listOf(twoBlackOneWhite, innerOuterInner)
    private val ruleResults = listOf(DartzeeRoundResult(2, true, 50), DartzeeRoundResult(1, false, -115))

    @Test
    fun `Should initialise totalRounds based on the number of rules`()
    {
        makeGamePanel(listOf(makeDartzeeRuleDto())).totalRounds shouldBe 2
        makeGamePanel(listOf(makeDartzeeRuleDto(), makeDartzeeRuleDto())).totalRounds shouldBe 3
    }

    @Test
    fun `Should register itself as a listener on the carousel`()
    {
        val carousel = DartzeeRuleCarousel(rules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(rules, summaryPanel)
        carousel.listener shouldBe gamePanel
    }

    @Test
    fun `Should tell the summaryPanel to finish and select the first player when loading a finished game`()
    {
        val game = setUpDartzeeGameOnDatabase(3)

        val summaryPanel = mockk<DartzeeRuleSummaryPanel>(relaxed = true)

        val gamePanel = makeGamePanel(rules, summaryPanel, game)
        gamePanel.initBasic(1)
        gamePanel.loadGame()

        verify { summaryPanel.gameFinished() }
        gamePanel.currentPlayerNumber shouldBe 0
    }

    @Test
    fun `Should load scores and results correctly`()
    {
        val game = setUpDartzeeGameOnDatabase(3)
        val carousel = DartzeeRuleCarousel(rules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(rules, summaryPanel, game)
        gamePanel.initBasic(1)
        gamePanel.loadGame()

        gamePanel.scorersOrdered.first().getTotalScore() shouldBe 115

        val tiles = carousel.completeTiles
        tiles[0].dto shouldBe innerOuterInner
        tiles[0].ruleNumber shouldBe 2
        tiles[0].getScoreForHover() shouldBe 50

        tiles[1].dto shouldBe twoBlackOneWhite
        tiles[1].ruleNumber shouldBe 1
        tiles[1].getScoreForHover() shouldBe -115
    }

    @Test
    fun `Should set lastScore to be the total score on the scorer when starting a new round`()
    {
        val panel = makeGamePanel(rules)

        val scorer = mockk<DartsScorerDartzee>()
        every { scorer.getTotalScore() } returns 35

        panel.activeScorer = scorer
        panel.updateVariablesForNewRound()
        panel.lastRoundScore shouldBe 35
    }

    @Test
    fun `Should update the carousel and dartboard on readyForThrow and each time a dart is thrown`()
    {
        val game = setUpDartzeeGameOnDatabase(1)
        val carousel = DartzeeRuleCarousel(rules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(rules, summaryPanel, game)
        panel.initBasic(1)
        panel.loadGame()

        val expectedSegments = getAllPossibleSegments().filter { it.getMultiplier() == 1 || it.getMultiplier() == 3 }.filterNot { it.score == 25 }
        panel.dartboard.validSegments.size shouldBe expectedSegments.size
        panel.dartboard.validSegments.shouldContainExactlyInAnyOrder(*expectedSegments.toTypedArray())

    }

    private fun setUpDartzeeGameOnDatabase(rounds: Int): GameEntity
    {
        val game = insertGame(gameType = GAME_TYPE_DARTZEE, dtFinish = getSqlDateNow())

        rules.forEachIndexed { ix, it ->
            val entity = it.toEntity(ix, "Game", game.rowId)
            entity.saveToDatabase()
        }

        val participant = insertParticipant(insertPlayer = true, gameId = game.rowId, ordinal = 0)

        insertDart(participant = participant, roundNumber = 1, ordinal = 1)
        insertDart(participant = participant, roundNumber = 1, ordinal = 2)
        insertDart(participant = participant, roundNumber = 1, ordinal = 3)

        if (rounds > 1)
        {
            insertDart(participant = participant, roundNumber = 2, ordinal = 1, score = 18, multiplier = 1)
            insertDart(participant = participant, roundNumber = 2, ordinal = 2, score = 12, multiplier = 1)
            insertDart(participant = participant, roundNumber = 2, ordinal = 3, score = 20, multiplier = 1)

            DartzeeRoundResultEntity.factoryAndSave(ruleResults[0], participant, 2)
        }

        if (rounds > 2)
        {
            insertDart(participant = participant, roundNumber = 3, ordinal = 1, score = 20, multiplier = 0)
            DartzeeRoundResultEntity.factoryAndSave(ruleResults[1], participant, 3)
        }

        return game
    }

    private fun makeGamePanel(dtos: List<DartzeeRuleDto>,
                              summaryPanel: DartzeeRuleSummaryPanel = mockk(relaxed = true),
                              game: GameEntity = insertGame()): GamePanelDartzee
    {
        return GamePanelDartzee(mockk(relaxed = true), game, dtos, summaryPanel)
    }
}