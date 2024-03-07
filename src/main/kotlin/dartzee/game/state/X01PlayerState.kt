package dartzee.game.state

import dartzee.game.X01Config
import dartzee.`object`.Dart
import dartzee.utils.isBust
import dartzee.utils.isFinishRound
import dartzee.utils.isNearMissDouble
import dartzee.utils.sumScore

data class X01PlayerState(
    private val config: X01Config,
    override val wrappedParticipant: IWrappedParticipant,
    override val completedRounds: MutableList<List<Dart>> = mutableListOf(),
    override val currentRound: MutableList<Dart> = mutableListOf(),
    override var isActive: Boolean = false
) : AbstractPlayerState<X01PlayerState>() {
    override fun getScoreSoFar(): Int {
        val hasFinished =
            completedRounds.isNotEmpty() && isFinishRound(completedRounds.last(), config.finishType)
        if (!hasFinished) {
            return (completedRounds.size * 3) + currentRound.size
        } else {
            val lastRound = completedRounds.last()
            val earlierRounds = completedRounds.subList(0, completedRounds.size - 1)
            return (earlierRounds.size * 3) + lastRound.size
        }
    }

    fun getRemainingScoreForRound(roundNumber: Int): Int {
        val lastCompleted =
            if (roundNumber == currentRoundNumber()) roundNumber - 1 else roundNumber
        val roundSubSet = completedRounds.subList(0, lastCompleted)

        val nonBustRounds =
            roundSubSet
                .filterNot { round ->
                    val lastDart = round.lastOrNull()
                    lastDart?.let { isBust(it, config.finishType) } ?: false
                }
                .toMutableList()

        if (roundNumber == currentRoundNumber()) {
            nonBustRounds.add(currentRound.toList())
        }

        return config.target - nonBustRounds.sumOf { sumScore(it) }
    }

    fun getRemainingScore() = getRemainingScoreForRound(currentRoundNumber())

    fun getBadLuckCount(): Int {
        val rounds = getRoundsForIndividual(currentIndividual())
        return rounds.flatten().count { isNearMissDouble(it) }
    }

    fun getLastRound() = completedRounds.last()

    fun isCurrentRoundComplete() =
        currentRound.size == 3 ||
            getRemainingScore() <= 0 ||
            currentRound.lastOrNull()?.let { isBust(it, config.finishType) } ?: false

    override fun dartThrown(dart: Dart) {
        dart.startingScore = getRemainingScore()
        super.dartThrown(dart)
    }
}
