package dartzee.screen.game

import dartzee.`object`.Dart
import dartzee.achievements.AbstractAchievement
import dartzee.achievements.getBestGameAchievement
import dartzee.achievements.getWinAchievementRef
import dartzee.ai.AbstractDartsModel
import dartzee.bean.SliderAiSpeed
import dartzee.core.obj.HashMapList
import dartzee.core.util.*
import dartzee.db.*
import dartzee.game.state.AbstractPlayerState
import dartzee.listener.DartboardListener
import dartzee.screen.Dartboard
import dartzee.screen.game.dartzee.DartzeeRuleCarousel
import dartzee.screen.game.dartzee.DartzeeRuleSummaryPanel
import dartzee.screen.game.dartzee.GamePanelDartzee
import dartzee.screen.game.golf.GamePanelGolf
import dartzee.screen.game.rtc.GamePanelRoundTheClock
import dartzee.screen.game.scorer.DartsScorer
import dartzee.screen.game.x01.GamePanelX01
import dartzee.stats.PlayerSummaryStats
import dartzee.utils.DatabaseUtil
import dartzee.utils.PREFERENCES_INT_AI_SPEED
import dartzee.utils.PreferenceUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Point
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.sql.SQLException
import java.util.*
import javax.swing.*

abstract class DartsGamePanel<S : DartsScorer, D: Dartboard, PlayerState: AbstractPlayerState<S>>(protected val parentWindow: AbstractDartsGameScreen, val gameEntity: GameEntity) :
        PanelWithScorers<S>(),
        DartboardListener,
        ActionListener,
        MouseListener
{
    private val hmPlayerNumberToState = mutableMapOf<Int, PlayerState>()

    protected var totalPlayers = -1

    var gameTitle = ""

    //Transitive things
    var currentPlayerNumber = 0
    var activeScorer: S = factoryScorer()
    protected var dartsThrown = ArrayList<Dart>()
    protected var currentRoundNumber = -1

    //For AI turns
    protected var cpuThread: Thread? = null

    /**
     * Screen stuff
     */
    val dartboard = factoryDartboard()
    protected val statsPanel: GameStatisticsPanel? = factoryStatsPanel()

    private val panelSouth = JPanel()
    protected val slider = SliderAiSpeed(true)
    private val panelButtons = JPanel()
    val btnConfirm = JButton("")
    val btnReset = JButton("")
    private val btnStats = JToggleButton("")
    private val btnSlider = JToggleButton("")

    private fun getLastRoundNumber() =  getCurrentPlayerState().lastRoundNumber
    private fun getPlayersDesc() = if (totalPlayers == 1) "practice game" else "$totalPlayers players"
    protected fun getActiveCount() = getParticipants().count{ it.isActive() }

    fun getGameId() = gameEntity.rowId

    open fun getFinishingPositionFromPlayersRemaining(): Int
    {
        if (totalPlayers == 1)
        {
            return -1
        }

        return totalPlayers - getActiveCount() + 1
    }

    protected fun getCurrentPlayerStrategy(): AbstractDartsModel?
    {
        val participant = getCurrentParticipant()
        if (!participant.isAi())
        {
            Debug.stackTrace("Trying to get current strategy for human player: $participant")
            return null
        }

        return participant.getModel()
    }

    /**
     * Stuff that will ultimately get refactored off into a GameState thingy
     */
    fun getPlayerStates() = hmPlayerNumberToState.getSortedValues()
    protected fun getParticipants() = hmPlayerNumberToState.entries.sortedBy { it.key }.map { it.value.pt }
    protected fun getCurrentPlayerId() = getCurrentParticipant().playerId
    private fun getCurrentPlayerState() = getPlayerState(currentPlayerNumber)
    private fun getPlayerState(playerNumber: Int) = hmPlayerNumberToState[playerNumber]!!
    protected fun getParticipant(playerNumber: Int) = getPlayerState(playerNumber).pt
    protected fun getCurrentParticipant() = getCurrentPlayerState().pt
    protected fun updateLastRoundNumber(playerNumber: Int, newRoundNumber: Int) {
        val state = getPlayerState(playerNumber)
        state.lastRoundNumber = newRoundNumber
    }

    protected fun addState(playerNumber: Int, state: PlayerState) {
        hmPlayerNumberToState[playerNumber] = state
    }

    protected fun getCurrentScorer() = getCurrentPlayerState().scorer
    protected fun getScorer(playerNumber: Int) = getPlayerState(playerNumber).scorer
    protected fun getPlayerNumberForScorer(scorer: S): Int = hmPlayerNumberToState.filter { it.value.scorer == scorer }.keys.first()


    init
    {
        panelCenter.add(dartboard, BorderLayout.CENTER)
        dartboard.addDartboardListener(this)
        panelCenter.add(panelSouth, BorderLayout.SOUTH)
        panelSouth.layout = BorderLayout(0, 0)
        slider.value = 1000
        slider.size = Dimension(100, 200)
        slider.preferredSize = Dimension(40, 200)
        panelSouth.add(panelButtons, BorderLayout.SOUTH)
        btnConfirm.preferredSize = Dimension(80, 80)
        btnConfirm.icon = ImageIcon(javaClass.getResource("/buttons/Confirm.png"))
        btnConfirm.toolTipText = "Confirm round"
        panelButtons.add(btnConfirm)
        btnReset.preferredSize = Dimension(80, 80)
        btnReset.icon = ImageIcon(javaClass.getResource("/buttons/Reset.png"))
        btnReset.toolTipText = "Reset round"
        panelButtons.add(btnReset)
        btnStats.toolTipText = "View stats"
        btnStats.preferredSize = Dimension(80, 80)
        btnStats.icon = ImageIcon(javaClass.getResource("/buttons/stats_large.png"))

        panelButtons.add(btnStats)
        btnSlider.icon = ImageIcon(javaClass.getResource("/buttons/aiSpeed.png"))
        btnSlider.toolTipText = "AI throw speed"
        btnSlider.preferredSize = Dimension(80, 80)

        slider.orientation = SwingConstants.VERTICAL
        slider.isVisible = false

        panelButtons.add(btnSlider)

        btnConfirm.addActionListener(this)
        btnReset.addActionListener(this)
        btnStats.addActionListener(this)
        btnSlider.addActionListener(this)

        addMouseListener(this)

        if (statsPanel == null)
        {
            btnStats.isVisible = false
        }

        dartboard.renderScoreLabels = true
    }


    /**
     * Abstract methods
     */
    abstract fun factoryState(pt: ParticipantEntity, scorer: S): PlayerState
    abstract fun doAiTurn(model: AbstractDartsModel)

    abstract fun loadDartsForParticipant(playerNumber: Int, hmRoundToDarts: HashMapList<Int, Dart>, totalRounds: Int)
    abstract fun updateVariablesForNewRound()
    abstract fun resetRoundVariables()
    abstract fun updateVariablesForDartThrown(dart: Dart)
    abstract fun shouldStopAfterDartThrown(): Boolean
    abstract fun shouldAIStop(): Boolean
    abstract fun saveDartsAndProceed()
    abstract fun factoryStatsPanel(): GameStatisticsPanel?
    abstract fun factoryDartboard(): D

    /**
     * Regular methods
     */
    fun startNewGame(players: List<PlayerEntity>)
    {
        players.forEachIndexed { ix, player ->
            val gameId = gameEntity.rowId
            val participant = ParticipantEntity.factoryAndSave(gameId, player, ix)
            addParticipant(participant)

            val scorer = assignScorer(player, gameEntity.gameParams)
            addState(ix, factoryState(participant, scorer))
        }

        initForAi(hasAi())
        dartboard.paintDartboardCached()

        nextTurn()
    }

    protected fun nextTurn()
    {
        activeScorer = getCurrentScorer()
        selectScorer(activeScorer)

        dartsThrown.clear()

        updateVariablesForNewRound()

        val lastRoundForThisPlayer = getLastRoundNumber()

        //Create a new round for this player
        val newRoundNo = lastRoundForThisPlayer + 1
        currentRoundNumber = newRoundNo
        updateLastRoundNumber(currentPlayerNumber, newRoundNo)

        Debug.appendBanner(activeScorer.playerName + ": Round " + newRoundNo, VERBOSE_LOGGING)

        btnReset.isEnabled = false
        btnConfirm.isEnabled = false

        btnStats.isEnabled = newRoundNo > 1

        readyForThrow()
    }

    private fun selectScorer(selectedScorer: S?)
    {
        for (scorer in scorersOrdered)
        {
            scorer.setSelected(false)
        }

        selectedScorer!!.setSelected(true)
    }

    private fun initForAi(hasAi: Boolean)
    {
        dartboard.addOverlay(Point(329, 350), slider)
        btnSlider.isVisible = hasAi

        val defaultSpd = PreferenceUtil.getIntValue(PREFERENCES_INT_AI_SPEED)
        slider.value = defaultSpd
    }


    fun initBasic(totalPlayers: Int)
    {
        this.totalPlayers = totalPlayers

        val gameNo = gameEntity.localId
        val gameDesc = gameEntity.getTypeDesc()
        gameTitle = "Game #$gameNo ($gameDesc - ${getPlayersDesc()})"

        if (statsPanel != null)
        {
            statsPanel.gameParams = gameEntity.gameParams
        }

        initScorers(totalPlayers)
    }

    fun loadGameInCatch()
    {
        try
        {
            loadGame()
        }
        catch (t: Throwable)
        {
            Debug.stackTrace(t)
            DialogUtil.showError("Failed to load Game #${gameEntity.localId}")
        }
    }


    fun loadGame()
    {
        val gameId = gameEntity.rowId

        //Get the participants, sorted by Ordinal. Assign their scorers.
        loadParticipants(gameId)
        loadScoresAndCurrentPlayer(gameId)

        //If the game is over, do some extra stuff to sort the screen out
        val dtFinish = gameEntity.dtFinish
        if (!isEndOfTime(dtFinish))
        {
            setGameReadOnly()
        }
        else
        {
            //Paint the dartboard
            dartboard.paintDartboardCached()

            nextTurn()
        }
    }

    protected open fun setGameReadOnly()
    {
        dartboard.stopListening()

        if (getActiveCount() == 0)
        {
            btnSlider.isVisible = false
            btnConfirm.isVisible = false
            btnReset.isVisible = false
        }
        else
        {
            slider.isEnabled = false
            btnConfirm.isEnabled = false
            btnReset.isEnabled = false
        }

        //Default to showing the stats panel for completed games, if applicable
        if (btnStats.isVisible)
        {
            btnStats.isSelected = true
            viewStats()
        }

        updateScorersWithFinishingPositions()
    }

    protected fun updateScorersWithFinishingPositions()
    {
        hmPlayerNumberToState.keys.forEach {
            val state = getPlayerState(it)
            state.scorer.updateResultColourForPosition(state.pt.finishingPosition)
        }
    }

    /**
     * Retrieve the ordered participants and assign their scorers
     */
    private fun loadParticipants(gameId: String)
    {
        val whereSql = "GameId = '$gameId' ORDER BY Ordinal ASC"
        val participants = ParticipantEntity().retrieveEntities(whereSql)

        for (i in participants.indices)
        {
            val pt = participants[i]
            addParticipant(pt)

            val scorer = assignScorer(pt.getPlayer(), gameEntity.gameParams)
            addState(i, factoryState(pt, scorer))
        }

        initForAi(hasAi())
    }

    /**
     * Populate the scorers and populate the current player by:
     *
     * - Finding the Max(RoundNumber) for this game
     * - Finding how many players have already completed this round, X.
     * - CurrentPlayerNumber = X % totalPlayers
     */
    private fun loadScoresAndCurrentPlayer(gameId: String)
    {
        var maxRounds = 0

        for (i in 0 until totalPlayers)
        {
            val pt = getParticipant(i)
            val sql = ("SELECT drt.RoundNumber, drt.Score, drt.Multiplier, drt.PosX, drt.PosY, drt.SegmentType, drt.StartingScore"
                    + " FROM Dart drt"
                    + " WHERE drt.ParticipantId = '" + pt.rowId + "'"
                    + " AND drt.PlayerId = '" + pt.playerId + "'"
                    + " ORDER BY drt.RoundNumber, drt.Ordinal")

            val hmRoundToDarts = HashMapList<Int, Dart>()
            var lastRound = 0

            try
            {
                DatabaseUtil.executeQuery(sql).use { rs ->
                    while (rs.next())
                    {
                        val roundNumber = rs.getInt("RoundNumber")
                        val score = rs.getInt("Score")
                        val multiplier = rs.getInt("Multiplier")
                        val posX = rs.getInt("PosX")
                        val posY = rs.getInt("PosY")
                        val segmentType = rs.getInt("SegmentType")
                        val startingScore = rs.getInt("StartingScore")

                        val drt = Dart(score, multiplier, Point(posX, posY), segmentType)
                        drt.startingScore = startingScore

                        hmRoundToDarts.putInList(roundNumber, drt)

                        lastRound = roundNumber
                    }
                }
            }
            catch (sqle: SQLException)
            {
                Debug.logSqlException(sql, sqle)
                throw sqle
            }

            val state = getPlayerState(i)
            hmRoundToDarts.getSortedValues().forEach {
                state.addDarts(it)
            }

            loadDartsForParticipant(i, hmRoundToDarts, lastRound)

            updateLastRoundNumber(i, lastRound)

            maxRounds = maxOf(maxRounds, lastRound)
        }

        setCurrentPlayer(maxRounds, gameId)
    }

    /**
     * 1) Get the MAX(Ordinal) of the person who's played the maxRounds, i.e. the last player to have a turn.
     * 2) Call into getNextPlayer(), which takes into account inactive players.
     */
    private fun setCurrentPlayer(maxRounds: Int, gameId: String)
    {
        if (maxRounds == 0)
        {
            //The game literally hasn't started yet. No one has completed a round.
            Debug.append("MaxRounds = 0, so setting CurrentPlayerNumber = 0 as game hasn't started.")
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

        val lastPlayerNumber = DatabaseUtil.executeQueryAggregate(sb)
        currentPlayerNumber = getNextPlayerNumber(lastPlayerNumber)

        Debug.append("MaxRounds = $maxRounds, CurrentPlayerNumber = $currentPlayerNumber")
    }

    fun allPlayersFinished()
    {
        Debug.append("All players now finished.", VERBOSE_LOGGING)

        if (!gameEntity.isFinished())
        {
            gameEntity.dtFinish = getSqlDateNow()
            gameEntity.saveToDatabase()
        }

        dartboard.stopListening()

        val participants = getParticipants()
        for (pt in participants)
        {
            val playerId = pt.playerId
            PlayerSummaryStats.resetPlayerStats(playerId, gameEntity.gameType)
        }
    }



    /**
     * Should I stop throwing?
     *
     * Default behaviour for if window has been closed, with extensible hook (e.g. in X01 where an AI can be paused).
     */
    private fun shouldAiStopThrowing(): Boolean
    {
        if (!parentWindow.isVisible)
        {
            Debug.append("Game window has been closed, stopping throwing.")
            return true
        }

        return shouldAIStop()
    }

    protected fun getNextPlayerNumber(currentPlayerNumber: Int): Int
    {
        if (getActiveCount() == 0)
        {
            return currentPlayerNumber
        }

        var candidate = (currentPlayerNumber + 1) % totalPlayers
        while (!isActive(candidate))
        {
            candidate = (candidate + 1) % totalPlayers
        }

        return candidate
    }

    private fun hasAi() = getParticipants().any { it.isAi() }

    private fun isActive(playerNumber: Int) = getParticipant(playerNumber).isActive()

    fun fireAppearancePreferencesChanged()
    {
        for (scorer in scorersOrdered)
        {
            scorer.repaint()
        }
    }

    protected open fun handlePlayerFinish(): Int
    {
        val participant = getCurrentParticipant()

        val finishingPosition = getFinishingPositionFromPlayersRemaining()
        val numberOfDarts = activeScorer.getTotalScore()

        participant.finishingPosition = finishingPosition
        participant.finalScore = numberOfDarts
        participant.dtFinished = getSqlDateNow()
        participant.saveToDatabase()

        val playerId = participant.playerId
        PlayerSummaryStats.resetPlayerStats(playerId, gameEntity.gameType)

        updateAchievementsForFinish(playerId, finishingPosition, numberOfDarts)

        return finishingPosition
    }

    open fun updateAchievementsForFinish(playerId: String, finishingPosition: Int, score: Int)
    {
        if (finishingPosition == 1)
        {
            val achievementRef = getWinAchievementRef(gameEntity.gameType)
            AchievementEntity.incrementAchievement(achievementRef, playerId, gameEntity.rowId, 1)
        }

        //Update the 'best game' achievement
        val aa = getBestGameAchievement(gameEntity.gameType) ?: return
        val gameParams = aa.gameParams
        if (gameParams == gameEntity.gameParams)
        {
            AchievementEntity.updateAchievement(aa.achievementRef, playerId, gameEntity.rowId, score)
        }
    }

    override fun dartThrown(dart: Dart)
    {
        Debug.append("Hit $dart", VERBOSE_LOGGING)

        dartsThrown.add(dart)
        activeScorer.addDart(dart)

        //We've clicked on the dartboard, so dismiss the slider
        if (activeScorer.human)
        {
            dismissSlider()
        }

        //If there are any specific variables we need to update (e.g. current score for X01), do it now
        updateVariablesForDartThrown(dart)

        doAnimations(dart)

        //Enable both of these
        btnReset.isEnabled = activeScorer.human
        if (!mustContinueThrowing())
        {
            btnConfirm.isEnabled = activeScorer.human
        }

        //If we've thrown three or should stop for other reasons (bust in X01), then stop throwing
        if (shouldStopAfterDartThrown())
        {
            stopThrowing()
        }
        else
        {
            //Fine, just carry on
            readyForThrow()
        }
    }

    private fun doAnimations(dart: Dart)
    {
        if (dart.multiplier == 0 && shouldAnimateMiss(dart))
        {
            doMissAnimation()
        }
        else if (dart.getTotal() == 50)
        {
            dartboard.doBull()
        }
    }

    protected open fun shouldAnimateMiss(dart: Dart): Boolean
    {
        return true
    }


    protected open fun doMissAnimation()
    {
        dartboard.doBadMiss()
    }

    protected fun stopThrowing()
    {
        if (activeScorer.human)
        {
            dartboard.stopListening()
        }
        else
        {
            Thread.sleep(slider.value.toLong())

            // If we've been told to pause then we're going to do a reset and not save anything
            if (!shouldAiStopThrowing())
            {
                SwingUtilities.invokeLater { confirmRound() }
            }
        }
    }

    private fun confirmRound()
    {
        btnConfirm.isEnabled = false
        btnReset.isEnabled = false

        dartboard.clearDarts()
        activeScorer.confirmCurrentRound()

        saveDartsAndProceed()
    }

    protected fun resetRound()
    {
        resetRoundVariables()

        dartboard.clearDarts()
        activeScorer.clearRound(currentRoundNumber)
        activeScorer.updatePlayerResult()
        dartsThrown.clear()

        //If we're resetting, disable the buttons
        btnConfirm.isEnabled = false
        btnReset.isEnabled = false

        //Might need to re-enable the dartboard for listening if we're a human player
        val human = activeScorer.human
        dartboard.listen(human)
    }

    /**
     * Loop through the darts thrown, saving them to the database.
     */
    protected fun saveDartsToDatabase()
    {
        val pt = getCurrentParticipant()
        val darts = mutableListOf<DartEntity>()
        for (i in dartsThrown.indices)
        {
            val dart = dartsThrown[i]
            darts.add(DartEntity.factory(dart, pt.playerId, pt.rowId, currentRoundNumber, i + 1, dart.startingScore))
        }

        BulkInserter.insert(darts)

        getCurrentPlayerState().addDarts(dartsThrown)
    }

    open fun readyForThrow()
    {
        if (activeScorer.human)
        {
            //Human player
            dartboard.ensureListening()
        }
        else
        {
            //AI
            dartboard.stopListening()

            cpuThread = Thread(DelayedOpponentTurn(), "Cpu-Thread-" + gameEntity.localId)
            cpuThread!!.start()
        }
    }

    protected open fun mustContinueThrowing() = false

    override fun actionPerformed(arg0: ActionEvent)
    {
        val source = arg0.source
        if (source !== btnSlider)
        {
            btnSlider.isSelected = false
            slider.isVisible = false
        }

        when (source)
        {
            btnReset -> {
                Debug.append("Reset pressed.")
                resetRound()
                readyForThrow()
            }
            btnConfirm -> confirmRound()
            btnStats -> viewStats()
            btnSlider -> toggleSlider()
        }
    }

    private fun toggleSlider()
    {
        slider.isVisible = btnSlider.isSelected

        if (btnStats.isSelected)
        {
            btnStats.isSelected = false
            viewStats()
        }
    }

    private fun viewStats()
    {
        if (btnStats.isSelected)
        {
            panelCenter.remove(dartboard)
            panelCenter.add(statsPanel!!, BorderLayout.CENTER)

            statsPanel.showStats(getPlayerStates())
        }
        else
        {
            panelCenter.remove(statsPanel)
            panelCenter.add(dartboard, BorderLayout.CENTER)

            //We might not have painted it if this is a complete, loaded game
            if (dartboard.dartboardImage == null)
            {
                dartboard.paintDartboardCached()
            }
        }

        panelCenter.revalidate()
        panelCenter.repaint()
    }

    private fun addParticipant(participant: ParticipantEntity)
    {
        if (parentWindow is DartsMatchScreen)
        {
            parentWindow.addParticipant(gameEntity.localId, participant)
        }
    }

    fun achievementUnlocked(playerId: String, achievement: AbstractAchievement)
    {
        scorersOrdered.find { it.playerId === playerId }?.achievementUnlocked(achievement)
    }

    private fun dismissSlider()
    {
        btnSlider.isSelected = false
        toggleSlider()
    }

    fun disableInputButtons()
    {
        btnConfirm.isEnabled = false
        btnReset.isEnabled = false
    }

    /**
     * MouseListener
     */
    override fun mouseClicked(e: MouseEvent)
    {
        if (e.source !== slider)
        {
            dismissSlider()
        }
    }
    override fun mouseEntered(e: MouseEvent){}
    override fun mouseExited(e: MouseEvent){}
    override fun mousePressed(e: MouseEvent){}
    override fun mouseReleased(e: MouseEvent){}

    internal inner class DelayedOpponentTurn : Runnable
    {
        override fun run()
        {
            Thread.sleep(slider.value.toLong())

            if (shouldAiStopThrowing())
            {
                return
            }

            val model = getCurrentPlayerStrategy()!!
            doAiTurn(model)
        }
    }

    companion object
    {
        const val VERBOSE_LOGGING = false

        fun factory(parent: AbstractDartsGameScreen, game: GameEntity): DartsGamePanel<*, *, *>
        {
            return when (game.gameType)
            {
                GAME_TYPE_X01 -> GamePanelX01(parent, game)
                GAME_TYPE_GOLF -> GamePanelGolf(parent, game)
                GAME_TYPE_ROUND_THE_CLOCK -> GamePanelRoundTheClock(
                    parent,
                    game
                )
                GAME_TYPE_DARTZEE -> constructGamePanelDartzee(parent, game)
                else -> GamePanelX01(parent, game)
            }
        }

        private fun constructGamePanelDartzee(parent: AbstractDartsGameScreen, game: GameEntity): GamePanelDartzee
        {
            val dtos = DartzeeRuleEntity().retrieveForGame(game.rowId).map { it.toDto() }
            val summaryPanel = DartzeeRuleSummaryPanel(
                DartzeeRuleCarousel(
                    dtos
                )
            )

            return GamePanelDartzee(parent, game, dtos, summaryPanel)
        }
    }
}
