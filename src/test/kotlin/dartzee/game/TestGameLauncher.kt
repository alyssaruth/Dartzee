package dartzee.game

import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.DartsMatchEntity
import dartzee.db.EntityName
import dartzee.game.state.GolfPlayerState
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.helper.insertDartsMatch
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.helper.retrieveGame
import dartzee.helper.retrieveParticipant
import dartzee.helper.scoreEighteens
import dartzee.helper.twoBlackOneWhite
import dartzee.logging.CODE_LOAD_ERROR
import dartzee.logging.Severity
import dartzee.screen.ScreenCache
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.DartsGamePanel
import dartzee.screen.game.DartsGameScreen
import dartzee.screen.game.DartsMatchScreen
import dartzee.screen.game.dartzee.GamePanelDartzee
import dartzee.screen.game.golf.GamePanelGolf
import dartzee.screen.game.rtc.GamePanelRoundTheClock
import dartzee.screen.game.x01.GamePanelX01
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TestGameLauncher : AbstractTest() {
    @Test
    fun `Should launch a new game of X01 successfully`() {
        testNewGameLaunch<GamePanelX01>(GameType.X01, "501")
    }

    @Test
    fun `Should launch a new game of RTC successfully`() {
        testNewGameLaunch<GamePanelRoundTheClock>(
            GameType.ROUND_THE_CLOCK,
            RoundTheClockConfig(ClockType.Standard, true).toJson()
        )
    }

    @Test
    fun `Should launch a new game of Golf successfully`() {
        testNewGameLaunch<GamePanelGolf>(GameType.GOLF, "18")
    }

    @Test
    fun `Should launch a new game of Dartzee successfully`() {
        val dartzeeDtos = listOf(twoBlackOneWhite, scoreEighteens)
        testNewGameLaunch<GamePanelDartzee>(GameType.DARTZEE, "", dartzeeDtos)
        getCountFromTable(EntityName.DartzeeRule) shouldBe 2
    }

    private inline fun <reified T : DartsGamePanel<*, *>> testNewGameLaunch(
        gameType: GameType,
        gameParams: String,
        dartzeeDtos: List<DartzeeRuleDto> = emptyList()
    ) {
        val p = insertPlayer(strategy = "")
        val params = GameLaunchParams(listOf(p), gameType, gameParams, false, dartzeeDtos)
        GameLauncher().launchNewGame(params)

        val scrns = ScreenCache.getDartsGameScreens()
        scrns.size shouldBe 1
        val scrn = scrns.first()
        scrn.shouldBeInstanceOf<DartsGameScreen>()
        scrn.isVisible shouldBe true
        scrn.gamePanel.shouldBeInstanceOf<T>()

        val g = retrieveGame()
        g.gameType shouldBe gameType
        g.gameParams shouldBe gameParams

        val pt = retrieveParticipant()
        pt.playerId shouldBe p.rowId
        pt.gameId shouldBe g.rowId
    }

    @Test
    fun `Should launch a new match successfully`() {
        val match = DartsMatchEntity.factoryFirstTo(2)
        val p = insertPlayer()
        val p2 = insertPlayer()
        val params = GameLaunchParams(listOf(p, p2), GameType.GOLF, "18", false, emptyList())

        GameLauncher().launchNewMatch(match, params)

        val scrns = ScreenCache.getDartsGameScreens()
        scrns.size shouldBe 1
        val scrn = scrns.first()
        scrn.shouldBeInstanceOf<DartsMatchScreen<GolfPlayerState>>()

        match.gameType shouldBe GameType.GOLF
        match.gameParams shouldBe "18"
    }

    @Test
    fun `Should bring up the window when loading an already visible game`() {
        val scrn = mockk<AbstractDartsGameScreen>(relaxed = true)
        ScreenCache.addDartsGameScreen("foo", scrn)

        GameLauncher().loadAndDisplayGame("foo")

        verify { scrn.displayGame("foo") }
    }

    @Test
    fun `Should show an error and return if no game exists for the id`() {
        GameLauncher().loadAndDisplayGame("foo")

        dialogFactory.errorsShown.shouldContainExactly("Game foo does not exist.")
    }

    @Test
    fun `Should handle an error when trying to load a single game`() {
        val g = insertGame()

        GameLauncher().loadAndDisplayGame(g.rowId)

        verifyLog(CODE_LOAD_ERROR, Severity.ERROR)
        dialogFactory.errorsShown.shouldContainExactly("Failed to load Game #${g.localId}")
        ScreenCache.getDartsGameScreens().shouldBeEmpty()
    }

    @Test
    fun `Should handle an error when trying to load a game that's part of a match`() {
        val match = insertDartsMatch()
        val g = insertGame(dartsMatchId = match.rowId)

        GameLauncher().loadAndDisplayGame(g.rowId)

        verifyLog(CODE_LOAD_ERROR, Severity.ERROR)
        dialogFactory.errorsShown.shouldContainExactly("Failed to load Match #${match.localId}")
        ScreenCache.getDartsGameScreens().shouldBeEmpty()
    }
}
