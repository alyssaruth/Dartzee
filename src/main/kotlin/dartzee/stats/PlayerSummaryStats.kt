package dartzee.stats

import dartzee.game.GameType

data class ParticipantStats(val gameType: GameType, val finalScore: Int, val finishingPosition: Int)
data class PlayerSummaryStats(val gamesPlayed: Int, val gamesWon: Int, val bestScore: Int)