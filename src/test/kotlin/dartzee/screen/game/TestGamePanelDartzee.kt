package dartzee.screen.game

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
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
import dartzee.db.AchievementEntity
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.DartzeeTemplateEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.doubleNineteen
import dartzee.doubleTwenty
import dartzee.game.GameType
import dartzee.game.loadParticipants
import dartzee.helper.AbstractTest
import dartzee.helper.AchievementSummary
import dartzee.helper.beastDartsModel
import dartzee.helper.getAchievementCount
import dartzee.helper.getAchievementRows
import dartzee.helper.getCountFromTable
import dartzee.helper.innerOuterInner
import dartzee.helper.insertDart
import dartzee.helper.insertDartzeeTemplate
import dartzee.helper.insertGame
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.makeDart
import dartzee.helper.makeDartzeeRuleDto
import dartzee.helper.preparePlayers
import dartzee.helper.retrieveAchievementsForPlayer
import dartzee.helper.scoreEighteens
import dartzee.helper.testRules
import dartzee.helper.totalIsFifty
import dartzee.helper.twoBlackOneWhite
import dartzee.listener.DartboardListener
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.screen.game.dartzee.DartzeeRuleCarousel
import dartzee.screen.game.dartzee.DartzeeRuleSummaryPanel
import dartzee.screen.game.dartzee.DartzeeRuleTile
import dartzee.screen.game.dartzee.GamePanelDartzee
import dartzee.segmentStatuses
import dartzee.singleTwenty
import dartzee.utils.InjectedThings
import dartzee.utils.getAllNonMissSegments
import dartzee.utils.getAllPossibleSegments
import dartzee.utils.insertDartzeeRules
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.string.shouldNotContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import javax.swing.JButton
import org.junit.jupiter.api.Test

class TestGamePanelDartzee : AbstractTest() {
    private val ruleResults =
        listOf(
            DartzeeRoundResult(2, true, 50),
            DartzeeRoundResult(1, false, -115),
            DartzeeRoundResult(3, true, 18),
            DartzeeRoundResult(4, true, -66),
        )

    @Test
    fun `Should initialise totalRounds based on the number of rules`() {
        makeGamePanel(listOf(makeDartzeeRuleDto())).totalRounds shouldBe 2
        makeGamePanel(listOf(makeDartzeeRuleDto(), makeDartzeeRuleDto())).totalRounds shouldBe 3
    }

