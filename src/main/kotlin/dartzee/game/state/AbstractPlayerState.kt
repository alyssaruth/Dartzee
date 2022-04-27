package dartzee.game.state

import IParticipant
import dartzee.`object`.Dart
import dartzee.core.util.getSqlDateNow
import dartzee.db.BulkInserter
import dartzee.db.DartEntity

abstract class AbstractPlayerState<S: AbstractPlayerState<S>>
{
    private val listeners = mutableListOf<PlayerStateListener<S>>()

    abstract val pt: IParticipant
    abstract val completedRounds: MutableList<List<Dart>>
    abstract val currentRound: MutableList<Dart>
    abstract var isActive: Boolean

    abstract fun getScoreSoFar(): Int

    @Suppress("UNCHECKED_CAST")
    fun addListener(listener: PlayerStateListener<S>)
    {
        listeners.add(listener)
        listener.stateChanged(this as S)
    }

    /**
     * Helpers
     */
    fun currentRoundNumber() = completedRounds.size + 1

    protected fun getAllDartsFlattened() = completedRounds.flatten() + currentRound

    fun isHuman(roundNumber: Int) = !pt.getParticipant(roundNumber).isAi()

    /**
     * Modifiers
     */
    open fun dartThrown(dart: Dart)
    {
        dart.participantId = pt.getParticipant(dart.roundNumber).rowId
        currentRound.add(dart)

        fireStateChanged()
    }

    fun resetRound()
    {
        currentRound.clear()
        fireStateChanged()
    }

    fun commitRound()
    {
        val entities = currentRound.mapIndexed { ix, drt ->
            DartEntity.factory(drt, pt.playerId, pt.rowId, currentRoundNumber(), ix + 1)
        }

        BulkInserter.insert(entities)

        addCompletedRound(currentRound.toList())
        currentRound.clear()

        fireStateChanged()
    }

    open fun addLoadedRound(darts: List<Dart>)
    {
        addCompletedRound(darts)
    }

    fun addCompletedRound(darts: List<Dart>)
    {
        darts.forEach { it.participantId = pt.rowId }
        this.completedRounds.add(darts.toList())

        fireStateChanged()
    }

    fun setParticipantFinishPosition(finishingPosition: Int)
    {
        val team = pt.getTeam()
        team.finishingPosition = finishingPosition
        team.saveToDatabase()

        fireStateChanged()
    }

    fun participantFinished(finishingPosition: Int, finalScore: Int)
    {
        val team = pt.getTeam()
        team.finishingPosition = finishingPosition
        team.finalScore = finalScore
        team.dtFinished = getSqlDateNow()
        team.saveToDatabase()

        fireStateChanged()
    }

    fun updateActive(active: Boolean)
    {
        val changing = active != isActive
        isActive = active
        if (changing)
        {
            fireStateChanged()
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun fireStateChanged()
    {
        listeners.forEach { it.stateChanged(this as S) }
    }
}



