package burlton.dartzee.code.db

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.parseDartRule
import burlton.dartzee.code.dartzee.parseTotalRule
import burlton.dartzee.code.screen.Dartboard

class DartzeeRuleEntity: AbstractEntity<DartzeeRuleEntity>()
{
    var gameId = ""
    var dart1Rule = ""
    var dart2Rule = ""
    var dart3Rule = ""
    var totalRule = ""
    var inOrder = false
    var allowMisses = false
    var textualName = ""
    var textualDescription = "" //Allow textual rules
    var ordinal = -1

    override fun getTableName() = "DartzeeRule"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("GameId VARCHAR(36) NOT NULL, "
                + "Dart1Rule VARCHAR(255) NOT NULL, "
                + "Dart2Rule VARCHAR(255) NOT NULL, "
                + "Dart3Rule VARCHAR(255) NOT NULL, "
                + "TotalRule VARCHAR(255) NOT NULL, "
                + "InOrder BOOLEAN NOT NULL, "
                + "AllowMisses BOOLEAN NOT NULL, "
                + "TextualName VARCHAR(255) NOT NULL, "
                + "TextualDescription VARCHAR(2500) NOT NULL, "
                + "Ordinal INT NOT NULL")
    }

    fun generateRuleDescription(): String
    {
        val dartsDesc = getDartsDescription()
        val totalDesc = getTotalDescription()

        val ruleParts = mutableListOf<String>()
        if (!dartsDesc.isEmpty()) ruleParts.add(dartsDesc)
        if (!totalDesc.isEmpty()) ruleParts.add(totalDesc)

        return ruleParts.joinToString()
    }
    private fun getTotalDescription(): String
    {
        if (totalRule == "")
        {
            return ""
        }

        val desc = parseTotalRule(totalRule)!!.getDescription()

        return "Total $desc"
    }

    private fun getDartsDescription(): String
    {
        if (dart1Rule == "")
        {
            return ""
        }

        if (dart2Rule == "")
        {
            return "Score ${parseDartRule(dart1Rule)!!.getDescription()}"
        }

        //It's a 3 dart rule
        val dart1Desc = parseDartRule(dart1Rule)!!.getDescription()
        val dart2Desc = parseDartRule(dart2Rule)!!.getDescription()
        val dart3Desc = parseDartRule(dart3Rule)!!.getDescription()

        return if (inOrder)
        {
            "$dart1Desc → $dart2Desc → $dart3Desc"
        }
        else
        {
            //Try to condense the descriptions
            val rules = listOf(dart1Desc, dart2Desc, dart3Desc)
            val mapEntries = rules.groupBy { it }.map { it }
            val sortedGroupedRules = mapEntries.sortedByDescending { it.value.size }.map { "${it.value.size}x ${it.key}" }
            "{ ${sortedGroupedRules.joinToString()} }"
        }
    }

    fun getValidSegments(dartboard: Dartboard, dartsSoFar: List<Dart>): List<DartboardSegment>
    {
        val segments = dartboard.getAllSegments()
        return segments.filter { isValidSegment(it, dartsSoFar) }
    }
    fun isValidSegment(segment: DartboardSegment, dartsSoFar: List<Dart>): Boolean
    {
        return isValidSegmentForDartsRules(segment, dartsSoFar)
                && isValidSegmentForTotalRule(segment, dartsSoFar)
    }
    fun isValidSegmentForDartsRules(segment: DartboardSegment, dartsSoFar: List<Dart>): Boolean
    {
        if (dart1Rule == "")
        {
            return true
        }

        if (dart2Rule == "")
        {
            dartsSoFar.map { it.}
        }
    }
    fun isValidSegmentForTotalRule(segment: DartboardSegment, dartsSoFar: List<Dart>): Boolean
    {
        if (totalRule == "")
        {
            return true
        }

        val rule = parseTotalRule(totalRule)!!
        return rule.isValidSegment(segment, dartsSoFar)
    }
}
