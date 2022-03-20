package dartzee.dartzee

import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.dartzee.aggregate.AbstractDartzeeAggregateRule
import dartzee.dartzee.dart.AbstractDartzeeDartRule
import dartzee.db.DartzeeRuleEntity
import dartzee.db.EntityName
import dartzee.utils.Database
import dartzee.utils.InjectedThings.dartzeeCalculator
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.sumScore

data class DartzeeRuleDto(val dart1Rule: AbstractDartzeeDartRule?, val dart2Rule: AbstractDartzeeDartRule?, val dart3Rule: AbstractDartzeeDartRule?,
                          val aggregateRule: AbstractDartzeeAggregateRule?, val inOrder: Boolean, val allowMisses: Boolean, val ruleName: String?)
{
    var calculationResult: DartzeeRuleCalculationResult? = null

    fun getDartRuleList(): List<AbstractDartzeeDartRule>?
    {
        dart1Rule ?: return null
        dart2Rule ?: return listOf(dart1Rule)

        return listOf(dart1Rule, dart2Rule, dart3Rule!!)
    }

    fun runStrengthCalculation(): DartzeeRuleCalculationResult
    {
        val calculationResult = dartzeeCalculator.getValidSegments(this, listOf())

        this.calculationResult = calculationResult

        return calculationResult
    }

    fun getSuccessTotal(darts: List<Dart>): Int
    {
        val dartsAfterAggregate = aggregateRule?.getScoringDarts(darts) ?: darts

        dart1Rule ?: return sumScore(dartsAfterAggregate)

        if (dart2Rule != null)
        {
            return sumScore(dartsAfterAggregate)
        }
        else
        {
            val validDarts = dartsAfterAggregate.filter { dart1Rule.isValidDart(it) }
            return sumScore(validDarts)
        }
    }

    /**
     * If this is a "Score X" rule, return the relevant segments
     */
    fun getScoringSegments(dartsSoFar: List<Dart>, validSegments: List<DartboardSegment>): List<DartboardSegment>
    {
        val scoringSegments = getScoringSegmentsForAggregateRule(dartsSoFar, validSegments)
        if (dart1Rule == null || dart2Rule != null)
        {
            return scoringSegments
        }

        return scoringSegments.filter { dart1Rule.isValidSegment(it) }
    }
    private fun getScoringSegmentsForAggregateRule(dartsSoFar: List<Dart>, validSegments: List<DartboardSegment>): List<DartboardSegment>
    {
        if (dartsSoFar.size == 2 && aggregateRule != null)
        {
            return validSegments.filter {
                val scoringDartsAfterTwo = aggregateRule.getScoringDarts(dartsSoFar).size
                val scoringDartsAfterThree = aggregateRule.getScoringDarts(dartsSoFar + Dart(it.score, it.getMultiplier())).size
                scoringDartsAfterThree > scoringDartsAfterTwo
            }
        }

        return validSegments
    }


    fun getDifficulty() = calculationResult?.percentage ?: 0.0
    fun getDifficultyDesc() = calculationResult?.getDifficultyDesc() ?: ""

    fun getDisplayName() = ruleName ?: generateRuleDescription()

    fun generateRuleDescription(): String
    {
        val dartsDesc = getDartsDescription()
        val totalDesc = getTotalDescription()

        val ruleParts = mutableListOf<String>()
        if (!dartsDesc.isEmpty()) ruleParts.add(dartsDesc)
        if (!totalDesc.isEmpty()) ruleParts.add(totalDesc)

        val result = ruleParts.joinToString()
        return if (result.isEmpty()) "Anything" else result
    }
    private fun getTotalDescription() = aggregateRule?.getDescription() ?: ""
    private fun getDartsDescription(): String
    {
        dart1Rule ?: return ""
        dart2Rule ?: return "Score ${dart1Rule.getDescription()}s"

        //It's a 3 dart rule
        val dart1Desc = dart1Rule.getDescription()
        val dart2Desc = dart2Rule.getDescription()
        val dart3Desc = dart3Rule!!.getDescription()

        val rules = listOf(dart1Desc, dart2Desc, dart3Desc)
        if (rules.all { it == "Any"} )
        {
            return ""
        }

        return if (inOrder)
        {
            "$dart1Desc → $dart2Desc → $dart3Desc"
        }
        else
        {
            //Try to condense the descriptions
            val interestingRules = rules.filter { it != "Any" }

            val mapEntries = interestingRules.groupBy { it }.map { it }
            val sortedGroupedRules = mapEntries.sortedByDescending { it.value.size }.map { "${it.value.size}x ${it.key}" }
            "{ ${sortedGroupedRules.joinToString()} }"
        }
    }

    fun toEntity(ordinal: Int, entityName: EntityName, entityId: String, database: Database = mainDatabase): DartzeeRuleEntity
    {
        val entity = DartzeeRuleEntity(database)
        entity.assignRowId()

        entity.dart1Rule = dart1Rule?.toDbString() ?: ""
        entity.dart2Rule = dart2Rule?.toDbString() ?: ""
        entity.dart3Rule = dart3Rule?.toDbString() ?: ""
        entity.aggregateRule = aggregateRule?.toDbString() ?: ""
        entity.allowMisses = allowMisses
        entity.inOrder = inOrder
        entity.entityName = entityName
        entity.entityId = entityId
        entity.ordinal = ordinal
        entity.calculationResult = calculationResult!!.toDbString()
        entity.ruleName = ruleName ?: ""

        return entity
    }
}
