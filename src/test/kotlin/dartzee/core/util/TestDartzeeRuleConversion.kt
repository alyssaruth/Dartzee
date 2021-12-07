package dartzee.core.util

import dartzee.dartzee.DartzeeCalculator
import dartzee.db.DartzeeRuleEntity
import dartzee.db.EntityName
import dartzee.helper.*
import dartzee.utils.DartzeeRuleConversion
import dartzee.utils.InjectedThings
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class TestDartzeeRuleConversion: AbstractTest()
{
    @Test
    fun `Should take all dartzee rules on the database and rerun their calculations`()
    {
        val dartzeeRule = makeDartzeeRuleDto(makeScoreRule(20), makeScoreRule(19), makeScoreRule(18), inOrder = true)
        dartzeeRule.calculationResult!!.validSegments.shouldBeEmpty()

        val entity = dartzeeRule.toEntity(1, EntityName.Game, randomGuid())
        entity.saveToDatabase()

        DartzeeRuleConversion.convertDartzeeRules()

        val updatedRule = DartzeeRuleEntity().retrieveForId(entity.rowId)!!
        val dto = updatedRule.toDto()

        dto.calculationResult!!.validSegments.shouldContainExactly(getFakeValidSegment(0))
    }

    @Test
    fun `Should dismiss the loading dialog even if an exception is thrown`()
    {
        val mockCalculator = mockk<DartzeeCalculator>()
        every { mockCalculator.getValidSegments(any(), any()) } throws Exception("Boom")

        InjectedThings.dartzeeCalculator = mockCalculator

        val dartzeeRule = makeDartzeeRuleDto(makeScoreRule(20), makeScoreRule(19), makeScoreRule(18), inOrder = true)
        dartzeeRule.calculationResult!!.validSegments.shouldBeEmpty()

        val entity = dartzeeRule.toEntity(1, EntityName.Game, randomGuid())
        entity.calculationResult = "boom"
        entity.saveToDatabase()

        DartzeeRuleConversion.convertDartzeeRules()

        errorLogged() shouldBe true
        dialogFactory.loadingVisible shouldBe false
    }
}