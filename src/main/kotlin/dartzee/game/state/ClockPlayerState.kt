package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.game.RoundTheClockConfig
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.getAllPossibleSegments
import dartzee.utils.getLongestStreak

data class ClockPlayerState(private val config: RoundTheClockConfig,
                            override val wrappedParticipant: IWrappedParticipant,
                            override val completedRounds: MutableList<List<Dart>> = mutableListOf(),
                            override val currentRound: MutableList<Dart> = mutableListOf(),
                            override var isActive: Boolean = false): AbstractPlayerState<ClockPlayerState>()
{
    fun findCurrentTarget() = getRemainingTargets().firstOrNull()

    fun getCurrentTarget() = findCurrentTarget() ?: throw Exception("Dart thrown when no remaining targets")

    override fun getScoreSoFar() = getAllDartsFlattened().size

    fun getLongestStreak() = getLongestStreak(getAllDartsFlattened(), config.clockType).size

    fun onTrackForBrucey() = currentRound.all { it.hitClockTarget(config.clockType) }

    fun hasHitTarget(target: Int) = getAllDartsFlattened().any { it.hitAnyClockTarget(config.clockType) && it.score == target }

    fun getSegmentStatus(): SegmentStatus
    {
        val scoringSegments = getAllPossibleSegments().filter { it.score == findCurrentTarget() }
        val validSegments = if (!config.inOrder)
        {
            getAllPossibleSegments().filterNot { hasHitTarget(it.score) || it.score == 25 }
        }
        else
        {
            getAllPossibleSegments() - scoringSegments
        }

        return SegmentStatus(scoringSegments, validSegments)
    }

    override fun dartThrown(dart: Dart)
    {
        dart.startingScore = getCurrentTarget()

        if (!config.inOrder)
        {
            dart.clockTargets = getRemainingTargets()
        }

        super.dartThrown(dart)
    }

    override fun addLoadedRound(darts: List<Dart>)
    {
        darts.forEach(::dartThrown)
        resetRound()

        addCompletedRound(darts)
    }

    private fun getRemainingTargets(): List<Int>
    {
        val targetsHit = getAllDartsFlattened().filter { it.hitAnyClockTarget(config.clockType) }.map { it.score }
        return (1..20).filterNot { targetsHit.contains(it) }
    }
}