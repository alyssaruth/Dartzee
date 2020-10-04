package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.core.util.maxOrZero
import dartzee.db.ParticipantEntity
import dartzee.game.RoundTheClockConfig
import dartzee.utils.getLongestStreak

data class ClockPlayerState(private val config: RoundTheClockConfig,
                            override val pt: ParticipantEntity,
                            override val completedRounds: MutableList<List<Dart>> = mutableListOf(),
                            override val currentRound: MutableList<Dart> = mutableListOf(),
                            override var isActive: Boolean = false): AbstractPlayerState<ClockPlayerState>()
{
    fun getCurrentTarget(): Int
    {
        val lastHit = getAllDartsFlattened().filter { it.hitClockTarget(config.clockType) }.map { it.score }.maxOrZero()
        return lastHit + 1
    }

    override fun getScoreSoFar() = getAllDartsFlattened().size

    fun getLongestStreak() = getLongestStreak(getAllDartsFlattened(), config.clockType).size

    fun onTrackForBrucey() = currentRound.all { it.hitClockTarget(config.clockType) }

    fun hasHitTarget(target: Int) = getAllDartsFlattened().any { it.hitClockTarget(config.clockType) && it.score == target }

    override fun dartThrown(dart: Dart)
    {
        dart.startingScore = getCurrentTarget()
        super.dartThrown(dart)
    }
}