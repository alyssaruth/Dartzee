package burlton.dartzee.code.db

import burlton.dartzee.code.dartzee.ValidSegmentCalculationResult
import burlton.dartzee.code.dartzee.dart.AbstractDartzeeDartRule
import burlton.dartzee.code.dartzee.getValidSegments
import burlton.dartzee.code.dartzee.parseDartRule
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

    private var calculationResult: ValidSegmentCalculationResult? = null

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

    fun getParsedDartRules(): List<AbstractDartzeeDartRule>?
    {
        val parsedRule1 = parseDartRule(dart1Rule) ?: return null
        val parsedRule2 = parseDartRule(dart2Rule) ?: return listOf(parsedRule1)
        val parsedRule3 = parseDartRule(dart3Rule)!!

        return listOf(parsedRule1, parsedRule2, parsedRule3)
    }

    fun runStrengthCalculation(dartboard: Dartboard): ValidSegmentCalculationResult
    {
        val calculationResult = getValidSegments(dartboard, listOf())

        this.calculationResult = calculationResult

        return calculationResult
    }

    fun getStrengthDesc() = calculationResult?.getCombinationsDesc() ?: ""
}
