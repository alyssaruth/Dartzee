package dartzee.screen.game

import dartzee.`object`.Dart
import dartzee.core.util.DateStatics
import dartzee.dartzee.DartzeeCalculator
import dartzee.db.DartzeeRoundResultEntity
import dartzee.game.GameType
import dartzee.helper.*
import dartzee.listener.DartboardListener
import dartzee.screen.game.dartzee.DartzeeRuleCarousel
import dartzee.screen.game.dartzee.DartzeeRuleSummaryPanel
import dartzee.screen.game.dartzee.GamePanelDartzee
import dartzee.screen.game.x01.GamePanelX01
import dartzee.utils.InjectedThings
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
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
        val carousel = DartzeeRuleCarousel(rules)
        val summaryPanel = DartzeeRuleSummaryPanel(carousel)
        val parentWindow = mockk<AbstractDartsGameScreen>(relaxed = true)
        every { parentWindow.isVisible } returns true

        val panel = GamePanelDartzee(parentWindow, game, 1, rules, summaryPanel)

        val listener = mockk<DartboardListener>(relaxed = true)
        panel.dartboard.addDartboardListener(listener)

        panel.startNewGame(listOf(player))

        while (!game.isFinished()) {
            Thread.sleep(200)
        }

        verifySequence {
            // Scoring round
            listener.dartThrown(Dart(20, 3))
            listener.dartThrown(Dart(20, 3))
            listener.dartThrown(Dart(20, 3))

            // All twenties
            listener.dartThrown(Dart(20, 1))
            listener.dartThrown(Dart(20, 1))
            listener.dartThrown(Dart(20, 1))

            // Score eighteens
            listener.dartThrown(Dart(18, 1))
            listener.dartThrown(Dart(18, 1))
            listener.dartThrown(Dart(25, 2))
        }

        val pt = retrieveParticipant()
        pt.finalScore shouldBe 276
        pt.dtFinished shouldNotBe DateStatics.END_OF_TIME

        val results = DartzeeRoundResultEntity().retrieveEntities().sortedBy { it.roundNumber }
        val roundOne = results.first()
        roundOne.success shouldBe true
        roundOne.ruleNumber shouldBe 2
        roundOne.score shouldBe 60
        roundOne.participantId shouldBe pt.rowId

        val roundTwo = results[1]
        roundTwo.success shouldBe true
        roundTwo.ruleNumber shouldBe 1
        roundTwo.score shouldBe 36
        roundTwo.participantId shouldBe pt.rowId
    }

    @Test
    fun `E2E - X01`()
    {
        val game = insertGame(gameType = GameType.X01, gameParams = "501")

        val model = beastDartsModel()
        model.hmScoreToDart[81] = Dart(19, 3)
        val player = insertPlayer(model = model)

        val parentWindow = mockk<AbstractDartsGameScreen>(relaxed = true)
        every { parentWindow.isVisible } returns true

        val panel = GamePanelX01(parentWindow, game, 1)

        val listener = mockk<DartboardListener>(relaxed = true)
        panel.dartboard.addDartboardListener(listener)

        panel.startNewGame(listOf(player))

        while (!game.isFinished()) {
            Thread.sleep(200)
        }

        verifySequence {
            listener.dartThrown(Dart(20, 3))
            listener.dartThrown(Dart(20, 3))
            listener.dartThrown(Dart(20, 3))

            listener.dartThrown(Dart(20, 3))
            listener.dartThrown(Dart(20, 3))
            listener.dartThrown(Dart(20, 3))

            listener.dartThrown(Dart(20, 3))
            listener.dartThrown(Dart(19, 3))
            listener.dartThrown(Dart(12, 2))
        }

        val pt = retrieveParticipant()
        pt.finalScore shouldBe 9
        pt.dtFinished shouldNotBe DateStatics.END_OF_TIME
    }
}