package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.db.ParticipantEntity
import dartzee.utils.isBust
import dartzee.utils.isFinishRound
import dartzee.utils.isNearMissDouble
import dartzee.utils.sumScore

data class X01PlayerState(override val pt: ParticipantEntity,
                          override val darts: MutableList<List<Dart>> = mutableListOf(),
                          override val dartsThrown: MutableList<Dart> = mutableListOf()): AbstractPlayerState()
{
    override fun getScoreSoFar(): Int
    {
        val hasFinished = darts.isNotEmpty() && isFinishRound(darts.last())
        if (!hasFinished)
        {
            return (darts.size * 3) + dartsThrown.size
        }
        else
        {
            val lastRound = darts.last()
            val earlierRounds = darts.subList(0, darts.size - 1)
            return (earlierRounds.size * 3) + lastRound.size
        }

    }

    fun getRemainingScoreForRound(startingScore: Int, roundNumber: Int): Int
    {
        val roundSubSet = darts.subList(0, roundNumber)

        val nonBustRounds = roundSubSet.filterNot { round ->
            val lastDart = round.last()
            isBust(lastDart)
        }

        return startingScore - nonBustRounds.sumBy { sumScore(it) }
    }

    fun getRemainingScore(startingScore: Int) = getRemainingScoreForRound(startingScore, currentRoundNumber() - 1)

    fun getBadLuckCount() = getAllDartsFlattened().count { isNearMissDouble(it) }

    fun getLastRound() = darts.last()
}