package burlton.dartzee.test.screen.game

import burlton.dartzee.code.dartzee.DartzeeRoundResult
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.db.DartzeeRoundResultEntity
import burlton.dartzee.code.db.GAME_TYPE_DARTZEE
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCarousel
import burlton.dartzee.code.screen.dartzee.DartzeeRuleSummaryPanel
import burlton.dartzee.code.screen.game.GamePanelDartzee
import burlton.dartzee.test.helper.*
import burlton.desktopcore.code.util.getSqlDateNow
import io.kotlintest.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestGamePanelDartzee: AbstractDartsTest()
{
    private val rules = listOf(scoreEighteens, totalIsFifty)
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
        val game = setUpDartzeeGameOnDatabase()

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
        val game = setUpDartzeeGameOnDatabase()
        val carousel = DartzeeRuleCarousel(rules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(rules, summaryPanel, game)
        gamePanel.initBasic(1)
        gamePanel.loadGame()

        gamePanel.scorersOrdered.first().getTotalScore() shouldBe 115

        val tiles = carousel.completeTiles
        tiles[0].dto shouldBe totalIsFifty
        tiles[0].ruleNumber shouldBe 2
        tiles[0].getScoreForHover() shouldBe 50

        tiles[1].dto shouldBe scoreEighteens
        tiles[1].ruleNumber shouldBe 1
        tiles[1].getScoreForHover() shouldBe -115
    }

    private fun setUpDartzeeGameOnDatabase(): GameEntity
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

        insertDart(participant = participant, roundNumber = 2, ordinal = 1, score = 18, multiplier = 1)
        insertDart(participant = participant, roundNumber = 2, ordinal = 2, score = 12, multiplier = 1)
        insertDart(participant = participant, roundNumber = 2, ordinal = 3, score = 20, multiplier = 1)

        insertDart(participant = participant, roundNumber = 3, ordinal = 1, score = 20, multiplier = 0)

        ruleResults.forEachIndexed { ix, it ->
            DartzeeRoundResultEntity.factoryAndSave(it, participant, ix + 2)
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