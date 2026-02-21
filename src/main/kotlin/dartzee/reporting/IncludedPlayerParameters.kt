package dartzee.reporting

const val COMPARATOR_SCORE_UNSET = "is unset"

data class IncludedPlayerParameters(
    val finishingPositions: List<Int>,
    val finalScoreComparator: String,
    val finalScore: Int?,
) {
    fun generateExtraWhereSql(alias: String): String {
        val sb = StringBuilder()
        if (finishingPositions.isNotEmpty()) {
            val finishingPositionsStr = finishingPositions.joinToString()
            sb.append(" AND $alias.FinishingPosition IN ($finishingPositionsStr)")
        }

        if (finalScoreComparator.equals(COMPARATOR_SCORE_UNSET, ignoreCase = true)) {
            sb.append(" AND $alias.FinalScore = -1")
        } else if (finalScore != null) {
            sb.append(" AND $alias.FinalScore $finalScoreComparator $finalScore")
            sb.append(" AND $alias.FinalScore > -1")
        }

        return sb.toString()
    }
}
