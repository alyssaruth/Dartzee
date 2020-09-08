package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.db.BulkInserter
import dartzee.db.DartEntity
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.ParticipantEntity
import dartzee.screen.game.scorer.DartsScorer
import dartzee.screen.game.scorer.DartsScorerDartzee

sealed class AbstractPlayerState<S: DartsScorer>
{
    abstract val pt: ParticipantEntity
    abstract val scorer: S
    abstract var lastRoundNumber: Int
    abstract val darts: MutableList<List<Dart>>
    abstract val dartsThrown: MutableList<Dart>

    fun dartThrown(dart: Dart)
    {
        dart.participantId = pt.rowId
        dartsThrown.add(dart)
    }

    fun resetRound() = dartsThrown.clear()

    fun commitRound()
    {
        val entities = dartsThrown.mapIndexed { ix, drt ->
            DartEntity.factory(drt, pt.playerId, pt.rowId, lastRoundNumber, ix + 1)
        }

        BulkInserter.insert(entities)

        addDarts(dartsThrown.toList())
        dartsThrown.clear()
    }

    fun addDarts(darts: List<Dart>)
    {
        darts.forEach { it.participantId = pt.rowId }
        this.darts.add(darts.toList())
    }
}

data class DefaultPlayerState<S: DartsScorer>(override val pt: ParticipantEntity,
                                              override val scorer: S,
                                              override var lastRoundNumber: Int = 0,
                                              override val darts: MutableList<List<Dart>> = mutableListOf(),
                                              override val dartsThrown: MutableList<Dart> = mutableListOf()): AbstractPlayerState<S>()

data class DartzeePlayerState(override val pt: ParticipantEntity,
                              override val scorer: DartsScorerDartzee,
                              override var lastRoundNumber: Int = 0,
                              override val darts: MutableList<List<Dart>> = mutableListOf(),
                              override val dartsThrown: MutableList<Dart> = mutableListOf(),
                              val roundResults: MutableList<DartzeeRoundResultEntity> = mutableListOf()): AbstractPlayerState<DartsScorerDartzee>()
{
    fun addRoundResult(result: DartzeeRoundResultEntity)
    {
        roundResults.add(result)
    }
}