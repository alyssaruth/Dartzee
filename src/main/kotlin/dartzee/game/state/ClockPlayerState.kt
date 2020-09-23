package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.core.util.maxOrZero
import dartzee.db.ParticipantEntity
import dartzee.game.ClockType
import dartzee.utils.getLongestStreak

data class ClockPlayerState(private val clockType: ClockType,
                            override val pt: ParticipantEntity,
                            override val completedRounds: MutableList<List<Dart>> = mutableListOf(),
                            override val currentRound: MutableList<Dart> = mutableListOf()): AbstractPlayerState<ClockPlayerState>()
{
    fun getCurrentTarget(): Int
    {
        val lastHit = getAllDartsFlattened().filter { it.hitClockTarget(clockType) }.map { it.score }.maxOrZero()
        return lastHit + 1
    }

    override fun getScoreSoFar() = getAllDartsFlattened().size

    fun getLongestStreak() = getLongestStreak(getAllDartsFlattened(), clockType).size

    fun onTrackForBrucey() = currentRound.all { it.hitClockTarget(clockType) }

    override fun dartThrown(dart: Dart)
    {
        dart.startingScore = getCurrentTarget()
        super.dartThrown(dart)
    }
}