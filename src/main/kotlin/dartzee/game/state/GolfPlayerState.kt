package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.db.ParticipantEntity

data class GolfPlayerState(override val pt: ParticipantEntity,
                           override val completedRounds: MutableList<List<Dart>> = mutableListOf(),
                           override val currentRound: MutableList<Dart> = mutableListOf()): AbstractPlayerState<GolfPlayerState>()
{
    override fun getScoreSoFar() = getCumulativeScoreForRound(completedRounds.size)

    fun getCumulativeScoreForRound(roundNumber: Int) = (1..roundNumber).map(::getScoreForRound).sum()

    fun getScoreForRound(roundNumber: Int) = completedRounds[roundNumber - 1].last().getGolfScore()
}