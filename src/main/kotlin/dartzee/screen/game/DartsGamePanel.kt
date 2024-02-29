package dartzee.screen.game

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.getBestGameAchievement
import dartzee.achievements.getTeamWinAchievementType
import dartzee.achievements.getWinAchievementType
import dartzee.ai.DartsAiModel
import dartzee.bean.SliderAiSpeed
import dartzee.core.obj.HashMapList
import dartzee.core.util.DialogUtil
import dartzee.core.util.doBadMiss
import dartzee.core.util.doBull
import dartzee.core.util.getSortedValues
import dartzee.core.util.getSqlDateNow
import dartzee.core.util.isEndOfTime
import dartzee.core.util.runOnEventThread
import dartzee.db.AchievementEntity
import dartzee.db.DartzeeRuleEntity
import dartzee.db.GameEntity
import dartzee.game.GameType
import dartzee.game.state.AbstractPlayerState
import dartzee.game.state.IWrappedParticipant
import dartzee.listener.DartboardListener
import dartzee.`object`.ComputedPoint
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.screen.GameplayDartboard
import dartzee.screen.game.dartzee.DartzeeRuleCarousel
import dartzee.screen.game.dartzee.DartzeeRuleSummaryPanel
import dartzee.screen.game.dartzee.GamePanelDartzee
import dartzee.screen.game.golf.GamePanelGolf
import dartzee.screen.game.rtc.GamePanelRoundTheClock
import dartzee.screen.game.scorer.AbstractDartsScorer
import dartzee.screen.game.x01.GamePanelX01
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import dartzee.utils.ResourceCache.ICON_STATS_LARGE
import dartzee.utils.getColourWrapperFromPrefs
import dartzee.utils.getQuotedIdStr
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JToggleButton

abstract class DartsGamePanel<
    S : AbstractDartsScorer<PlayerState>,
    PlayerState : AbstractPlayerState<PlayerState>
