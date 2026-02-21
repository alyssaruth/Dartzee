package dartzee.game

import dartzee.db.DartsMatchEntity
import dartzee.game.state.IWrappedParticipant

fun matchIsComplete(match: DartsMatchEntity, participants: List<IWrappedParticipant>): Boolean {
    val grouped = participants.groupBy { it.getUniqueParticipantName() }
    val winCounts = grouped.values.map(::countWins)
    return when (match.mode) {
        MatchMode.FIRST_TO -> winCounts.any { it == match.games }
        MatchMode.POINTS -> winCounts.sum() == match.games
    }
}

private fun countWins(participants: List<IWrappedParticipant>) =
    participants.count { it.participant.finishingPosition == 1 }
