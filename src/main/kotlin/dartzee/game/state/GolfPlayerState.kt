package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.db.ParticipantEntity

data class GolfPlayerState(override val pt: ParticipantEntity,
                           override val darts: MutableList<List<Dart>> = mutableListOf(),
                           override val dartsThrown: MutableList<Dart> = mutableListOf()): AbstractPlayerState()
{
    override fun getScoreSoFar() = darts.sumBy { it.lastOrNull()?.getGolfScore() ?: 0 }
}