package dartzee.core.util

import dartzee.db.DartzeeRuleEntity
import dartzee.helper.*
import dartzee.utils.DartzeeRuleConversion
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import org.junit.Test

class TestDartzeeRuleConversion: AbstractTest()
{
    @Test
    fun `Should take all dartzee rules on the database and rerun their calculations`()
    {
        val dartzeeRule = makeDartzeeRuleDto(makeScoreRule(20), makeScoreRule(19), makeScoreRule(18), inOrder = true)
        dartzeeRule.calculationResult!!.validSegments.shouldBeEmpty()

        val entity = dartzeeRule.toEntity(1, "Game", randomGuid())
        entity.saveToDatabase()

        DartzeeRuleConversion.convertDartzeeRules()

        val updatedRule = DartzeeRuleEntity().retrieveForId(entity.rowId)!!
        val dto = updatedRule.toDto()

        dto.calculationResult!!.validSegments.shouldContainExactly(getFakeValidSegment(0))
    }
}