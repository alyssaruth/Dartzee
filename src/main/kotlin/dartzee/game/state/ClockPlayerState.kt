package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.core.util.maxOrZero
import dartzee.db.ParticipantEntity
import dartzee.game.ClockType
import dartzee.utils.getLongestStreak

data class ClockPlayerState(override val pt: ParticipantEntity,
                            override val completedRounds: MutableList<List<Dart>> = mutableListOf(),
                            override val currentRound: MutableList<Dart> = mutableListOf()): AbstractPlayerState<ClockPlayerState>()
{
    fun getCurrentTarget(clockType: ClockType): Int
    {
        val lastHit = getAllDartsFlattened().filter { it.hitClockTarget(clockType) }.map { it.score }.maxOrZero()
        return lastHit + 1
    }

    override fun getScoreSoFar() = getAllDartsFlattened().size

    fun getLongestStreak(clockType: ClockType) = getLongestStreak(getAllDartsFlattened(), clockType).size
}