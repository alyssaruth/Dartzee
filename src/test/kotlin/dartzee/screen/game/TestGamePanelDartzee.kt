package dartzee.screen.game

import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.achievements.AchievementType
import dartzee.achievements.retrieveAchievementForDetail
import dartzee.ai.DartzeePlayStyle
import dartzee.bullseye
import dartzee.core.util.DateStatics
import dartzee.core.util.getAllChildComponentsForType
import dartzee.core.util.getSqlDateNow
import dartzee.dartzee.DartzeeCalculator
import dartzee.dartzee.DartzeeRoundResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.doubleNineteen
import dartzee.doubleTwenty
import dartzee.game.GameType
import dartzee.helper.*
import dartzee.listener.DartboardListener
import dartzee.screen.game.dartzee.*
import dartzee.singleTwenty
import dartzee.utils.InjectedThings
import dartzee.utils.getAllPossibleSegments
import dartzee.utils.insertDartzeeRules
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test
import java.awt.Color

class TestGamePanelDartzee: AbstractTest()
{
    private val rules = listOf(twoBlackOneWhite, innerOuterInner, scoreEighteens, totalIsFifty)
    private val ruleResults = listOf(
        DartzeeRoundResult(2, true, 50),
        DartzeeRoundResult(1, false, -115),
        DartzeeRoundResult(3, true, 18),
        DartzeeRoundResult(4, true, -66),
    )

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
        val game = setUpDartzeeGameOnDatabase(5)

        val summaryPanel = mockk<DartzeeRuleSummaryPanel>(relaxed = true)

        val gamePanel = makeGamePanel(rules, summaryPanel, game)
        gamePanel.loadGame()

