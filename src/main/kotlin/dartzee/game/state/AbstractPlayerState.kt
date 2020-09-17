package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.db.BulkInserter
import dartzee.db.DartEntity
import dartzee.db.ParticipantEntity

abstract class AbstractPlayerState
{
    abstract val pt: ParticipantEntity
    abstract val completedRounds: MutableList<List<Dart>>
    abstract val currentRound: MutableList<Dart>

    abstract fun getScoreSoFar(): Int

    fun currentRoundNumber() = completedRounds.size + 1

    fun getAllDartsFlattened() = completedRounds.flatten() + currentRound

    fun isHuman() = !pt.isAi()

    fun dartThrown(dart: Dart)
    {
        dart.participantId = pt.rowId
        currentRound.add(dart)
    }

    fun resetRound() = currentRound.clear()

    fun commitRound()
    {
        val entities = currentRound.mapIndexed { ix, drt ->
            DartEntity.factory(drt, pt.playerId, pt.rowId, currentRoundNumber(), ix + 1)
        }

        BulkInserter.insert(entities)

        addDarts(currentRound.toList())
        currentRound.clear()
    }

    fun addDarts(darts: List<Dart>)
    {
        darts.forEach { it.participantId = pt.rowId }
        this.completedRounds.add(darts.toList())
    }
}



