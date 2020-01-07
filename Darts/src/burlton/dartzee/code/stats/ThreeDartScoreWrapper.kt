package burlton.dartzee.code.stats

import burlton.core.code.obj.HashMapCount
import java.util.*

/**
 * Wraps up the stuff for a specific 3 dart score
 */
class ThreeDartScoreWrapper
{
    private val hmDartStrToCount = HashMapCount<String>()
    private val hmDartStrToExampleGameId = HashMap<String, Long>()

    fun addDartStr(dartStr: String, gameId: Long)
    {
        val count = hmDartStrToCount.incrementCount(dartStr)
        if (count == 1)
        {
            hmDartStrToExampleGameId[dartStr] = gameId
        }
    }

    fun createRows(): List<Array<Any>>
    {
        return hmDartStrToCount.entries.map {(dartStr, count) ->
            arrayOf(dartStr, count, hmDartStrToExampleGameId[dartStr]!!)
        }
    }

    fun getTotalCount() = hmDartStrToCount.getTotalCount()
}