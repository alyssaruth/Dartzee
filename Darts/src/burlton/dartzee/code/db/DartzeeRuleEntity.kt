package burlton.dartzee.code.db

import burlton.dartzee.code.dartzee.DartzeeRuleCalculationResult
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.dartzee.parseDartRule
import burlton.dartzee.code.dartzee.parseTotalRule

class DartzeeRuleEntity: AbstractEntity<DartzeeRuleEntity>()
{
    var entityName = ""
    var entityId = ""
    var dart1Rule = ""
    var dart2Rule = ""
    var dart3Rule = ""
    var totalRule = ""
    var inOrder = false
    var allowMisses = false
    var ordinal = -1
    var calculationResult = ""

    override fun getTableName() = "DartzeeRule"

    override fun getCreateTableSqlSpecific(): String
    {
        return ("EntityName VARCHAR(255) NOT NULL, "
                + "EntityId VARCHAR(36) NOT NULL, "
                + "Dart1Rule VARCHAR(255) NOT NULL, "
                + "Dart2Rule VARCHAR(255) NOT NULL, "
                + "Dart3Rule VARCHAR(255) NOT NULL, "
                + "TotalRule VARCHAR(255) NOT NULL, "
                + "InOrder BOOLEAN NOT NULL, "
                + "AllowMisses BOOLEAN NOT NULL, "
                + "Ordinal INT NOT NULL, "
                + "CalculationResult VARCHAR(32000) NOT NULL")
    }

    fun toDto(): DartzeeRuleDto
    {
        val rule1 = parseDartRule(dart1Rule)
        val rule2 = parseDartRule(dart2Rule)
        val rule3 = parseDartRule(dart3Rule)
        val total = parseTotalRule(totalRule)
        val calculationResult = DartzeeRuleCalculationResult.fromDbString(calculationResult)

        val dto = DartzeeRuleDto(rule1, rule2, rule3, total, inOrder, allowMisses)
        dto.calculationResult = calculationResult
        return dto
    }

    fun retrieveForTemplate(templateId: String) = retrieveEntities(getTemplateWhere(templateId)).sortedBy { it.ordinal }
    fun deleteForTemplate(templateId: String) = deleteWhere(getTemplateWhere(templateId))


    fun getTemplateWhere(templateId: String) = "EntityName = '$DARTZEE_TEMPLATE' AND EntityId = '$templateId'"
}
