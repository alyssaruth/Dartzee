package dartzee.screen.game

import dartzee.`object`.SEGMENT_TYPE_INNER_SINGLE
import dartzee.`object`.SEGMENT_TYPE_MISS
import dartzee.`object`.SEGMENT_TYPE_OUTER_SINGLE
import dartzee.bullseye
import dartzee.core.util.DateStatics
import dartzee.core.util.getAllChildComponentsForType
import dartzee.core.util.getSqlDateNow
import dartzee.dartzee.DartzeeCalculator
import dartzee.dartzee.DartzeeRoundResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.GAME_TYPE_DARTZEE
import dartzee.db.GameEntity
import dartzee.doubleNineteen
import dartzee.doubleTwenty
import dartzee.helper.*
import dartzee.screen.game.dartzee.DartzeeRuleCarousel
import dartzee.screen.game.dartzee.DartzeeRuleSummaryPanel
import dartzee.screen.game.dartzee.DartzeeRuleTile
import dartzee.screen.game.dartzee.GamePanelDartzee
import dartzee.screen.game.scorer.DartsScorerDartzee
import dartzee.utils.InjectedThings
import dartzee.utils.getAllPossibleSegments
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.awt.Color

class TestGamePanelDartzee: AbstractTest()
{
    private val rules = listOf(twoBlackOneWhite, innerOuterInner)
    private val ruleResults = listOf(DartzeeRoundResult(2, true, 50), DartzeeRoundResult(1, false, -115))

    override fun afterEachTest()
    {
        super.afterEachTest()
        InjectedThings.dartzeeCalculator = FakeDartzeeCalculator()
    }

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
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val game = setUpDartzeeGameOnDatabase(1)
        val carousel = DartzeeRuleCarousel(rules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(rules, summaryPanel, game)
        panel.initBasic(1)
        panel.loadGame()

        carousel.completeTiles.shouldBeEmpty()
        carousel.pendingTiles.size shouldBe 2

        val expectedSegments = getAllPossibleSegments().filter { !it.isMiss() && !it.isDoubleExcludingBull() }
        panel.dartboard.validSegments.shouldContainExactlyInAnyOrder(*expectedSegments.toTypedArray())

        panel.dartThrown(makeDart(20, 1, SEGMENT_TYPE_OUTER_SINGLE))

        val twoBlackOneWhiteSegments = twoBlackOneWhite.calculationResult!!.validSegments.toTypedArray()
        panel.dartboard.validSegments.shouldContainExactlyInAnyOrder(*twoBlackOneWhiteSegments)

        panel.dartThrown(makeDart(20, 0, SEGMENT_TYPE_MISS))
        panel.dartboard.validSegments.shouldBeEmpty()

        panel.btnReset.isEnabled = true
        panel.btnReset.doClick()
        panel.dartboard.validSegments.shouldContainExactlyInAnyOrder(*expectedSegments.toTypedArray())
    }

    @Test
    fun `Should save darts on confirm pressed, then hide it after the first round`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val game = insertGame(gameType = GAME_TYPE_DARTZEE)
        val player = insertPlayer(strategy = -1)

        val carousel = DartzeeRuleCarousel(rules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(rules, summaryPanel, game)
        panel.initBasic(1)
        panel.startNewGame(listOf(player))

        panel.btnConfirm.isVisible shouldBe true

        panel.dartThrown(makeDart(20, 1, SEGMENT_TYPE_OUTER_SINGLE))
        panel.dartThrown(makeDart(5, 1, SEGMENT_TYPE_OUTER_SINGLE))
        panel.dartThrown(makeDart(1, 1, SEGMENT_TYPE_OUTER_SINGLE))

        panel.btnConfirm.doClick()

        panel.btnConfirm.isVisible shouldBe false
        getCountFromTable("DartzeeRoundResult") shouldBe 0
        panel.activeScorer.getTotalScore() shouldBe 26

        panel.dartThrown(makeDart(20, 1, SEGMENT_TYPE_INNER_SINGLE))
        panel.dartThrown(makeDart(5, 1, SEGMENT_TYPE_OUTER_SINGLE))
        panel.dartThrown(makeDart(1, 1, SEGMENT_TYPE_INNER_SINGLE))

        carousel.getDisplayedTiles().first().doClick()

        panel.activeScorer.getTotalScore() shouldBe 52

        val rr = DartzeeRoundResultEntity().retrieveEntities().first()
        rr.score shouldBe 26
        rr.roundNumber shouldBe 2
        rr.playerId shouldBe player.rowId
        rr.success shouldBe true
        rr.ruleNumber shouldBe 2
    }

