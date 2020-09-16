package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.db.ParticipantEntity
import dartzee.utils.isBust
import dartzee.utils.isNearMissDouble
import dartzee.utils.sumScore

data class X01PlayerState(override val pt: ParticipantEntity,
                          override var lastRoundNumber: Int = 0,
                          override val darts: MutableList<List<Dart>> = mutableListOf(),
                          override val dartsThrown: MutableList<Dart> = mutableListOf()): AbstractPlayerState()
{
    override fun getScoreSoFar() = (darts.size * 3) + dartsThrown.size

    fun getRemainingScoreForRound(startingScore: Int, roundNumber: Int): Int
    {
        val roundSubSet = darts.subList(0, roundNumber)

        val nonBustRounds = roundSubSet.filterNot { round ->
            val lastDart = round.last()
            isBust(lastDart)
        }

        return startingScore - nonBustRounds.sumBy { sumScore(it) }
    }

    fun getRemainingScore(startingScore: Int) = getRemainingScoreForRound(startingScore, lastRoundNumber)

    fun getBadLuckCount() = getAllDartsFlattened().count { isNearMissDouble(it) }
}