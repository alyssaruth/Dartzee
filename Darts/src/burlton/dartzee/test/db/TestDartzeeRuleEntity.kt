package burlton.dartzee.test.db

import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleEven
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOdd
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOuter
import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleEven
import burlton.dartzee.code.db.DartzeeRuleEntity
import burlton.dartzee.test.doubleNineteen
import burlton.dartzee.test.helper.makeDartzeeRuleCalculationResult
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartzeeRuleEntity: AbstractEntityTest<DartzeeRuleEntity>()
{
    override fun factoryDao() = DartzeeRuleEntity()

    @Test
    fun `Should support unset dartzee rules`()
    {
        val entity = DartzeeRuleEntity()
        val rowId = entity.assignRowId()
        entity.dart1Rule = DartzeeDartRuleEven().toDbString()
        entity.dart2Rule = ""

        entity.saveToDatabase()

        val reretrievedEntity = DartzeeRuleEntity().retrieveForId(rowId)!!
        reretrievedEntity.dart1Rule shouldBe DartzeeDartRuleEven().toDbString()
        reretrievedEntity.dart2Rule shouldBe ""
    }

    @Test
    fun `Should repopulate the DTO correctly`()
    {
        val entity = DartzeeRuleEntity()
        entity.dart1Rule = "<Even />"
        entity.dart2Rule = "<Odd />"
        entity.dart3Rule = "<Outer />"
        entity.totalRule = "<Even />"
        entity.allowMisses = true
        entity.inOrder = false

        val calculationResult = makeDartzeeRuleCalculationResult(listOf(doubleNineteen))
        entity.calculationResult = calculationResult.toDbString()

        val dto = entity.toDto()

        dto.dart1Rule!!.shouldBeInstanceOf<DartzeeDartRuleEven>()
        dto.dart2Rule!!.shouldBeInstanceOf<DartzeeDartRuleOdd>()
        dto.dart3Rule!!.shouldBeInstanceOf<DartzeeDartRuleOuter>()
        dto.totalRule!!.shouldBeInstanceOf<DartzeeTotalRuleEven>()
        dto.allowMisses shouldBe true
        dto.inOrder shouldBe false

        val newCalcResult = dto.calculationResult!!
        newCalcResult.validSegments.shouldContainExactly(doubleNineteen)
    }

}