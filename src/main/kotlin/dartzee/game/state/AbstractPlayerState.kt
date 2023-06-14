package dartzee.game.state

import dartzee.core.util.getSqlDateNow
import dartzee.db.BulkInserter
import dartzee.db.DartEntity
import dartzee.db.ParticipantEntity
import dartzee.`object`.Dart

abstract class AbstractPlayerState<S: AbstractPlayerState<S>>
{
    private val listeners = mutableListOf<PlayerStateListener<S>>()

    abstract val wrappedParticipant: IWrappedParticipant
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
    fun currentIndividual() = wrappedParticipant.getIndividual(currentRoundNumber())
    fun lastIndividual() = wrappedParticipant.getIndividual(completedRounds.size)

    fun getRoundsForIndividual(individual: ParticipantEntity) =
        (completedRounds + listOf(currentRound)).filter { it.all { drt -> drt.participantId == individual.rowId } }

    protected fun getAllDartsFlattened() = completedRounds.flatten() + currentRound

    fun isHuman() = !currentIndividual().isAi()
    fun hasMultiplePlayers() = wrappedParticipant.individuals.size > 1

    /**
     * Modifiers
     */
    open fun dartThrown(dart: Dart)
    {
        dart.roundNumber = currentRoundNumber()
        dart.participantId = currentIndividual().rowId
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
        val pt = currentIndividual()
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
        val pt = currentIndividual()
        darts.forEach { it.participantId = pt.rowId }
        this.completedRounds.add(darts.toList())

        fireStateChanged()
    }

    fun setParticipantFinishPosition(finishingPosition: Int)
    {
        val ptEntity = wrappedParticipant.participant
        ptEntity.finishingPosition = finishingPosition
        ptEntity.saveToDatabase()

        fireStateChanged()
    }

    fun participantFinished(finishingPosition: Int, finalScore: Int)
    {
        val ptEntity = wrappedParticipant.participant
        ptEntity.finishingPosition = finishingPosition
        ptEntity.finalScore = finalScore
        ptEntity.dtFinished = getSqlDateNow()
        ptEntity.saveToDatabase()

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



