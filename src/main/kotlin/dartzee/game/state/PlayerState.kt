package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.db.ParticipantEntity
import dartzee.screen.game.scorer.DartsScorer

data class PlayerState<S: DartsScorer>(val pt: ParticipantEntity,
                                       val scorer: S,
                                       val lastRoundNumber: Int,
                                       val darts: MutableList<List<Dart>> = mutableListOf())
{
    fun addDarts(darts: List<Dart>)
    {
        this.darts.add(darts)
    }
}