package dartzee.stats

import dartzee.core.obj.HashMapList
import dartzee.db.DartEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.game.X01Config
import dartzee.`object`.Dart
import dartzee.screen.stats.player.HoleBreakdownWrapper
import dartzee.screen.stats.player.golf.OptimalHoleStat
import dartzee.utils.calculateThreeDartAverage
import dartzee.utils.getScoringDarts
import dartzee.utils.getScoringRounds
import dartzee.utils.getSortedDartStr
import dartzee.utils.sumScore
import java.sql.Timestamp
import kotlin.math.max

enum class GolfMode {
    FRONT_9,
    BACK_9,
    FULL_18
}

/** Wraps up an entire game of darts from a single player's perspective */
class GameWrapper(
    val localId: Long,
    val gameParams: String,
    val dtStart: Timestamp,
    val dtFinish: Timestamp,
    val finalScore: Int,
    val teamGame: Boolean,
    private var totalRounds: Int = 0,
    private val hmRoundNumberToDarts: HashMapList<Int, Dart> = HashMapList(),
) {
    /** Helpers */
    fun getAllDarts() = hmRoundNumberToDarts.getFlattenedValuesSortedByKey()

    fun isFinished() = finalScore > -1

    private fun getScoreForFinalRound() = getScoreForRound(totalRounds)

    fun getDartsForFinalRound() = getDartsForRound(totalRounds)

    /** X01 Helpers */
    // For unfinished games, return -1 so they're sorted to the back
    fun getCheckoutTotal() = if (finalScore == -1) -1 else getScoreForFinalRound()

    fun getGameStartValueX01() = X01Config.fromJson(gameParams).target

    private fun getAllDartsFlattened() = hmRoundNumberToDarts.getAllValues()

    fun addDart(dart: Dart) {
        totalRounds = max(dart.roundNumber, totalRounds)
        hmRoundNumberToDarts.putInList(dart.roundNumber, dart)
    }

    private fun getDartsForRound(roundNumber: Int) =
        hmRoundNumberToDarts[roundNumber] ?: emptyList()

    private fun getScoreForRound(roundNumber: Int): Int {
        val darts = getDartsForRound(roundNumber)

        return sumScore(darts)
    }

    /**
     * Calculate the 3-dart average, only counting the darts that were thrown up to a certain point.
     * N.B: This method does NOT handle 'busts' when considering whether you've gone below the score
     * threshold. Therefore, the smallest threshold that should ever be passed in is 62.
     */
    fun getThreeDartAverage(scoreCutOff: Int): Double {
        val darts = getAllDartsFlattened()
        if (darts.isEmpty()) {
            return -1.0
        }

        return calculateThreeDartAverage(darts, scoreCutOff)
    }

    fun getScoringDarts(scoreCutOff: Int): List<Dart> {
        val allDarts = getAllDartsFlattened()
        return getScoringDarts(allDarts, scoreCutOff)
    }

    /** Three dart scores */
    fun populateThreeDartScoreMap(
        hmScoreToBreakdownWrapper: MutableMap<Int, ThreeDartScoreWrapper>,
        scoreThreshold: Int
    ) {
        val dartRounds = hmRoundNumberToDarts.values.toList()
        val scoringRounds = getScoringRounds(dartRounds, scoreThreshold)

        scoringRounds.forEach { dartsForRound ->
            val score = sumScore(dartsForRound)

            val wrapper: ThreeDartScoreWrapper =
                hmScoreToBreakdownWrapper.getOrPut(score, ::ThreeDartScoreWrapper)
            val dartStr = getSortedDartStr(dartsForRound)
            wrapper.addDartStr(dartStr, localId)
        }
    }

    /** Golf Helpers */
    private fun getScoreForHole(hole: Int): Int {
        val darts = getDartsForRound(hole)
        val scoringDart = darts.last()
        return scoringDart.getGolfScore(hole)
    }

    fun updateHoleBreakdowns(hm: MutableMap<Int, HoleBreakdownWrapper>) {
        var overallBreakdown: HoleBreakdownWrapper? = hm[-1]
        if (overallBreakdown == null) {
            overallBreakdown = HoleBreakdownWrapper()
            hm[-1] = overallBreakdown
        }

        for (i in 1..totalRounds) {
            var wrapper: HoleBreakdownWrapper? = hm[i]
            if (wrapper == null) {
                wrapper = HoleBreakdownWrapper()
                hm[i] = wrapper
            }

            val score = getScoreForHole(i)
            wrapper.increment(score)

            // Increment an overall one
            overallBreakdown.increment(score)
        }
    }

    /** Get the overall score for front 9, back 9 or the whole lot */
    fun getRoundScore(mode: GolfMode): Int {
        val startHole = getStartHoleForMode(mode)
        val endHole = getEndHoleForMode(mode)

        return getScore(startHole, endHole)
    }

    private fun getScore(startHole: Int, finishHole: Int): Int {
        if (totalRounds < finishHole) {
            // We haven't completed all the necessary rounds
            return -1
        }

        var total = 0
        for (i in startHole..finishHole) {
            total += getScoreForHole(i)
        }

        return total
    }

    fun getGolfRounds(mode: GolfMode): List<List<Dart>> {
        val startHole = getStartHoleForMode(mode)
        val endHole = getEndHoleForMode(mode)

        return (startHole..endHole).map(::getDartsForRound)
    }

    private fun getStartHoleForMode(mode: GolfMode) = if (mode == GolfMode.BACK_9) 10 else 1

    private fun getEndHoleForMode(mode: GolfMode) = if (mode == GolfMode.FRONT_9) 9 else 18

    fun populateOptimalScorecardMaps(hmHoleToOptimalHoleStat: MutableMap<Int, OptimalHoleStat>) {
        for (i in 1..totalRounds) {
            val darts = getDartsForRound(i)
            if (darts.isEmpty()) continue

            val currentValue = hmHoleToOptimalHoleStat.getValue(i)

            if (isBetterGolfRound(i, darts, currentValue.localGameId, currentValue.darts)) {
                hmHoleToOptimalHoleStat[i] = OptimalHoleStat(darts, localId)
            }
        }
    }

    private fun isBetterGolfRound(
        hole: Int,
        dartsNew: List<Dart>,
        currentGameId: Long,
        dartsCurrent: List<Dart>
    ): Boolean {
        if (currentGameId == -1L) {
            return true
        }

        var lastDart = dartsNew.last()
        val scoreNew = lastDart.getGolfScore(hole)

        lastDart = dartsCurrent.last()
        val scoreCurrent = lastDart.getGolfScore(hole)

        // If the new score is strictly less, then it's better
        if (scoreNew < scoreCurrent) {
            return true
        }

        if (scoreNew > scoreCurrent) {
            return false
        }

        // Equal scores, so go on number of darts thrown. Less is better.
        val newSize = dartsNew.size
        val currentSize = dartsCurrent.size
        return newSize < currentSize
    }

    /** RTC Helpers */
    fun getRangeByTarget(ranges: List<IntRange>): Map<Int, IntRange> {
        return getAllDarts()
            .groupBy { it.startingScore }
            .mapValues { it.value.size }
            .mapValues { e -> ranges.find { it.contains(e.value) }!! }
    }

    var gameEntity: GameEntity? = null
    var participantEntity: ParticipantEntity? = null
    val simulationDartEntities = mutableListOf<DartEntity>()

    fun clearEntities() {
        gameEntity = null
        participantEntity = null
        simulationDartEntities.clear()
    }

    fun generateRealEntities(gameType: GameType, player: PlayerEntity) {
        val game = GameEntity()
        game.assignRowId()
        game.gameType = gameType
        game.gameParams = gameParams
        game.dtCreation = dtStart
        game.dtFinish = dtFinish

        gameEntity = game

        val pt = ParticipantEntity()
        pt.assignRowId()
        pt.gameId = game.rowId
        pt.playerId = player.rowId
        pt.finalScore = finalScore
        pt.dtFinished = dtFinish
        pt.ordinal = 1

        participantEntity = pt

        for (i in 1..totalRounds) {
            val darts = hmRoundNumberToDarts[i]!!

            darts.forEachIndexed { ix, drt ->
                val de = DartEntity.factory(drt, player.rowId, pt.rowId, i, ix + 1)
                simulationDartEntities.add(de)
            }
        }
    }
}
