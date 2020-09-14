package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.db.ParticipantEntity

data class GolfPlayerState(override val pt: ParticipantEntity,
                           override var lastRoundNumber: Int = 0,
                           override val darts: MutableList<List<Dart>> = mutableListOf(),
                           override val dartsThrown: MutableList<Dart> = mutableListOf()): AbstractPlayerState()
{
    override fun getScoreSoFar() = -1
}