package burlton.dartzee.code.db

import burlton.dartzee.code.dartzee.parseDartRule
import burlton.dartzee.code.dartzee.parseTotalRule

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

    fun getRuleDescription(): String
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
            "{ $dart1Desc, $dart2Desc, $dart3Desc }"
        }
    }
}