    @Test
    fun `Should update valid segments on hover changed if fewer than 3 darts thrown`()
    {
        val carousel = DartzeeRuleCarousel(rules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(rules, summaryPanel)
        panel.initBasic(1)
        panel.startNewGame(listOf(insertPlayer(strategy = -1)))

        panel.hoverChanged(listOf(doubleNineteen))
        panel.dartboard.validSegments.shouldContainExactly(doubleNineteen)

        panel.dartThrown(makeDart(20, 1, SEGMENT_TYPE_OUTER_SINGLE))
        panel.dartThrown(makeDart(20, 1, SEGMENT_TYPE_OUTER_SINGLE))

        panel.hoverChanged(listOf(doubleTwenty))
        panel.dartboard.validSegments.shouldContainExactly(doubleTwenty)

        panel.dartThrown(makeDart(20, 1, SEGMENT_TYPE_OUTER_SINGLE))
        panel.dartboard.validSegments.shouldContainExactlyInAnyOrder(*getAllPossibleSegments().toTypedArray())
        panel.hoverChanged(listOf(bullseye))
        panel.dartboard.validSegments.shouldContainExactlyInAnyOrder(*getAllPossibleSegments().toTypedArray()) //Should not have changed
    }

    @Test
    fun `Should select the right player when a scorer is selected`()
    {
        val summaryPanel = mockk<DartzeeRuleSummaryPanel>(relaxed = true)
        val panel = makeGamePanel(rules, summaryPanel)
        panel.initBasic(2)
        panel.startNewGame(listOf(insertPlayer(strategy = -1), insertPlayer(strategy = -1)))

        panel.scorerSelected(panel.scorersOrdered[0])
        panel.currentPlayerNumber shouldBe 0
        panel.scorersOrdered[0].lblName.foreground shouldBe Color.RED
        panel.scorersOrdered[1].lblName.foreground shouldBe Color.BLACK
        verify { summaryPanel.update(listOf(), listOf(), 0, 1) }

        clearAllMocks()

        panel.scorerSelected(panel.scorersOrdered[1])
        panel.currentPlayerNumber shouldBe 1
        panel.scorersOrdered[0].lblName.foreground shouldBe Color.BLACK
        panel.scorersOrdered[1].lblName.foreground shouldBe Color.RED
        verify { summaryPanel.update(listOf(), listOf(), 0, 1) }
    }

    private fun DartzeeRuleCarousel.getDisplayedTiles() = getAllChildComponentsForType(tilePanel, DartzeeRuleTile::class.java).filter { it.isVisible }

    private fun setUpDartzeeGameOnDatabase(rounds: Int): GameEntity
    {
        val dtFinish = if (rounds > 2) getSqlDateNow() else DateStatics.END_OF_TIME
        val game = insertGame(gameType = GAME_TYPE_DARTZEE, dtFinish = dtFinish)

        rules.forEachIndexed { ix, it ->
            val entity = it.toEntity(ix, "Game", game.rowId)
            entity.saveToDatabase()
        }

        val participant = insertParticipant(insertPlayer = true, gameId = game.rowId, ordinal = 0)

        if (rounds > 0)
        {
            insertDart(participant = participant, roundNumber = 1, ordinal = 1)
            insertDart(participant = participant, roundNumber = 1, ordinal = 2)
            insertDart(participant = participant, roundNumber = 1, ordinal = 3)
        }

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
        return GamePanelDartzee(
            mockk(relaxed = true),
            game,
            dtos,
            summaryPanel
        )
    }
}