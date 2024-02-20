package dartzee.reporting

import dartzee.game.GameType
import java.sql.Timestamp

data class ReportParametersGame(
    val gameType: GameType?,
    val gameParams: String,
    val dtStartFrom: Timestamp?,
    val dtStartTo: Timestamp?,
    val unfinishedOnly: Boolean,
    val dtFinishFrom: Timestamp?,
    val dtFinishTo: Timestamp?,
    val partOfMatch: MatchFilter,
    val pendingChanges: Boolean?
)
