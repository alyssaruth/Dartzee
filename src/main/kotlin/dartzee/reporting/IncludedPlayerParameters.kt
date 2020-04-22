package dartzee.reporting

const val COMPARATOR_SCORE_UNSET = "is unset"

data class IncludedPlayerParameters(var finishingPositions: List<Int> = listOf(),
                                    var finalScoreComparator: String = "",
                                    var finalScore: Int = -1)
{
    fun generateExtraWhereSql(alias: String): String
    {
        val sb = StringBuilder()
        if (finishingPositions.isNotEmpty())
        {
            val finishingPositionsStr = finishingPositions.joinToString()
            sb.append(" AND $alias.FinishingPosition IN ($finishingPositionsStr)")
        }

        if (finalScoreComparator.equals(COMPARATOR_SCORE_UNSET, ignoreCase = true))
        {
            sb.append(" AND $alias.FinalScore = -1")
        }
        else if (finalScore > -1)
        {
            sb.append(" AND $alias.FinalScore $finalScoreComparator $finalScore")
            sb.append(" AND $alias.FinalScore > -1")
        }

        return sb.toString()
    }
}