    @Test
    fun `Should register itself as a listener on the carousel`() {
        val carousel = DartzeeRuleCarousel(testRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(testRules, summaryPanel)
        carousel.listener shouldBe gamePanel
    }

    @Test
    fun `Should not show the convert to template button if game is already part of a template`() {
        val template = insertDartzeeTemplate()
        val g = insertGame(gameType = GameType.DARTZEE, gameParams = template.rowId)
        val panel = makeGamePanel(testRules, game = g)
        panel.getChild<JButton>("convertToTemplate").shouldNotBeVisible()
    }

    @Test
    fun `Should convert to a template, hiding the button and updating the window title`() {
        val g = insertGame(gameType = GameType.DARTZEE, gameParams = "")
        val parentWindow = FakeDartsScreen()

        dialogFactory.inputSelection = "The Jeneration Game"

        val panel = makeGamePanel(testRules, game = g, parentWindow = parentWindow)
        panel.clickChild<JButton>("convertToTemplate")

        val templateId = panel.gameEntity.gameParams
        templateId.shouldNotBeEmpty()
        val template = DartzeeTemplateEntity().retrieveForId(templateId)!!
        template.name shouldBe "The Jeneration Game"

        panel.getChild<JButton>("convertToTemplate").shouldNotBeVisible()
        parentWindow.title shouldBe "Game #1 (Dartzee - The Jeneration Game - practice game)"
    }

    @Test
    fun `Should not hide the button or update window title if template generation is cancelled`() {
        val g = insertGame(gameType = GameType.DARTZEE, gameParams = "")
        val parentWindow = FakeDartsScreen()

        dialogFactory.inputSelection = null

        val panel = makeGamePanel(testRules, game = g, parentWindow = parentWindow)
        panel.clickChild<JButton>("convertToTemplate")

        val templateId = panel.gameEntity.gameParams
        templateId.shouldBeEmpty()

        panel.getChild<JButton>("convertToTemplate").shouldBeVisible()
        parentWindow.title shouldBe ""
    }

    @Test
    fun `Should tell the summaryPanel to finish and select the first player when loading a finished game`() {
        val game = setUpDartzeeGameOnDatabase(5)

        val summaryPanel = mockk<DartzeeRuleSummaryPanel>(relaxed = true)

        val gamePanel = makeGamePanel(testRules, summaryPanel, game)
        gamePanel.loadGame(loadParticipants(game.rowId))

        verify { summaryPanel.gameFinished() }
        gamePanel.currentPlayerNumber shouldBe 0
    }

    @Test
    fun `Should load scores and results correctly`() {
        val game = setUpDartzeeGameOnDatabase(5)
        val carousel = DartzeeRuleCarousel(testRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(testRules, summaryPanel, game)
        gamePanel.loadGame(loadParticipants(game.rowId))

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
    fun `Should not update best game achievement for too few rules`() {
        val shorterRules = testRules.subList(0, 2)

        val player = insertPlayer()
        val game = setUpDartzeeGameOnDatabase(2, player)
        val carousel = DartzeeRuleCarousel(shorterRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(shorterRules, summaryPanel, game)
        gamePanel.loadGame(loadParticipants(game.rowId))

        gamePanel.getPlayerState().getScoreSoFar() shouldBe 230

        // Finish the game by failing
        gamePanel.dartThrown(makeDart(20, 0, SegmentType.MISS))
        carousel.getDisplayedTiles().first().doClick()

        retrieveAchievementForDetail(AchievementType.DARTZEE_BEST_GAME, player.rowId, "") shouldBe
            null
    }

    @Test
    fun `Should update best game achievement if there are 5 or more rules`() {
        val player = insertPlayer()
        val game = setUpDartzeeGameOnDatabase(4, player)
        val carousel = DartzeeRuleCarousel(testRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(testRules, summaryPanel, game)
        gamePanel.loadGame(loadParticipants(game.rowId))

        gamePanel.getPlayerState().getScoreSoFar() shouldBe 133

        // Finish the game by failing
        gamePanel.dartThrown(makeDart(20, 0, SegmentType.MISS))
        carousel.getDisplayedTiles().first().doClick()

        val achievement =
            retrieveAchievementForDetail(AchievementType.DARTZEE_BEST_GAME, player.rowId, "")!!
        achievement.achievementCounter shouldBe 13
        achievement.gameIdEarned shouldBe game.rowId
    }

    @Test
    fun `Should update flawless achievement if all rules passed`() {
        val allPassed =
            listOf(
                DartzeeRoundResult(2, true, 50),
                DartzeeRoundResult(1, true, 35),
                DartzeeRoundResult(3, true, 18),
                DartzeeRoundResult(4, true, 40),
            )

        val player = insertPlayer()
        val game = setUpDartzeeGameOnDatabase(5, player, allPassed)
        val carousel = DartzeeRuleCarousel(testRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(testRules, summaryPanel, game)
        gamePanel.loadGame(loadParticipants(game.rowId))
        gamePanel.updateAchievementsForFinish(-1, 180)

        val achievement =
            retrieveAchievementForDetail(AchievementType.DARTZEE_FLAWLESS, player.rowId, "")!!
        achievement.achievementCounter shouldBe 180
        achievement.achievementDetail shouldBe ""
        achievement.gameIdEarned shouldBe game.rowId
    }

    @Test
    fun `Should not update flawless achievement if a rule was failed`() {
        val player = insertPlayer()
        val game = setUpDartzeeGameOnDatabase(5, player)
        val carousel = DartzeeRuleCarousel(testRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(testRules, summaryPanel, game)
        gamePanel.loadGame(loadParticipants(game.rowId))
        gamePanel.updateAchievementsForFinish(-1, 180)

        getAchievementCount(AchievementType.DARTZEE_FLAWLESS) shouldBe 0
    }

    @Test
    fun `Should update under pressure achievement if hardest rule passed last`() {
        val hardestPassedLast =
            listOf(
                DartzeeRoundResult(2, true, 50),
                DartzeeRoundResult(1, true, 35),
                DartzeeRoundResult(3, true, 18),
                DartzeeRoundResult(4, true, 50),
            )

        val player = insertPlayer()
        val game = setUpDartzeeGameOnDatabase(5, player, hardestPassedLast)
        val carousel = DartzeeRuleCarousel(testRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(testRules, summaryPanel, game)
        gamePanel.loadGame(loadParticipants(game.rowId))
        gamePanel.updateAchievementsForFinish(-1, 180)

        val achievement =
            retrieveAchievementForDetail(
                AchievementType.DARTZEE_UNDER_PRESSURE,
                player.rowId,
                totalIsFifty.getDisplayName()
            )!!
        achievement.achievementCounter shouldBe 50
        achievement.achievementDetail shouldBe totalIsFifty.getDisplayName()
        achievement.gameIdEarned shouldBe game.rowId
    }

    @Test
    fun `Should not update under pressure achievement if last round was a fail`() {
        val hardestPassedLast =
            listOf(
                DartzeeRoundResult(2, true, 50),
                DartzeeRoundResult(1, true, 35),
                DartzeeRoundResult(3, true, 18),
                DartzeeRoundResult(4, false, -100),
            )

        val player = insertPlayer()
        val game = setUpDartzeeGameOnDatabase(5, player, hardestPassedLast)
        val carousel = DartzeeRuleCarousel(testRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(testRules, summaryPanel, game)
        gamePanel.loadGame(loadParticipants(game.rowId))
        gamePanel.updateAchievementsForFinish(-1, 180)

        getAchievementCount(AchievementType.DARTZEE_UNDER_PRESSURE) shouldBe 0
    }

    @Test
    fun `Should not update under pressure achievement if last round was not the hardest rule`() {
        val hardestPassedLast =
            listOf(
                DartzeeRoundResult(2, true, 50),
                DartzeeRoundResult(1, true, 35),
                DartzeeRoundResult(4, true, 50),
                DartzeeRoundResult(3, true, 18),
            )

        val player = insertPlayer()
        val game = setUpDartzeeGameOnDatabase(5, player, hardestPassedLast)
        val carousel = DartzeeRuleCarousel(testRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(testRules, summaryPanel, game)
        gamePanel.loadGame(loadParticipants(game.rowId))
        gamePanel.updateAchievementsForFinish(-1, 180)

        getAchievementCount(AchievementType.DARTZEE_UNDER_PRESSURE) shouldBe 0
    }

    @Test
    fun `Should insert a row for bingo, calculating the score correctly and not adding duplicates`() {
        val player = insertPlayer()
        val game = setUpDartzeeGameOnDatabase(5, player)
        val carousel = DartzeeRuleCarousel(testRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)

        val gamePanel = makeGamePanel(testRules, summaryPanel, game)
        gamePanel.loadGame(loadParticipants(game.rowId))
        gamePanel.updateAchievementsForFinish(-1, 180)
        gamePanel.updateAchievementsForFinish(-1, 80)
        gamePanel.updateAchievementsForFinish(-1, 1080)

        val rows = getAchievementRows(AchievementType.DARTZEE_BINGO)
        rows.size shouldBe 1
        val achievement = rows.first()
        achievement.achievementCounter shouldBe 80
        achievement.achievementDetail shouldBe "180"
    }

    @Test
    fun `Should update the carousel and dartboard on readyForThrow and each time a dart is thrown`() {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val reducedRules = testRules.subList(0, 2)
        val game = setUpDartzeeGameOnDatabase(1)
        val carousel = DartzeeRuleCarousel(reducedRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(reducedRules, summaryPanel, game)
        panel.loadGame(loadParticipants(game.rowId))

        carousel.completeTiles.shouldBeEmpty()
        carousel.pendingTiles.size shouldBe reducedRules.size

        val expectedSegments = getAllNonMissSegments().filter { !it.isDoubleExcludingBull() }
        panel.dartboard
            .segmentStatuses()!!
            .scoringSegments
            .shouldContainExactlyInAnyOrder(*expectedSegments.toTypedArray())

        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))

        val twoBlackOneWhiteSegments =
            twoBlackOneWhite.calculationResult!!.scoringSegments.toTypedArray()
        panel.dartboard
            .segmentStatuses()!!
            .scoringSegments
            .shouldContainExactlyInAnyOrder(*twoBlackOneWhiteSegments)

        panel.dartThrown(makeDart(20, 0, SegmentType.MISS))
        panel.dartboard.segmentStatuses()!!.scoringSegments.shouldBeEmpty()

        panel.btnReset.isEnabled = true
        panel.btnReset.doClick()
        panel.dartboard
            .segmentStatuses()!!
            .scoringSegments
            .shouldContainExactlyInAnyOrder(*expectedSegments.toTypedArray())
    }

    @Test
    fun `Should save darts on confirm pressed, then hide it after the first round`() {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val game = insertGame(gameType = GameType.DARTZEE)
        val player = insertPlayer(strategy = "")

        val carousel = DartzeeRuleCarousel(testRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(testRules, summaryPanel, game)
        panel.startNewGame(listOf(makeSingleParticipant(player)))

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
    fun `Should update valid segments on hover changed if fewer than 3 darts thrown`() {
        val carousel = DartzeeRuleCarousel(testRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(testRules, summaryPanel)
        panel.startNewGame(listOf(makeSingleParticipant()))

        panel.hoverChanged(SegmentStatuses(listOf(doubleNineteen), listOf(doubleNineteen)))
        panel.dartboard.segmentStatuses() shouldBe
            SegmentStatuses(listOf(doubleNineteen), listOf(doubleNineteen))

        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))

        panel.hoverChanged(SegmentStatuses(listOf(doubleTwenty), listOf(doubleTwenty)))
        panel.dartboard.segmentStatuses() shouldBe
            SegmentStatuses(listOf(doubleTwenty), listOf(doubleTwenty))

        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))
        panel.dartboard.segmentStatuses() shouldBe
            SegmentStatuses(getAllPossibleSegments(), getAllPossibleSegments())
        panel.hoverChanged(SegmentStatuses(listOf(bullseye), listOf(bullseye)))
        panel.dartboard.segmentStatuses() shouldBe
            SegmentStatuses(getAllPossibleSegments(), getAllPossibleSegments())
    }

    @Test
    fun `Should select the right player when a scorer is selected`() {
        val summaryPanel = mockk<DartzeeRuleSummaryPanel>(relaxed = true)
        val panel = makeGamePanel(testRules, summaryPanel, totalPlayers = 2)
        panel.startNewGame(listOf(makeSingleParticipant(), makeSingleParticipant()))

        panel.scorerSelected(panel.scorersOrdered[0])
        panel.currentPlayerNumber shouldBe 0
        panel.scorersOrdered[0].lblName.text shouldContain "<b>"
        panel.scorersOrdered[1].lblName.text shouldNotContain "<b>"
        verify { summaryPanel.update(listOf(), listOf(), 0, 1) }

        clearAllMocks()

        panel.scorerSelected(panel.scorersOrdered[1])
        panel.currentPlayerNumber shouldBe 1
        panel.scorersOrdered[0].lblName.text shouldNotContain "<b>"
        panel.scorersOrdered[1].lblName.text shouldContain "<b>"
        verify { summaryPanel.update(listOf(), listOf(), 0, 1) }
    }

    @Test
    fun `AI should throw scoring darts during the scoring round`() {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val game = insertGame(gameType = GameType.DARTZEE)
        val player = insertPlayer(strategy = "")

        val carousel = DartzeeRuleCarousel(testRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(testRules, summaryPanel, game)
        panel.startNewGame(listOf(makeSingleParticipant(player)))

        val listener = mockk<DartboardListener>(relaxed = true)
        panel.dartboard.addDartboardListener(listener)

        panel.doAiTurn(beastDartsModel())

        verify { listener.dartThrown(Dart(20, 3)) }
    }

    @Test
    fun `AI should throw based on segment status, and adjust correctly for number of darts thrown`() {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val game = setUpDartzeeGameOnDatabase(1)

        val carousel = mockk<DartzeeRuleCarousel>(relaxed = true)
        every { carousel.getSegmentStatus() } returns
            SegmentStatuses(listOf(singleTwenty), getAllNonMissSegments())
        every { carousel.initialised } returns true

        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(testRules, summaryPanel, game)
        panel.loadGame(loadParticipants(game.rowId))

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
    fun `AI turn should complete normally, and the highest passed rule should be selected`() {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val model = beastDartsModel(dartzeePlayStyle = DartzeePlayStyle.CAUTIOUS)

        val player = insertPlayer(model = beastDartsModel())
        val game = setUpDartzeeGameOnDatabase(1, player)

        val carousel = DartzeeRuleCarousel(testRules)

        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(testRules, summaryPanel, game)
        panel.loadGame(loadParticipants(game.rowId))

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

    @Test
    fun `Should update achievements correctly for a team game`() {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val (p1, p2) = preparePlayers(2)
        val game = insertGame(gameType = GameType.DARTZEE)
        val team = makeTeam(p1, p2, gameId = game.rowId)

        val carousel = DartzeeRuleCarousel(testRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(testRules, summaryPanel, game)
        panel.startNewGame(listOf(team))

        // P1 - Scoring round
        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(5, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(1, 1, SegmentType.OUTER_SINGLE))
        panel.btnConfirm.doClick()
        panel.getPlayerState().getScoreSoFar() shouldBe 26

        // P2 - IOI
        panel.dartThrown(makeDart(20, 1, SegmentType.INNER_SINGLE))
        panel.dartThrown(makeDart(5, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(1, 1, SegmentType.INNER_SINGLE))
        carousel.getDisplayedTiles().first().doClick()
        panel.getPlayerState().getScoreSoFar() shouldBe 52

        // P1 - Score eighteens
        panel.dartThrown(makeDart(18, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(18, 1, SegmentType.OUTER_SINGLE))
        carousel.getDisplayedTiles().first().doClick()
        panel.getPlayerState().getScoreSoFar() shouldBe 88

        // P2 - Fail
        panel.dartThrown(makeDart(18, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(18, 1, SegmentType.OUTER_SINGLE))
        carousel.getDisplayedTiles().first().doClick()
        panel.getPlayerState().getScoreSoFar() shouldBe 44

        // P1 - Score 50
        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(18, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(12, 1, SegmentType.OUTER_SINGLE))
        carousel.getDisplayedTiles().first().doClick()
        panel.getPlayerState().getScoreSoFar() shouldBe 94

        retrieveAchievementsForPlayer(p1.rowId)
            .shouldContainExactly(
                AchievementSummary(
                    AchievementType.DARTZEE_UNDER_PRESSURE,
                    50,
                    game.rowId,
                    totalIsFifty.getDisplayName()
                )
            )

        retrieveAchievementsForPlayer(p2.rowId)
            .shouldContainExactly(
                AchievementSummary(AchievementType.DARTZEE_HALVED, 44, game.rowId)
            )
    }

    @Test
    fun `Should not unlock flawless achievement for a team game`() {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val (p1, p2) = preparePlayers(2)
        val game = insertGame(gameType = GameType.DARTZEE)
        val team = makeTeam(p1, p2, gameId = game.rowId)

        val carousel = DartzeeRuleCarousel(testRules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val panel = makeGamePanel(testRules, summaryPanel, game)
        panel.startNewGame(listOf(team))

        // P1 - Scoring round
        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(5, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(1, 1, SegmentType.OUTER_SINGLE))
        panel.btnConfirm.doClick()
        panel.getPlayerState().getScoreSoFar() shouldBe 26

        // P2 - IOI
        panel.dartThrown(makeDart(20, 1, SegmentType.INNER_SINGLE))
        panel.dartThrown(makeDart(5, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(1, 1, SegmentType.INNER_SINGLE))
        carousel.getDisplayedTiles().first().doClick()
        panel.getPlayerState().getScoreSoFar() shouldBe 52

        // P1 - Score eighteens
        panel.dartThrown(makeDart(18, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(18, 1, SegmentType.OUTER_SINGLE))
        carousel.getDisplayedTiles().first().doClick()
        panel.getPlayerState().getScoreSoFar() shouldBe 88

        // P2 - 2B1W
        panel.dartThrown(makeDart(18, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(5, 1, SegmentType.OUTER_SINGLE))
        carousel.getDisplayedTiles().first().doClick()
        panel.getPlayerState().getScoreSoFar() shouldBe 131

        // P1 - Score 50
        panel.dartThrown(makeDart(20, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(18, 1, SegmentType.OUTER_SINGLE))
        panel.dartThrown(makeDart(12, 1, SegmentType.OUTER_SINGLE))
        carousel.getDisplayedTiles().first().doClick()
        panel.getPlayerState().getScoreSoFar() shouldBe 181

        AchievementEntity()
            .countWhere("AchievementType = '${AchievementType.DARTZEE_FLAWLESS}'") shouldBe 0
    }

    private fun GamePanelDartzee.getPlayerState() = getPlayerStates().first()

    private fun DartzeeRuleCarousel.getDisplayedTiles() =
        tilePanel.getAllChildComponentsForType<DartzeeRuleTile>().filter { it.isVisible }

    private fun setUpDartzeeGameOnDatabase(
        rounds: Int,
        player: PlayerEntity = insertPlayer(),
        results: List<DartzeeRoundResult> = ruleResults
    ): GameEntity {
        val dtFinish = if (rounds > 4) getSqlDateNow() else DateStatics.END_OF_TIME
        val game = insertGame(gameType = GameType.DARTZEE, dtFinish = dtFinish)

        insertDartzeeRules(game.rowId, testRules)

        val participant =
            insertParticipant(gameId = game.rowId, ordinal = 0, playerId = player.rowId)

        if (rounds > 0) {
            insertDart(participant = participant, roundNumber = 1, ordinal = 1)
            insertDart(participant = participant, roundNumber = 1, ordinal = 2)
            insertDart(participant = participant, roundNumber = 1, ordinal = 3)
        }

        if (rounds > 1) {
            insertDart(
                participant = participant,
                roundNumber = 2,
                ordinal = 1,
                score = 18,
                multiplier = 1
            )
            insertDart(
                participant = participant,
                roundNumber = 2,
                ordinal = 2,
                score = 12,
                multiplier = 1
            )
            insertDart(
                participant = participant,
                roundNumber = 2,
                ordinal = 3,
                score = 20,
                multiplier = 1
            )

            DartzeeRoundResultEntity.factoryAndSave(results[0], participant, 2)
        }

        if (rounds > 2) {
            insertDart(
                participant = participant,
                roundNumber = 3,
                ordinal = 1,
                score = 20,
                multiplier = 0
            )
            DartzeeRoundResultEntity.factoryAndSave(results[1], participant, 3)
        }

        if (rounds > 3) {
            insertDart(
                participant = participant,
                roundNumber = 4,
                ordinal = 1,
                score = 18,
                multiplier = 1
            )
            insertDart(
                participant = participant,
                roundNumber = 4,
                ordinal = 2,
                score = 4,
                multiplier = 1
            )
            insertDart(
                participant = participant,
                roundNumber = 4,
                ordinal = 3,
                score = 13,
                multiplier = 1
            )
            DartzeeRoundResultEntity.factoryAndSave(results[2], participant, 4)
        }

        if (rounds > 4) {
            insertDart(
                participant = participant,
                roundNumber = 5,
                ordinal = 1,
                score = 20,
                multiplier = 1
            )
            insertDart(
                participant = participant,
                roundNumber = 5,
                ordinal = 2,
                score = 20,
                multiplier = 0
            )
            insertDart(
                participant = participant,
                roundNumber = 5,
                ordinal = 3,
                score = 15,
                multiplier = 0
            )
            DartzeeRoundResultEntity.factoryAndSave(results[3], participant, 5)
        }

        return game
    }

    private fun makeGamePanel(
        dtos: List<DartzeeRuleDto>,
        summaryPanel: DartzeeRuleSummaryPanel = mockk(relaxed = true),
        game: GameEntity = insertGame(),
        totalPlayers: Int = 1,
        parentWindow: AbstractDartsGameScreen = mockk(relaxed = true)
    ): GamePanelDartzee {
        return GamePanelDartzee(parentWindow, game, totalPlayers, dtos, summaryPanel)
    }
}