        verify { summaryPanel.gameFinished() }
        gamePanel.currentPlayerNumber shouldBe 0
    }

    @Test
    fun `Should load scores and results correctly`()
    {
        val game = setUpDartzeeGameOnDatabase(5)
        val carousel = DartzeeRuleCarousel(rules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(rules, summaryPanel, game)
        gamePanel.loadGame()

        gamePanel.getPlayerState().getScoreSoFar() shouldBe 67

        val tiles = carousel.completeTiles
        tiles[0].dto shouldBe innerOuterInner
        tiles[0].ruleNumber shouldBe 2
        tiles[0].getScoreForHover() shouldBe 50

        tiles[1].dto shouldBe twoBlackOneWhite
        tiles[1].ruleNumber shouldBe 1
        tiles[1].getScoreForHover() shouldBe -115

        tiles[2].dto shouldBe scoreEighteens
        tiles[2].ruleNumber shouldBe 3
        tiles[2].getScoreForHover() shouldBe 18

        tiles[3].dto shouldBe totalIsFifty
        tiles[3].ruleNumber shouldBe 4
        tiles[3].getScoreForHover() shouldBe -66
    }

    @Test
    fun `Should not update best game achievement for too few rules`()
    {
        val shorterRules = rules.subList(0, 2)

        val player = insertPlayer()
        val game = setUpDartzeeGameOnDatabase(2, player)
        val carousel = DartzeeRuleCarousel(shorterRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(shorterRules, summaryPanel, game)
        gamePanel.loadGame()

        gamePanel.getPlayerState().getScoreSoFar() shouldBe 230

        // Finish the game by failing
        gamePanel.dartThrown(makeDart(20, 0, SegmentType.MISS))
        carousel.getDisplayedTiles().first().doClick()

        retrieveAchievementForDetail(AchievementType.DARTZEE_BEST_GAME, player.rowId, "") shouldBe null
    }

    @Test
    fun `Should update best game achievement if there are 5 or more rules`()
    {
        val player = insertPlayer()
        val game = setUpDartzeeGameOnDatabase(4, player)
        val carousel = DartzeeRuleCarousel(rules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(rules, summaryPanel, game)
        gamePanel.loadGame()

        gamePanel.getPlayerState().getScoreSoFar() shouldBe 133

        // Finish the game by failing
        gamePanel.dartThrown(makeDart(20, 0, SegmentType.MISS))
        carousel.getDisplayedTiles().first().doClick()

        val achievement = retrieveAchievementForDetail(AchievementType.DARTZEE_BEST_GAME, player.rowId, "")!!
        achievement.achievementCounter shouldBe 13
        achievement.gameIdEarned shouldBe game.rowId
    }

    @Test
    fun `Should update the carousel and dartboard on readyForThrow and each time a dart is thrown`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val reducedRules = rules.subList(0, 2)
        val game = setUpDartzeeGameOnDatabase(1)
        val carousel = DartzeeRuleCarousel(reducedRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(reducedRules, summaryPanel, game)
        panel.loadGame()

        carousel.completeTiles.shouldBeEmpty()
        carousel.pendingTiles.size shouldBe reducedRules.size

        val expectedSegments = getAllPossibleSegments().filter { !it.isMiss() && !it.isDoubleExcludingBull() }
        panel.dartboard.segmentStatus!!.scoringSegments.shouldContainExactlyInAnyOrder(*expectedSegments.toTypedArray())

        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))

        val twoBlackOneWhiteSegments = twoBlackOneWhite.calculationResult!!.scoringSegments.toTypedArray()
        panel.dartboard.segmentStatus!!.scoringSegments.shouldContainExactlyInAnyOrder(*twoBlackOneWhiteSegments)

        panel.dartThrown(makeDart(20, 0, SegmentType.MISS))
        panel.dartboard.segmentStatus!!.scoringSegments.shouldBeEmpty()

        panel.btnReset.isEnabled = true
        panel.btnReset.doClick()
        panel.dartboard.segmentStatus!!.scoringSegments.shouldContainExactlyInAnyOrder(*expectedSegments.toTypedArray())
    }

    @Test
    fun `Should save darts on confirm pressed, then hide it after the first round`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val game = insertGame(gameType = GameType.DARTZEE)
        val player = insertPlayer(strategy = "")

        val carousel = DartzeeRuleCarousel(rules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(rules, summaryPanel, game)
        panel.startNewGame(listOf(player))

        panel.btnConfirm.isVisible shouldBe true

        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(5, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(1, 1, SegmentType.OUTER_SINGLE))

        panel.btnConfirm.doClick()

        panel.btnConfirm.isVisible shouldBe false
        getCountFromTable("DartzeeRoundResult") shouldBe 0
        panel.getPlayerState().getScoreSoFar() shouldBe 26

        panel.dartThrown(makeDart(20, 1, SegmentType.INNER_SINGLE))
        panel.dartThrown(makeDart(5, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(1, 1, SegmentType.INNER_SINGLE))

        carousel.getDisplayedTiles().first().doClick()

        panel.getPlayerState().getScoreSoFar() shouldBe 52

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
        panel.startNewGame(listOf(insertPlayer(strategy = "")))

        panel.hoverChanged(SegmentStatus(listOf(doubleNineteen), listOf(doubleNineteen)))
        panel.dartboard.segmentStatus shouldBe SegmentStatus(listOf(doubleNineteen), listOf(doubleNineteen))

        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))

        panel.hoverChanged(SegmentStatus(listOf(doubleTwenty), listOf(doubleTwenty)))
        panel.dartboard.segmentStatus shouldBe SegmentStatus(listOf(doubleTwenty), listOf(doubleTwenty))

        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))
        panel.dartboard.segmentStatus shouldBe SegmentStatus(getAllPossibleSegments(), getAllPossibleSegments())
        panel.hoverChanged(SegmentStatus(listOf(bullseye), listOf(bullseye)))
        panel.dartboard.segmentStatus shouldBe SegmentStatus(getAllPossibleSegments(), getAllPossibleSegments())
    }

    @Test
    fun `Should select the right player when a scorer is selected`()
    {
        val summaryPanel = mockk<DartzeeRuleSummaryPanel>(relaxed = true)
        val panel = makeGamePanel(rules, summaryPanel, totalPlayers = 2)
        panel.startNewGame(listOf(insertPlayer(strategy = ""), insertPlayer(strategy = "")))

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

    @Test
    fun `AI should throw scoring darts during the scoring round`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val game = insertGame(gameType = GameType.DARTZEE)
        val player = insertPlayer(strategy = "")

        val carousel = DartzeeRuleCarousel(rules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(rules, summaryPanel, game)
        panel.startNewGame(listOf(player))

        val listener = mockk<DartboardListener>(relaxed = true)
        panel.dartboard.addDartboardListener(listener)

        panel.doAiTurn(beastDartsModel())

        verify { listener.dartThrown(Dart(20, 3)) }
    }

    @Test
    fun `AI should throw based on segment status, and adjust correctly for number of darts thrown`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val game = setUpDartzeeGameOnDatabase(1)

        val carousel = mockk<DartzeeRuleCarousel>(relaxed = true)
        every { carousel.getSegmentStatus() } returns SegmentStatus(listOf(singleTwenty), getAllPossibleSegments())
        every { carousel.initialised } returns true

        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(rules, summaryPanel, game)
        panel.loadGame()

        val listener = mockk<DartboardListener>(relaxed = true)
        panel.dartboard.addDartboardListener(listener)

        val model = beastDartsModel(dartzeePlayStyle = DartzeePlayStyle.CAUTIOUS)
        panel.doAiTurn(model)
        panel.doAiTurn(model)
        panel.doAiTurn(model)

        verifySequence {
            listener.dartThrown(Dart(20, 1))
            listener.dartThrown(Dart(20, 1))
            listener.dartThrown(Dart(25, 2))
        }
    }

    @Test
    fun `AI turn should complete normally, and the highest passed rule should be selected`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val model = beastDartsModel(dartzeePlayStyle = DartzeePlayStyle.CAUTIOUS)

        val player = insertPlayer(model = beastDartsModel())
        val game = setUpDartzeeGameOnDatabase(1, player)

        val carousel = DartzeeRuleCarousel(rules)

        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(rules, summaryPanel, game)
        panel.loadGame()

        panel.doAiTurn(model)
        panel.doAiTurn(model)
        panel.doAiTurn(model)

        panel.saveDartsAndProceed()

        val results = DartzeeRoundResultEntity().retrieveEntities()
        results.size shouldBe 1

        val result = results.first()
        result.ruleNumber shouldBe 3
        result.success shouldBe true

        carousel.getAvailableRuleTiles().size shouldBe 3
        carousel.getAvailableRuleTiles().first().dto shouldBe twoBlackOneWhite
    }

    private fun GamePanelDartzee.getPlayerState() = getPlayerStates().first()

    private fun DartzeeRuleCarousel.getDisplayedTiles() = tilePanel.getAllChildComponentsForType<DartzeeRuleTile>().filter { it.isVisible }

    private fun setUpDartzeeGameOnDatabase(rounds: Int, player: PlayerEntity? = null): GameEntity
    {
        val dtFinish = if (rounds > 4) getSqlDateNow() else DateStatics.END_OF_TIME
        val game = insertGame(gameType = GameType.DARTZEE, dtFinish = dtFinish)

        insertDartzeeRules(game.rowId, rules)

        val participant = insertParticipant(insertPlayer = (player == null), gameId = game.rowId, ordinal = 0, playerId = player?.rowId ?: "")

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

        if (rounds > 3)
        {
            insertDart(participant = participant, roundNumber = 4, ordinal = 1, score = 18, multiplier = 1)
            insertDart(participant = participant, roundNumber = 4, ordinal = 2, score = 4, multiplier = 1)
            insertDart(participant = participant, roundNumber = 4, ordinal = 3, score = 13, multiplier = 1)
            DartzeeRoundResultEntity.factoryAndSave(ruleResults[2], participant, 4)
        }

        if (rounds > 4)
        {
            insertDart(participant = participant, roundNumber = 5, ordinal = 1, score = 20, multiplier = 1)
            insertDart(participant = participant, roundNumber = 5, ordinal = 2, score = 20, multiplier = 0)
            insertDart(participant = participant, roundNumber = 5, ordinal = 3, score = 15, multiplier = 0)
            DartzeeRoundResultEntity.factoryAndSave(ruleResults[3], participant, 5)
        }

        return game
    }

    private fun makeGamePanel(dtos: List<DartzeeRuleDto>,
                              summaryPanel: DartzeeRuleSummaryPanel = mockk(relaxed = true),
                              game: GameEntity = insertGame(),
                              totalPlayers: Int = 1,
                              parentWindow: AbstractDartsGameScreen = mockk(relaxed = true)): GamePanelDartzee
    {
        return GamePanelDartzee(
            parentWindow,
            game,
            totalPlayers,
            dtos,
            summaryPanel
        )
    }
}