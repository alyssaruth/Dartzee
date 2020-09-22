package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.db.ParticipantEntity
import dartzee.utils.isBust
import dartzee.utils.isFinishRound
import dartzee.utils.isNearMissDouble
import dartzee.utils.sumScore

data class X01PlayerState(val startingScore: Int,
                          override val pt: ParticipantEntity,
                          override val completedRounds: MutableList<List<Dart>> = mutableListOf(),
                          override val currentRound: MutableList<Dart> = mutableListOf()): AbstractPlayerState<X01PlayerState>()
{
    override fun getScoreSoFar(): Int
    {
        val hasFinished = completedRounds.isNotEmpty() && isFinishRound(completedRounds.last())
        if (!hasFinished)
        {
            return (completedRounds.size * 3) + currentRound.size
        }
        else
        {
            val lastRound = completedRounds.last()
            val earlierRounds = completedRounds.subList(0, completedRounds.size - 1)
            return (earlierRounds.size * 3) + lastRound.size
        }
    }

    fun getRemainingScoreForRound(roundNumber: Int): Int
    {
        val allRounds = completedRounds + listOf(currentRound.toList())
        val roundSubSet = allRounds.subList(0, roundNumber)

        val nonBustRounds = roundSubSet.filterNot { round ->
            val lastDart = round.lastOrNull()
            lastDart?.let(::isBust) ?: false
        }

        return startingScore - nonBustRounds.sumBy { sumScore(it) }
    }

    fun getRemainingScore() = getRemainingScoreForRound(currentRoundNumber())

    fun getBadLuckCount() = getAllDartsFlattened().count { isNearMissDouble(it) }

    fun getLastRound() = completedRounds.last()

    fun currentRoundIsComplete() = currentRound.size == 3 || getRemainingScore() <= 1

    override fun dartThrown(dart: Dart)
    {
        dart.startingScore = getRemainingScore()
        super.dartThrown(dart)
    }
}