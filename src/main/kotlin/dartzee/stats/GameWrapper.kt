package dartzee.stats

import dartzee.`object`.Dart
import dartzee.core.obj.HashMapList
import dartzee.db.DartEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.game.state.GolfPlayerState
import dartzee.screen.game.scorer.DartsScorerGolf
import dartzee.screen.stats.player.HoleBreakdownWrapper
import dartzee.utils.calculateThreeDartAverage
import dartzee.utils.getScoringDarts
import dartzee.utils.getSortedDartStr
import dartzee.utils.sumScore
import java.sql.Timestamp


const val MODE_FRONT_9 = 0
const val MODE_BACK_9 = 1
const val MODE_FULL_18 = 2

/**
 * Wraps up an entire game of darts from a single player's perspective
 */
class GameWrapper(val localId: Long, val gameParams: String, val dtStart: Timestamp, val dtFinish: Timestamp, val finalScore: Int)
{
    private var hmRoundNumberToDarts = HashMapList<Int, Dart>()
    private var totalRounds = 0

    /**
     * Helpers
     */
    fun getAllDarts() = hmRoundNumberToDarts.getFlattenedValuesSortedByKey()

    fun isFinished() = finalScore > -1

    private fun getScoreForFinalRound() = getScoreForRound(totalRounds)
    fun getDartsForFinalRound() = getDartsForRound(totalRounds)

    /**
     * X01 Helpers
     */
    //For unfinished games, return -1 so they're sorted to the back
    fun getCheckoutTotal() = if (finalScore == -1) -1 else getScoreForFinalRound()
    fun getGameStartValueX01() = gameParams.toInt()

    private fun getAllDartsFlattened(): MutableList<Dart>
    {
        return hmRoundNumberToDarts.getAllValues()
    }

    private fun getScoringDartsGroupedByRound(scoreCutOff: Int): MutableList<List<Dart>>
    {
        if (scoreCutOff < 62)
        {
            throw Exception("Calculating scoring darts with cutoff that makes busts possible: $scoreCutOff")
        }

        val allDartsInRounds = mutableListOf<List<Dart>>()

        var score = Integer.parseInt(gameParams)
        for (i in 1..totalRounds)
        {
            val drts = getDartsForRound(i)
            for (j in drts.indices)
            {
                val dart = drts[j]

                if (j == drts.size - 1)
                {
                    //We've got a full round, add this before we return
                    allDartsInRounds.add(drts)
                }

                score -= dart.getTotal()
                if (score <= scoreCutOff)
                {
                    return allDartsInRounds
                }
            }
        }

        if (isFinished())
        {
            throw Exception("Unable to calculate scoring darts for finished game $localId. Score never went below threshold: $scoreCutOff")
        }

        //If we get to here, it's an unfinished game that never went below the threshold. THat's fine - just return what we've got.
        return allDartsInRounds
    }


    fun addDart(roundNumber: Int, dart: Dart)
    {
        if (roundNumber > totalRounds)
        {
            totalRounds = roundNumber
        }

        hmRoundNumberToDarts.putInList(roundNumber, dart)
    }

    private fun getDartsForRound(roundNumber: Int): List<Dart>
    {
        return hmRoundNumberToDarts[roundNumber] ?: emptyList()
    }

    private fun getScoreForRound(roundNumber: Int): Int
    {
        val darts = getDartsForRound(roundNumber)

        return sumScore(darts)
    }

    /**
     * Calculate the 3-dart average, only counting the darts that were thrown up to a certain point.
     * N.B: This method does NOT handle 'busts' when considering whether you've gone below the score threshold.
     * Therefore, the smallest threshold that should ever be passed in is 62.
     */
    fun getThreeDartAverage(scoreCutOff: Int): Double
    {
        val darts = getAllDartsFlattened()
        if (darts.isEmpty())
        {
            return -1.0
        }

        return calculateThreeDartAverage(darts, scoreCutOff)
    }

    fun getScoringDarts(scoreCutOff: Int): MutableList<Dart>
    {
        val allDarts = getAllDartsFlattened()
        return getScoringDarts(allDarts, scoreCutOff)
    }

    /**
     * Burlton Constant
     */
    fun populateThreeDartScoreMap(hmScoreToBreakdownWrapper: MutableMap<Int, ThreeDartScoreWrapper>, scoreThreshold: Int)
    {
        val dartsRounds = getScoringDartsGroupedByRound(scoreThreshold)

        for (i in dartsRounds.indices)
        {
            val dartsForRound = dartsRounds[i]
            val score = sumScore(dartsForRound)

            var wrapper: ThreeDartScoreWrapper? = hmScoreToBreakdownWrapper[score]
            if (wrapper == null)
            {
                wrapper = ThreeDartScoreWrapper()
                hmScoreToBreakdownWrapper[score] = wrapper
            }

            val dartStr = getSortedDartStr(dartsForRound)
            wrapper.addDartStr(dartStr, localId)
        }
    }



