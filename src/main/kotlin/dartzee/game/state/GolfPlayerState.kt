package dartzee.game.state

import dartzee.`object`.Dart

data class GolfPlayerState(
    override val wrappedParticipant: IWrappedParticipant,
    override val completedRounds: MutableList<List<Dart>> = mutableListOf(),
    override val currentRound: MutableList<Dart> = mutableListOf(),
    override var isActive: Boolean = false
) : AbstractPlayerState<GolfPlayerState>() {
    override fun getScoreSoFar() = getCumulativeScoreForRound(completedRounds.size)

    fun getCumulativeScoreForRound(roundNumber: Int) =
        (1..roundNumber).map(::getScoreForRound).sum()

    fun getScoreForRound(roundNumber: Int) = completedRounds[roundNumber - 1].last().getGolfScore()

    fun countHoleInOnes(): Int {
        val rounds = getRoundsForIndividual(currentIndividual())
        return rounds.count { it.lastOrNull()?.getGolfScore() == 1 }
    }
}
