package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.db.BulkInserter
import dartzee.db.DartEntity
import dartzee.db.ParticipantEntity

abstract class AbstractPlayerState
{
    abstract val pt: ParticipantEntity
    abstract var lastRoundNumber: Int
    abstract val darts: MutableList<List<Dart>>
    abstract val dartsThrown: MutableList<Dart>

    abstract fun getScoreSoFar(): Int

    fun isHuman() = !pt.isAi()

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