    /**
     * Golf Helpers
     */
    private fun getScoreForHole(hole: Int): Int
    {
        val darts = getDartsForRound(hole)
        val scoringDart = darts.last()
        return scoringDart.getGolfScore(hole)
    }

    fun updateHoleBreakdowns(hm: MutableMap<Int, HoleBreakdownWrapper>)
    {
        var overallBreakdown: HoleBreakdownWrapper? = hm[-1]
        if (overallBreakdown == null)
        {
            overallBreakdown = HoleBreakdownWrapper()
            hm[-1] = overallBreakdown
        }

        for (i in 1..totalRounds)
        {
            var wrapper: HoleBreakdownWrapper? = hm[i]
            if (wrapper == null)
            {
                wrapper = HoleBreakdownWrapper()
                hm[i] = wrapper
            }

            val score = getScoreForHole(i)
            wrapper.increment(score)

            //Increment an overall one
            overallBreakdown.increment(score)
        }
    }

    /**
     * Get the overall score for front 9, back 9 or the whole lot
     */
    fun getRoundScore(mode: Int): Int
    {
        val startHole = getStartHoleForMode(mode)
        val endHole = getEndHoleForMode(mode)

        return getScore(startHole, endHole)
    }

    private fun getScore(startHole: Int, finishHole: Int): Int
    {
        if (totalRounds < finishHole)
        {
            //We haven't completed all the necessary rounds
            return -1
        }

        var total = 0
        for (i in startHole..finishHole)
        {
            total += getScoreForHole(i)
        }

        return total
    }

    fun populateScorer(scorer: DartsScorerGolf, mode: Int)
    {
        val startHole = getStartHoleForMode(mode)
        val endHole = getEndHoleForMode(mode)

        val rounds = (startHole..endHole).map(::getDartsForRound).toMutableList()
        val state = GolfPlayerState(ParticipantEntity(), rounds)
        scorer.stateChanged(state)
    }

    private fun getStartHoleForMode(mode: Int): Int
    {
        return if (mode == MODE_BACK_9)
        {
            10
        } else 1

    }

    private fun getEndHoleForMode(mode: Int): Int
    {
        return if (mode == MODE_FRONT_9)
        {
            9
        } else 18

    }

    fun populateOptimalScorecardMaps(hmHoleToBestDarts: MutableMap<Int, List<Dart>>, hmHoleToBestGameId: MutableMap<Int, Long>)
    {
        for (i in 1..totalRounds)
        {
            val darts = getDartsForRound(i)
            val currentDarts = hmHoleToBestDarts[i]

            if (isBetterGolfRound(i, darts, currentDarts))
            {
                hmHoleToBestDarts[i] = darts
                hmHoleToBestGameId[i] = localId
            }
        }
    }

    private fun isBetterGolfRound(hole: Int, dartsNew: List<Dart>, dartsCurrent: List<Dart>?): Boolean
    {
        if (dartsCurrent == null)
        {
            return true
        }

        var lastDart = dartsNew.last()
        val scoreNew = lastDart.getGolfScore(hole)

        lastDart = dartsCurrent.last()
        val scoreCurrent = lastDart.getGolfScore(hole)

        //If the new score is strictly less, then it's better
        if (scoreNew < scoreCurrent)
        {
            return true
        }

        if (scoreNew > scoreCurrent)
        {
            return false
        }

        //Equal scores, so go on number of darts thrown. Less is better.
        val newSize = dartsNew.size
        val currentSize = dartsCurrent.size
        return newSize < currentSize
    }

    /**
     * RTC Helpers
     */
    fun getRangeByTarget(ranges: List<IntRange>): Map<Int, IntRange>
    {
        return getAllDarts().groupBy{ it.startingScore }
                            .mapValues{ it.value.size }
                            .mapValues{ e -> ranges.find{ it.contains(e.value) }!! }
    }

    /**
     * These are normally calculated when adding darts retrieved from the DB.
     * Need direct setters if we're from a simulation
     */
    fun setTotalRounds(totalRounds: Int)
    {
        this.totalRounds = totalRounds
    }

    fun setHmRoundNumberToDartsThrown(hmRoundNumberToDarts: HashMapList<Int, Dart>)
    {
        this.hmRoundNumberToDarts = hmRoundNumberToDarts
    }

    var gameEntity: GameEntity? = null
    var participantEntity: ParticipantEntity? = null
    val dartEntities = mutableListOf<DartEntity>()

    fun clearEntities()
    {
        gameEntity = null
        participantEntity = null
        dartEntities.clear()
    }

    fun generateRealEntities(gameType: GameType, player: PlayerEntity)
    {
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

        for (i in 1..totalRounds)
        {
            val darts = hmRoundNumberToDarts[i]!!

            darts.forEachIndexed { ix, drt ->
                val de = DartEntity.factory(drt, player.rowId, pt.rowId, i, ix+1)
                dartEntities.add(de)
            }
        }
    }
}
