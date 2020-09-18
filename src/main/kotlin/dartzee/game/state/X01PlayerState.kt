package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.db.ParticipantEntity
import dartzee.utils.isBust
import dartzee.utils.isFinishRound
import dartzee.utils.isNearMissDouble
import dartzee.utils.sumScore

data class X01PlayerState(override val pt: ParticipantEntity,
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

    fun getRemainingScoreForRound(startingScore: Int, roundNumber: Int): Int
    {
        val roundSubSet = completedRounds.subList(0, roundNumber)

        val nonBustRounds = roundSubSet.filterNot { round ->
            val lastDart = round.last()
            isBust(lastDart)
        }

        return startingScore - nonBustRounds.sumBy { sumScore(it) }
    }

    fun getRemainingScore(startingScore: Int) = getRemainingScoreForRound(startingScore, currentRoundNumber() - 1)

    fun getBadLuckCount() = getAllDartsFlattened().count { isNearMissDouble(it) }

    fun getLastRound() = completedRounds.last()
}