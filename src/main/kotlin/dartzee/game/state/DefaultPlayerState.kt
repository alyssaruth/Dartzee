package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.ParticipantEntity
import dartzee.screen.game.scorer.DartsScorer
import dartzee.screen.game.scorer.DartsScorerDartzee

abstract sealed class AbstractPlayerState<S: DartsScorer>
{
    abstract val pt: ParticipantEntity
    abstract val scorer: S
    abstract val lastRoundNumber: Int
    abstract val darts: MutableList<List<Dart>>

    fun addDarts(darts: List<Dart>)
    {
        this.darts.add(darts)
    }
}

data class DefaultPlayerState<S: DartsScorer>(override val pt: ParticipantEntity,
                                              override val scorer: S,
                                              override val lastRoundNumber: Int,
                                              override val darts: MutableList<List<Dart>> = mutableListOf()): AbstractPlayerState<S>()

data class DartzeePlayerState(override val pt: ParticipantEntity,
                              override val scorer: DartsScorerDartzee,
                              override val lastRoundNumber: Int,
                              override val darts: MutableList<List<Dart>> = mutableListOf(),
                              val roundResults: MutableList<DartzeeRoundResultEntity> = mutableListOf()): AbstractPlayerState<DartsScorerDartzee>()