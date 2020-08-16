package dartzee.screen.game

import dartzee.`object`.Dart
import dartzee.ai.AimDart
import dartzee.awaitCondition
import dartzee.core.util.DateStatics
import dartzee.core.util.getSortedValues
import dartzee.dartzee.DartzeeCalculator
import dartzee.db.CLOCK_TYPE_STANDARD
import dartzee.db.DartEntity
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.GameEntity
import dartzee.game.GameType
import dartzee.helper.*
import dartzee.listener.DartboardListener
import dartzee.utils.InjectedThings
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import dartzee.utils.insertDartzeeRules
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.Test

class TestGameplayE2E: AbstractRegistryTest()
{
    override fun getPreferencesAffected() = listOf(PREFERENCES_INT_AI_SPEED)

    override fun beforeEachTest()
    {
        super.beforeEachTest()
        PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, 0)
    }

    @Test
    fun `E2E - Dartzee`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val game = insertGame(gameType = GameType.DARTZEE)

        val model = beastDartsModel()
        val player = insertPlayer(model = model)

        val rules = listOf(scoreEighteens, allTwenties)
        insertDartzeeRules(game, rules)

        val (panel, listener) = setUpGamePanel(game)
        panel.startNewGame(listOf(player))
        awaitGameFinish(game)

        val expectedRounds = listOf(
                listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)), //Scoring round
                listOf(Dart(20, 1), Dart(20, 1), Dart(20, 1)), //All Twenties
                listOf(Dart(18, 1), Dart(18, 1), Dart(25, 2)) //Score Eighteens
        )

        verifyState(panel, listener, expectedRounds, finalScore = 276)

        val participantId = retrieveParticipant().rowId

        val results = DartzeeRoundResultEntity().retrieveEntities().sortedBy { it.roundNumber }
        val roundOne = results.first()
        roundOne.success shouldBe true
        roundOne.ruleNumber shouldBe 2
        roundOne.score shouldBe 60
        roundOne.participantId shouldBe participantId

        val roundTwo = results[1]
        roundTwo.success shouldBe true
        roundTwo.ruleNumber shouldBe 1
        roundTwo.score shouldBe 36
        roundTwo.participantId shouldBe participantId
    }

    @Test
    fun `E2E - 501`()
    {
        val game = insertGame(gameType = GameType.X01, gameParams = "501")

        val aiModel = beastDartsModel(hmScoreToDart = mapOf(81 to AimDart(19, 3)))
        val player = insertPlayer(model = aiModel)

        val (panel, listener) = setUpGamePanel(game)

        panel.startNewGame(listOf(player))
        awaitGameFinish(game)

        val expectedRounds = listOf(
                listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)),
                listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)),
                listOf(Dart(20, 3), Dart(19, 3), Dart(12, 2))
        )

        verifyState(panel, listener, expectedRounds, finalScore = 9)
    }

    @Test
    fun `E2E - Golf`()
    {
        val game = insertGame(gameType = GameType.GOLF, gameParams = "18")

        val model = beastDartsModel()
        val player = insertPlayer(model = model)

        val (panel, listener) = setUpGamePanel(game)
        panel.startNewGame(listOf(player))
        awaitGameFinish(game)

        val expectedDarts = (1..18).map { listOf(Dart(it, 2)) }
        verifyState(panel, listener, expectedDarts, finalScore = 18, expectedScorerRows = 20)
    }

    @Test
    fun `E2E - RTC`()
    {
        val game = insertGame(gameType = GameType.ROUND_THE_CLOCK, gameParams = CLOCK_TYPE_STANDARD)

        val model = beastDartsModel()
        val player = insertPlayer(model = model)

        val (panel, listener) = setUpGamePanel(game)
        panel.startNewGame(listOf(player))
        awaitGameFinish(game)

        val expectedDarts = (1..20).map { Dart(it, 1) }.chunked(4)
        verifyState(panel, listener, expectedDarts, 20)
    }

    data class GamePanelTestSetup(val gamePanel: DartsGamePanel<*, *, *>, val listener: DartboardListener)

    private fun setUpGamePanel(game: GameEntity): GamePanelTestSetup
    {
        val parentWindow = mockk<AbstractDartsGameScreen>(relaxed = true)
        every { parentWindow.isVisible } returns true

        val panel = DartsGamePanel.factory(parentWindow, game, 1)
        val listener = mockk<DartboardListener>(relaxed = true)
        panel.dartboard.addDartboardListener(listener)

        return GamePanelTestSetup(panel, listener)
    }

    private fun awaitGameFinish(game: GameEntity)
    {
        awaitCondition { game.isFinished() }
    }

    private fun verifyState(panel: DartsGamePanel<*, *, *>,
                            listener: DartboardListener,
                            dartRounds: List<List<Dart>>,
                            finalScore: Int,
                            expectedScorerRows: Int = dartRounds.size)
    {
        // ParticipantEntity on the database
        val pt = retrieveParticipant()
        pt.finalScore shouldBe finalScore
        pt.dtFinished shouldNotBe DateStatics.END_OF_TIME
        pt.gameId shouldBe panel.gameEntity.rowId
        pt.finishingPosition shouldBe -1
        pt.ordinal shouldBe 0

        // Screen state
        panel.activeScorer.getTotalScore() shouldBe finalScore
        panel.activeScorer.getRowCount() shouldBe expectedScorerRows

        // Use our dartboardListener to verify that the right throws were registered
        val darts = dartRounds.flatten()
        verifySequence {
            darts.forEach {
                listener.dartThrown(it)
            }
        }

        // Check that the dart entities on the database line up
        val dartEntities = DartEntity().retrieveEntities().sortedWith(compareBy( { it.roundNumber }, { it.ordinal }))
        dartEntities.forEach {
            it.participantId shouldBe pt.rowId
            it.playerId shouldBe pt.playerId
        }

        val chunkedDartEntities: List<List<DartEntity>> = dartEntities.groupBy { it.roundNumber }.getSortedValues().map { it.sortedBy { drt -> drt.ordinal } }
        val retrievedDartRounds = chunkedDartEntities.map { rnd -> rnd.map { drt -> Dart(drt.score, drt.multiplier) } }
        retrievedDartRounds shouldBe dartRounds
    }
}