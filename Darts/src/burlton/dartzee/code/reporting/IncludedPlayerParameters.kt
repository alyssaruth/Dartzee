package burlton.dartzee.code.reporting

const val COMPARATOR_SCORE_UNSET = "is unset"

class IncludedPlayerParameters
{
    var finishingPositions = listOf<Int>()
    var finalScoreComparator = ""
    var finalScore = -1

    fun generateExtraWhereSql(alias: String): String
    {
        val sb = StringBuilder()
        if (!finishingPositions.isEmpty())
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
