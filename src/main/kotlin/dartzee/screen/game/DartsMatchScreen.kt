package dartzee.screen.game

import dartzee.achievements.AbstractAchievement
import dartzee.core.util.getSqlDateNow
import dartzee.db.DartsMatchEntity
import dartzee.db.GameEntity
import dartzee.game.matchIsComplete
import dartzee.game.prepareNextEntities
import dartzee.game.state.AbstractPlayerState
import dartzee.logging.CODE_GAME_LAUNCHED
import dartzee.logging.CODE_MATCH_FINISHED
import dartzee.logging.KEY_FROM_MATCH
import dartzee.logging.KEY_GAME_ID
import dartzee.logging.KEY_GAME_LOCAL_ID
import dartzee.logging.KEY_MATCH_ID
import dartzee.logging.KEY_MATCH_LOCAL_ID
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.logger
import java.awt.BorderLayout
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

abstract class DartsMatchScreen<PlayerState : AbstractPlayerState<PlayerState>>(
    private val matchPanel: MatchSummaryPanel<PlayerState>,
    val match: DartsMatchEntity
) : AbstractDartsGameScreen(), ChangeListener {
    override val windowName = match.getMatchDesc()

    private val tabbedPane = JTabbedPane(SwingConstants.TOP)
    private val hmGameIdToTab = mutableMapOf<String, DartsGamePanel<*, PlayerState>>()

    init {
        contentPane.add(tabbedPane, BorderLayout.CENTER)

        tabbedPane.addTab("Match", matchPanel)
        tabbedPane.addChangeListener(this)

        title = match.getMatchDesc()
    }

    abstract fun factoryGamePanel(
        parent: AbstractDartsGameScreen,
        game: GameEntity,
        totalPlayers: Int
    ): DartsGamePanel<*, PlayerState>

    fun addGameToMatch(game: GameEntity, totalPlayers: Int): DartsGamePanel<*, *> {
        // Cache this screen in ScreenCache
        val gameId = game.rowId
        ScreenCache.addDartsGameScreen(gameId, this)

        // Initialise some basic properties of the tab, such as visibility of components etc
        val tab = factoryGamePanel(this, game, totalPlayers)

        matchPanel.addGameTab(tab)

        // Add the single game tab and set the parent window to be visible
        tabbedPane.addTab("#" + game.localId, tab)
        hmGameIdToTab[gameId] = tab
        isVisible = true

        return tab
    }

    fun addParticipant(localId: Long, state: PlayerState) {
        matchPanel.addParticipant(localId, state)
    }

    fun finaliseParticipants() {
        matchPanel.finaliseScorers(this)
    }

    override fun startNextGameIfNecessary() {
        if (isMatchComplete()) {
            logger.info(
                CODE_MATCH_FINISHED,
                "Match #${match.localId} finished.",
                KEY_MATCH_ID to match.rowId,
                KEY_MATCH_LOCAL_ID to match.localId
            )
            match.dtFinish = getSqlDateNow()
            match.saveToDatabase()
            return
        }

        shouldPack = false

        val firstGamePanel = hmGameIdToTab.values.first()
        val firstGameParticipants = firstGamePanel.getPlayerStates().map { it.wrappedParticipant }

        val (nextGame, nextParticipants) =
            prepareNextEntities(
                firstGamePanel.gameEntity,
                firstGameParticipants,
                hmGameIdToTab.size + 1
            )

        logger.info(
            CODE_GAME_LAUNCHED,
            "Launched game ${nextGame.matchOrdinal} of match #${match.localId}.",
            KEY_MATCH_ID to match.rowId,
            KEY_MATCH_LOCAL_ID to match.localId,
            KEY_GAME_ID to nextGame.rowId,
            KEY_GAME_LOCAL_ID to nextGame.localId,
            KEY_FROM_MATCH to true
        )

        val panel = addGameToMatch(nextGame, nextParticipants.size)
        panel.startNewGame(nextParticipants)
    }

    private fun isMatchComplete(): Boolean {
        val participants = matchPanel.getAllParticipants()
        return matchIsComplete(match, participants)
    }

    override fun achievementUnlocked(
        gameId: String,
        playerId: String,
        achievement: AbstractAchievement
    ) {
        val tab = hmGameIdToTab[gameId]!!
        tab.achievementUnlocked(playerId, achievement)
    }

    override fun displayGame(gameId: String) {
        super.displayGame(gameId)

        val tab = hmGameIdToTab[gameId]
        tabbedPane.selectedComponent = tab
    }

    override fun fireAppearancePreferencesChanged() {
        hmGameIdToTab.values.forEach { it.fireAppearancePreferencesChanged() }
    }

    /** ChangeListener */
    override fun stateChanged(e: ChangeEvent) {
        val sourceTabbedPane = e.source as JTabbedPane
        val selectedTab = sourceTabbedPane.selectedComponent
        if (selectedTab is DartsGamePanel<*, *>) {
            val title = selectedTab.gameTitle
            setTitle(title)
        } else {
            title = match.getMatchDesc()
        }
    }
}
