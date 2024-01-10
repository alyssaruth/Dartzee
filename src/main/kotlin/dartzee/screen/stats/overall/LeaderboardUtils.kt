package dartzee.screen.stats.overall

data class LeaderboardEntry(val score: Int, val rowValues: List<Any>)

fun getRankedRowsForTable(entries: List<LeaderboardEntry>): List<Array<Any>> {
    var previousPosition = 1
    var previousScore = -1
    return entries.mapIndexed { ix, entry ->
        val position = if (entry.score != previousScore) ix + 1 else previousPosition
        previousPosition = position
        previousScore = entry.score
        (listOf(position) + entry.rowValues).toTypedArray()
    }
}
