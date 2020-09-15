package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.core.util.maxOrZero
import dartzee.db.ParticipantEntity
import dartzee.game.ClockType

data class ClockPlayerState(override val pt: ParticipantEntity,
                            override var lastRoundNumber: Int = 0,
                            override val darts: MutableList<List<Dart>> = mutableListOf(),
                            override val dartsThrown: MutableList<Dart> = mutableListOf()): AbstractPlayerState()
{
    fun getCurrentTarget(clockType: ClockType): Int
    {
        val allDarts = darts.flatten() + dartsThrown
        val lastHit = allDarts.filter { it.hitClockTarget(clockType) }.map { it.score }.maxOrZero()
        return lastHit + 1
    }

    override fun getScoreSoFar() = -1
}