package dartzee.utils

import dartzee.`object`.Dart
import dartzee.db.CLOCK_TYPE_STANDARD

fun getLongestStreak(darts: List<Dart>, gameParams: String = CLOCK_TYPE_STANDARD): List<Dart>
{
    var biggestChain = mutableListOf<Dart>()
    var currentChain = mutableListOf<Dart>()
    var currentPtId = ""

    for (d in darts)
    {
        if (!d.hitClockTarget(gameParams))
        {
            currentChain = mutableListOf()
            continue
        }

        if (d.participantId != currentPtId)
        {
            currentChain = mutableListOf()
            currentPtId = d.participantId
        }

        //It's a hit and we've reset for a new game if necessary. Just increment.
        currentChain.add(d)
        if (currentChain.size > biggestChain.size)
        {
            biggestChain = currentChain.toMutableList()
        }
    }

    return biggestChain
}