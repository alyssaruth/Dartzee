package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.db.ParticipantEntity
import dartzee.game.RoundTheClockConfig
import dartzee.utils.getLongestStreak

data class ClockPlayerState(private val config: RoundTheClockConfig,
                            override val pt: ParticipantEntity,
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

    override fun dartThrown(dart: Dart)
    {
        dart.startingScore = getCurrentTarget()

        if (!config.inOrder)
        {
            dart.clockTargets = getRemainingTargets()
        }

        super.dartThrown(dart)
    }

    fun getRemainingTargets(): List<Int>
    {
        val targetsHit = getAllDartsFlattened().filter { it.hitAnyClockTarget(config.clockType) }.map { it.score }
        return (1..20).filterNot { targetsHit.contains(it) }
    }
}