>(
    protected val parentWindow: AbstractDartsGameScreen,
    val gameEntity: GameEntity,
    protected val totalPlayers: Int
) : PanelWithScorers<S>(), DartboardListener, ActionListener {
    private val hmPlayerNumberToState = mutableMapOf<Int, PlayerState>()
    private val hmPlayerNumberToScorer = mutableMapOf<Int, S>()

    val gameTitle = makeGameTitle()

    // Transitive things
    var currentPlayerNumber = 0
    protected var currentRoundNumber = -1

    // For AI turns
    protected var cpuThread: Thread? = null

    /** Screen stuff */
    val dartboard = GameplayDartboard(colourWrapper())
    private val statsPanel = factoryStatsPanel(gameEntity.gameParams)

    private val panelAiSlider = JPanel()
    private val panelSouth = JPanel()
    protected val slider = SliderAiSpeed()
    protected val panelButtons = JPanel()
    val btnConfirm = JButton("")
    val btnReset = JButton("")
    private val btnResign = JButton("")
    private val btnStats = JToggleButton("")
    private val btnSlider = JToggleButton("")

    private fun getPlayersDesc() =
        if (totalPlayers == 1) "practice game" else "$totalPlayers players"

    protected fun getActiveCount() = getParticipants().count { it.participant.isActive() }

    private fun getResignedCount() = getParticipants().count { it.participant.resigned }

    fun getGameId() = gameEntity.rowId

    open fun colourWrapper() = getColourWrapperFromPrefs()

    open fun getFinishingPositionFromPlayersRemaining(): Int {
        if (totalPlayers == 1) {
            return -1
        }

        return totalPlayers - getActiveCount() - getResignedCount() + 1
    }

    protected fun getCurrentPlayerStrategy(): DartsAiModel {
        val participant = getCurrentIndividual()
        return participant.getModel()
    }

    /** Stuff that will ultimately get refactored off into a GameState thingy */
    fun getPlayerStates() = hmPlayerNumberToState.getSortedValues()

    protected fun getParticipants() =
        hmPlayerNumberToState.entries.sortedBy { it.key }.map { it.value.wrappedParticipant }

    protected fun getCurrentPlayerId() = getCurrentIndividual().playerId

    protected fun getCurrentPlayerState() = getPlayerState(currentPlayerNumber)

    private fun getPlayerState(playerNumber: Int) = hmPlayerNumberToState[playerNumber]!!

    private fun getParticipant(playerNumber: Int) = getPlayerState(playerNumber).wrappedParticipant

    private fun getCurrentIndividual() = getCurrentPlayerState().currentIndividual()

    fun getDartsThrown() = getCurrentPlayerState().currentRound

    fun dartsThrownCount() = getDartsThrown().size

    private fun addState(playerNumber: Int, state: PlayerState, scorer: S) {
        hmPlayerNumberToState[playerNumber] = state
        hmPlayerNumberToScorer[playerNumber] = scorer
    }

    protected fun getCurrentScorer() = hmPlayerNumberToScorer.getValue(currentPlayerNumber)

    protected fun getPlayerNumberForScorer(scorer: S): Int = scorersOrdered.indexOf(scorer)

    init {
        panelCenter.add(dartboard, BorderLayout.CENTER)
        dartboard.addDartboardListener(this)
        panelCenter.add(panelSouth, BorderLayout.SOUTH)
        panelSouth.layout = BorderLayout(0, 0)
        panelSouth.add(panelAiSlider, BorderLayout.NORTH)
        panelSouth.add(panelButtons, BorderLayout.CENTER)
        btnConfirm.preferredSize = Dimension(80, 80)
        btnConfirm.icon = ImageIcon(javaClass.getResource("/buttons/Confirm.png"))
        btnConfirm.toolTipText = "Confirm round"
        panelButtons.add(btnConfirm)
        btnReset.preferredSize = Dimension(80, 80)
        btnReset.icon = ImageIcon(javaClass.getResource("/buttons/Reset.png"))
        btnReset.toolTipText = "Reset round"
        panelButtons.add(btnReset)
        btnResign.preferredSize = Dimension(80, 80)
        btnResign.icon = ImageIcon(javaClass.getResource("/buttons/resign.png"))
        btnResign.toolTipText = "Resign"
        panelButtons.add(btnResign)

        btnStats.toolTipText = "View stats"
        btnStats.preferredSize = Dimension(80, 80)
        btnStats.icon = ICON_STATS_LARGE

        panelButtons.add(btnStats)
        btnSlider.icon = ImageIcon(javaClass.getResource("/buttons/aiSpeed.png"))
        btnSlider.toolTipText = "AI throw speed"
        btnSlider.preferredSize = Dimension(80, 80)

        panelAiSlider.layout = FlowLayout(FlowLayout.CENTER, 0, 0)
        panelAiSlider.add(slider)
        slider.isVisible = false
        slider.preferredSize = Dimension(320, 30)

        panelButtons.add(btnSlider)

        btnResign.addActionListener(this)
        btnConfirm.addActionListener(this)
        btnReset.addActionListener(this)
        btnStats.addActionListener(this)
        btnSlider.addActionListener(this)
    }

    /** Abstract methods */
    abstract fun factoryState(pt: IWrappedParticipant): PlayerState

    abstract fun computeAiDart(model: DartsAiModel): ComputedPoint?

    abstract fun shouldStopAfterDartThrown(): Boolean

    abstract fun shouldAIStop(): Boolean

    abstract fun saveDartsAndProceed()

    abstract fun factoryStatsPanel(gameParams: String): AbstractGameStatisticsPanel<PlayerState>

    abstract fun turnFinished()

    open fun updateVariablesForDartThrown(dart: Dart) {}

    /** Regular methods */
    fun startNewGame(participants: List<IWrappedParticipant>) {
        participants.forEachIndexed(::addParticipant)

        finaliseParticipants()

        nextTurn()
    }

    protected fun nextTurn() {
        updateActivePlayer()

        println("NEXT TURN")

        // Create a new round for this player
        currentRoundNumber = getCurrentPlayerState().currentRoundNumber()

        btnReset.isVisible = false
        btnConfirm.isVisible = false
        btnResign.isVisible = getCurrentPlayerState().isHuman() && getActiveCount() > 1

        btnStats.isEnabled = currentRoundNumber > 1

        readyForThrow()
    }

    protected fun updateActivePlayer() {
        hmPlayerNumberToState.values.forEach { it.updateActive(it == getCurrentPlayerState()) }
    }

    private fun initForAi(hasAi: Boolean) {
        btnSlider.isVisible = hasAi

        val defaultSpd = PreferenceUtil.getIntValue(PREFERENCES_INT_AI_SPEED)
        slider.value = defaultSpd
    }

    protected fun makeGameTitle(): String {
        val gameNo = gameEntity.localId
        val gameDesc = gameEntity.getTypeDesc()
        return "Game #$gameNo ($gameDesc - ${getPlayersDesc()})"
    }

    fun loadGame(participants: List<IWrappedParticipant>) {
        val gameId = gameEntity.rowId

        participants.forEachIndexed(::addParticipant)
        finaliseParticipants()

        loadScoresAndCurrentPlayer(gameId)

        // If the game is over, do some extra stuff to sort the screen out
        val dtFinish = gameEntity.dtFinish
        if (!isEndOfTime(dtFinish)) {
            setGameReadOnly()
        } else {
            nextTurn()
        }
    }

    protected open fun setGameReadOnly() {
        dartboard.stopListening()
        scorersOrdered.forEach { it.gameFinished() }

        if (getActiveCount() == 0) {
            btnSlider.isVisible = false
        }

        btnConfirm.isVisible = false
        btnReset.isVisible = false
        btnResign.isVisible = false

        // Default to showing the stats panel for completed games, if applicable
        if (btnStats.isVisible) {
            btnStats.isSelected = true
            viewStats()
        }
    }

    /**
     * Populate the scorers and populate the current player by:
     * - Finding the Max(RoundNumber) for this game
     * - Finding how many players have already completed this round, X.
     * - CurrentPlayerNumber = X % totalPlayers
     */
    private fun loadScoresAndCurrentPlayer(gameId: String) {
        for (i in 0 until totalPlayers) {
            val pt = getParticipant(i)

            val hmRoundToDarts = loadRoundsMap(pt)

            val state = getPlayerState(i)
            hmRoundToDarts.getSortedValues().forEach { state.addLoadedRound(it) }

            loadAdditionalEntities(state)
        }

        val maxRounds = getPlayerStates().maxOf { it.currentRoundNumber() - 1 }
        setCurrentPlayer(maxRounds, gameId)
    }

    private fun loadRoundsMap(pt: IWrappedParticipant): HashMapList<Int, Dart> {
        val individuals = pt.individuals
        val sql =
            """
            SELECT drt.RoundNumber, drt.Score, drt.Multiplier, drt.SegmentType, drt.StartingScore
            FROM Dart drt
            WHERE drt.ParticipantId IN ${individuals.getQuotedIdStr { it.rowId } }
            AND drt.PlayerId IN ${individuals.getQuotedIdStr { it.playerId } }
            ORDER BY drt.RoundNumber, drt.Ordinal
            """

        val hmRoundToDarts = HashMapList<Int, Dart>()

        mainDatabase.executeQuery(sql).use { rs ->
            while (rs.next()) {
                val roundNumber = rs.getInt("RoundNumber")
                val score = rs.getInt("Score")
                val multiplier = rs.getInt("Multiplier")
                val segmentType = SegmentType.valueOf(rs.getString("SegmentType"))
                val startingScore = rs.getInt("StartingScore")

                val drt = Dart(score, multiplier, segmentType = segmentType)
                drt.startingScore = startingScore
                drt.roundNumber = roundNumber

                hmRoundToDarts.putInList(roundNumber, drt)
            }
        }

        return hmRoundToDarts
    }

    protected open fun loadAdditionalEntities(state: PlayerState) {}

    /**
     * 1) Get the MAX(Ordinal) of the person who's played the maxRounds, i.e. the last player to
     *    have a turn.
     * 2) Call into getNextPlayer(), which takes into account inactive players.
     */
    private fun setCurrentPlayer(maxRounds: Int, gameId: String) {
        if (maxRounds == 0) {
            // The game literally hasn't started yet. No one has completed a round.
            currentPlayerNumber = 0
            return
        }

        val sb = StringBuilder()

        sb.append("SELECT MAX(pt.Ordinal) ")
        sb.append(" FROM Dart drt, Participant pt")
        sb.append(" WHERE drt.ParticipantId = pt.RowId")
        sb.append(" AND drt.PlayerId = pt.PlayerId")
        sb.append(" AND drt.RoundNumber = ")
        sb.append(maxRounds)
        sb.append(" AND pt.GameId = '")
        sb.append(gameId)
        sb.append("'")

        val lastPlayerNumber = mainDatabase.executeQueryAggregate(sb)
        currentPlayerNumber = getNextPlayerNumber(lastPlayerNumber)
    }

    protected fun allPlayersFinished() {
        if (!gameEntity.isFinished()) {
            gameEntity.dtFinish = getSqlDateNow()
            gameEntity.saveToDatabase()
        }

        setGameReadOnly()
    }

    /**
     * Should I stop throwing?
     *
     * Default behaviour for if window has been closed, with extensible hook (e.g. in X01 where an
     * AI can be paused).
     */
    private fun shouldAiStopThrowing(): Boolean {
        if (!parentWindow.isVisible) {
            return true
        }

        return shouldAIStop()
    }

    protected fun getNextPlayerNumber(currentPlayerNumber: Int): Int {
        if (getActiveCount() == 0) {
            return currentPlayerNumber
        }

        var candidate = (currentPlayerNumber + 1) % totalPlayers
        while (!isActive(candidate)) {
            candidate = (candidate + 1) % totalPlayers
        }

        return candidate
    }

    private fun hasAi() = getParticipants().flatMap { it.individuals }.any { it.isAi() }

    private fun isActive(playerNumber: Int) = getParticipant(playerNumber).participant.isActive()

    fun fireAppearancePreferencesChanged() {
        for (scorer in scorersOrdered) {
            scorer.repaint()
        }
    }

    protected fun handlePlayerFinish(): Int {
        val state = getCurrentPlayerState()
        val finishingPosition = getFinishingPositionFromPlayersRemaining()
        val numberOfDarts = state.getScoreSoFar()
        state.participantFinished(finishingPosition, numberOfDarts)

        updateAchievementsForFinish(getCurrentPlayerState(), finishingPosition, numberOfDarts)

        return finishingPosition
    }

    open fun updateAchievementsForFinish(
        playerState: PlayerState,
        finishingPosition: Int,
        score: Int
    ) {
        if (playerState.hasMultiplePlayers()) {
            if (finishingPosition == 1) {
                val type = getTeamWinAchievementType(gameEntity.gameType)
                playerState.getPlayerIds().forEach { playerId ->
                    AchievementEntity.insertAchievement(type, playerId, gameEntity.rowId, "$score")
                }
            }
        } else {
            val playerId = playerState.lastIndividual().playerId
            if (finishingPosition == 1) {
                val type = getWinAchievementType(gameEntity.gameType)
                AchievementEntity.insertAchievement(type, playerId, gameEntity.rowId, "$score")
            }

            // Update the 'best game' achievement
            val aa = getBestGameAchievement(gameEntity.gameType) ?: return
            val gameParams = aa.gameParams
            if (gameParams == gameEntity.gameParams) {
                AchievementEntity.updateAchievement(
                    aa.achievementType,
                    playerId,
                    gameEntity.rowId,
                    score
                )
            }
        }
    }

    override fun dartThrown(dart: Dart) {
        getCurrentPlayerState().dartThrown(dart)

        // If there are any specific variables we need to update (e.g. current score for X01), do it
        // now
        updateVariablesForDartThrown(dart)

        doAnimations(dart)

        // Display human controls to reset / confirm
        btnReset.isVisible = getCurrentPlayerState().isHuman()
        if (showConfirmButton()) {
            btnConfirm.isVisible = getCurrentPlayerState().isHuman()
        }

        // If we've thrown three or should stop for other reasons (bust in X01), then stop throwing
        if (shouldStopAfterDartThrown()) {
            stopThrowing()
        } else {
            // Fine, just carry on
            readyForThrow()
        }
    }

    private fun doAnimations(dart: Dart) {
        if (dart.multiplier == 0 && shouldAnimateMiss(dart)) {
            doMissAnimation()
        } else if (dart.getTotal() == 50) {
            dartboard.doBull()
        }
    }

    protected open fun shouldAnimateMiss(dart: Dart) = true

    protected open fun doMissAnimation() {
        dartboard.doBadMiss()
    }

    protected fun stopThrowing() {
        if (getCurrentPlayerState().isHuman()) {
            dartboard.stopListening()
        } else {
            Thread.sleep(slider.value.toLong())

            // If we've been told to pause then we're going to do a reset and not save anything
            if (!shouldAiStopThrowing()) {
                runOnEventThread { confirmRound() }
            }
        }
    }

    private fun resignFromGame() {
        val state = getCurrentPlayerState()
        val name = state.wrappedParticipant.getParticipantName()
        val a =
            DialogUtil.showQuestion(
                "Are you sure you want to resign $name from this game? They will not be able to return.",
                parent = parentWindow
            )

        if (a == JOptionPane.NO_OPTION) {
            return
        }

        hideInputButtons()
        dartboard.clearDarts()
        dartboard.stopListening()

        val finishingPosition = getActiveCount()
        state.participantResigned(finishingPosition)
        turnFinished()
    }

    private fun confirmRound() {
        btnConfirm.isVisible = false
        btnReset.isVisible = false

        dartboard.clearDarts()

        saveDartsAndProceed()
    }

    protected fun resetRound() {
        dartboard.clearDarts()
        getCurrentPlayerState().resetRound()

        btnConfirm.isVisible = false
        btnReset.isVisible = false

        // Might need to re-enable the dartboard for listening if we're a human player
        if (getCurrentPlayerState().isHuman()) {
            dartboard.ensureListening()
        }
    }

    /** Commit round to current player state and the database */
    protected fun commitRound() {
        getCurrentPlayerState().commitRound()
    }

    open fun readyForThrow() {
        if (getCurrentPlayerState().isHuman()) {
            // Human player
            dartboard.ensureListening()
        } else {
            // AI
            dartboard.stopListening()

            cpuThread = Thread(DelayedOpponentTurn(), "Cpu-Thread-" + gameEntity.localId)
            cpuThread!!.start()
        }
    }

    protected open fun showConfirmButton() = true

    override fun actionPerformed(arg0: ActionEvent) {
        when (arg0.source) {
            btnReset -> {
                resetRound()
                readyForThrow()
            }
            btnResign -> resignFromGame()
            btnConfirm -> confirmRound()
            btnStats -> viewStats()
            btnSlider -> toggleSlider()
        }
    }

    private fun toggleSlider() {
        slider.isVisible = btnSlider.isSelected
    }

    private fun viewStats() {
        if (btnStats.isSelected) {
            panelCenter.remove(dartboard)
            panelCenter.add(statsPanel, BorderLayout.CENTER)

            statsPanel.showStats(getPlayerStates())
        } else {
            panelCenter.remove(statsPanel)
            panelCenter.add(dartboard, BorderLayout.CENTER)
        }

        panelCenter.revalidate()
        panelCenter.repaint()
    }

    private fun addParticipant(ordinal: Int, wrappedPt: IWrappedParticipant) {
        val scorer = assignScorer(wrappedPt)
        val state = factoryState(wrappedPt)
        state.addListener(scorer)
        addState(ordinal, state, scorer)

        runForMatch { it.addParticipant(gameEntity.localId, state) }
    }

    private fun finaliseParticipants() {
        finaliseScorers(parentWindow)
        initForAi(hasAi())

        if (gameEntity.matchOrdinal == 1) {
            runForMatch { it.finaliseParticipants() }
        }
    }

    private fun runForMatch(fn: (matchScreen: DartsMatchScreen<PlayerState>) -> Unit) {
        if (parentWindow is DartsMatchScreen<*>) {
            @Suppress("UNCHECKED_CAST") fn(parentWindow as DartsMatchScreen<PlayerState>)
        }
    }

    fun achievementUnlocked(playerId: String, achievement: AbstractAchievement) {
        scorersOrdered
            .find { it.playerIds.contains(playerId) }
            ?.achievementUnlocked(achievement, playerId)
    }

    fun hideInputButtons() {
        btnResign.isVisible = false
        btnConfirm.isVisible = false
        btnReset.isVisible = false
    }

    internal inner class DelayedOpponentTurn : Runnable {
        override fun run() {
            Thread.sleep(slider.value.toLong())

            if (shouldAiStopThrowing()) {
                return
            }

            val model = getCurrentPlayerStrategy()
            val pt = computeAiDart(model)

            pt?.let { dartboard.dartThrown(pt) }
        }
    }

    companion object {
        fun factory(parent: AbstractDartsGameScreen, game: GameEntity, totalPlayers: Int) =
            when (game.gameType) {
                GameType.X01 -> GamePanelX01(parent, game, totalPlayers)
                GameType.GOLF -> GamePanelGolf(parent, game, totalPlayers)
                GameType.ROUND_THE_CLOCK -> GamePanelRoundTheClock(parent, game, totalPlayers)
                GameType.DARTZEE -> constructGamePanelDartzee(parent, game, totalPlayers)
            }

        fun constructGamePanelDartzee(
            parent: AbstractDartsGameScreen,
            game: GameEntity,
            totalPlayers: Int
        ): GamePanelDartzee {
            val dtos = DartzeeRuleEntity().retrieveForGame(game.rowId).map { it.toDto() }
            val summaryPanel = DartzeeRuleSummaryPanel(DartzeeRuleCarousel(dtos))

            return GamePanelDartzee(parent, game, totalPlayers, dtos, summaryPanel)
        }
    }
}
