package dartzee.db

import dartzee.dartzee.DartzeeRuleCalculationResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.dartzee.parseDartRule
import dartzee.dartzee.parseAggregateRule
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase

const val MAX_RULE_NAME = 1000

class DartzeeRuleEntity(database: Database = mainDatabase): AbstractEntity<DartzeeRuleEntity>(database)
{
    var entityName = EntityName.DartzeeRule
    var entityId = ""
    var dart1Rule = ""
    var dart2Rule = ""
    var dart3Rule = ""
    var aggregateRule = ""
    var inOrder = false
    var allowMisses = false
    var ordinal = -1
    var calculationResult = ""
    var ruleName = ""

    override fun getTableName() = EntityName.DartzeeRule

    override fun getCreateTableSqlSpecific(): String
    {
        return ("EntityName VARCHAR(255) NOT NULL, "
                + "EntityId VARCHAR(36) NOT NULL, "
                + "Dart1Rule VARCHAR(32000) NOT NULL, "
                + "Dart2Rule VARCHAR(32000) NOT NULL, "
                + "Dart3Rule VARCHAR(32000) NOT NULL, "
                + "AggregateRule VARCHAR(255) NOT NULL, "
                + "InOrder BOOLEAN NOT NULL, "
                + "AllowMisses BOOLEAN NOT NULL, "
                + "Ordinal INT NOT NULL, "
                + "CalculationResult VARCHAR(32000) NOT NULL, "
                + "RuleName VARCHAR($MAX_RULE_NAME) NOT NULL")
    }

    fun toDto(includeCalculationResult: Boolean = true): DartzeeRuleDto
    {
        val rule1 = parseDartRule(dart1Rule)
        val rule2 = parseDartRule(dart2Rule)
        val rule3 = parseDartRule(dart3Rule)
        val total = parseAggregateRule(aggregateRule)
        val name = ruleName.ifBlank { null }

        val dto = DartzeeRuleDto(rule1, rule2, rule3, total, inOrder, allowMisses, name)
        if (includeCalculationResult)
        {
            val calculationResult = DartzeeRuleCalculationResult.fromDbString(calculationResult)
            dto.calculationResult = calculationResult
        }

        return dto
    }

    fun retrieveForTemplate(templateId: String) = retrieveEntities(getTemplateWhere(templateId)).sortedBy { it.ordinal }
    fun deleteForTemplate(templateId: String) = deleteWhere(getTemplateWhere(templateId))

    fun retrieveForGame(gameId: String) = retrieveEntities("EntityName = 'Game' AND EntityId = '$gameId'").sortedBy { it.ordinal }

    private fun getTemplateWhere(templateId: String) = "EntityName = '${EntityName.DartzeeTemplate}' AND EntityId = '$templateId'"
}
