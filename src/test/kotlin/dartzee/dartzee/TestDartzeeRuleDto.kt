package dartzee.dartzee

import dartzee.dartzee.dart.*
import dartzee.dartzee.total.DartzeeTotalRuleGreaterThan
import dartzee.dartzee.total.DartzeeTotalRulePrime
import dartzee.doubleNineteen
import dartzee.doubleTwenty
import dartzee.helper.*
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.string.shouldNotBeEmpty
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeRuleDto: AbstractTest()
{
    @Test
    fun `Dart rule list should return all 3 rules when set`()
    {
        val dartRule1 = DartzeeDartRuleInner()
        val dartRule2 = DartzeeDartRuleEven()
        val dartRule3 = DartzeeDartRuleOdd()
        val rule = makeDartzeeRuleDto(dartRule1, dartRule2, dartRule3)

        rule.getDartRuleList().shouldContainExactly(dartRule1, dartRule2, dartRule3)
    }

    @Test
    fun `Dart rule list should just contain the first rule if the others arent set`()
    {
        val dartRule1 = DartzeeDartRuleOuter()
        val rule = makeDartzeeRuleDto(dartRule1)

        rule.getDartRuleList().shouldContainExactly(dartRule1)
    }

    @Test
    fun `Dart rule list should be null if no dart rules have been set`()
    {
        val rule = makeDartzeeRuleDto(totalRule = DartzeeTotalRulePrime())

        rule.getDartRuleList() shouldBe null
    }

    @Test
    fun `Should run a calculation and cache the result`()
    {
        val dto = makeDartzeeRuleDto()
        dto.runStrengthCalculation()

        val result = dto.calculationResult
        result.shouldNotBeNull()
        result.validSegments.shouldContainExactly(getFakeValidSegment(0))
    }

    @Test
    fun `Should return the difficulty + difficulty desc of its calculation result`()
    {
        val dto = makeDartzeeRuleDto()
        dto.calculationResult = makeDartzeeRuleCalculationResult(50)

        dto.getDifficulty() shouldBe 50.0
        dto.getDifficultyDesc() shouldBe "Very Easy"
    }

    @Test
    fun `Should describe total rules correctly`()
    {
        val rule = makeDartzeeRuleDto(totalRule = DartzeeTotalRulePrime())
        rule.generateRuleDescription() shouldBe "Total is prime"

        val rule2 = makeDartzeeRuleDto(totalRule = DartzeeTotalRuleGreaterThan())
        rule2.generateRuleDescription() shouldBe "Total > 20"
    }

    @Test
    fun `Should describe in-order dart rules`()
    {
        val rule = makeDartzeeRuleDto(
            DartzeeDartRuleEven(),
            DartzeeDartRuleOdd(),
            DartzeeDartRuleEven(),
            inOrder = true
        )
        rule.generateRuleDescription() shouldBe "Even → Odd → Even"
    }

    @Test
    fun `Should condense the same rules if order isn't required`()
    {
        val rule = makeDartzeeRuleDto(
            DartzeeDartRuleInner(),
            DartzeeDartRuleOuter(),
            DartzeeDartRuleOuter(),
            inOrder = false
        )
        rule.generateRuleDescription() shouldBe "{ 2x Outer, 1x Inner }"
    }

    @Test
    fun `Should ignore Any rules if order isn't required`()
    {
        val rule = makeDartzeeRuleDto(
            DartzeeDartRuleInner(),
            DartzeeDartRuleOuter(),
            DartzeeDartRuleAny(),
            inOrder = false
        )
        rule.generateRuleDescription() shouldBe "{ 1x Inner, 1x Outer }"
    }

    @Test
    fun `Should return Anything for a totally empty rule`()
    {
        val rule = makeDartzeeRuleDto()
        rule.generateRuleDescription() shouldBe "Anything"
    }

    @Test
    fun `Should return Anything for a rule with Any dart rules`()
    {
        val unorderedRule = makeDartzeeRuleDto(
            DartzeeDartRuleAny(),
            DartzeeDartRuleAny(),
            DartzeeDartRuleAny(),
            inOrder = false
        )
        val orderedRule = makeDartzeeRuleDto(
            DartzeeDartRuleAny(),
            DartzeeDartRuleAny(),
            DartzeeDartRuleAny(),
            inOrder = false
        )

        unorderedRule.generateRuleDescription() shouldBe "Anything"
        orderedRule.generateRuleDescription() shouldBe "Anything"
    }

    @Test
    fun `Should describe 'score' dart rules`()
    {
        val scoreRule = DartzeeDartRuleScore()
        scoreRule.score = 15

        val rule = makeDartzeeRuleDto(scoreRule)
        rule.generateRuleDescription() shouldBe "Score 15s"
    }

    @Test
    fun `Dart and total rules should be concatenated if both are present`()
    {
        val rule = makeDartzeeRuleDto(
            DartzeeDartRuleEven(),
            totalRule = DartzeeTotalRuleGreaterThan()
        )
        rule.generateRuleDescription() shouldBe "Score Evens, Total > 20"
    }

    @Test
    fun `Should return all validSegments as scoring segments if there are no dart rules`()
    {
        val validSegments = listOf(doubleNineteen, doubleTwenty)

        val rule = makeDartzeeRuleDto(totalRule = DartzeeTotalRulePrime())
        rule.getScoringSegments(validSegments).shouldContainExactly(doubleNineteen, doubleTwenty)
    }

    @Test
    fun `Should return all validSegments as scoring segments if there are three dart rules`()
    {
        val validSegments = listOf(doubleNineteen, doubleTwenty)

        val rule = makeDartzeeRuleDto(makeScoreRule(19), DartzeeDartRuleOdd(), DartzeeDartRuleEven())
        rule.getScoringSegments(validSegments).shouldContainExactly(doubleNineteen, doubleTwenty)
    }

    @Test
    fun `Should only return the segments that score if there is a single dart rule`()
    {
        val validSegments = listOf(doubleNineteen, doubleTwenty)

        val rule = makeDartzeeRuleDto(makeScoreRule(19))
        rule.getScoringSegments(validSegments).shouldContainExactly(doubleNineteen)
    }

    @Test
    fun `Should convert to an entity correctly`()
    {
        val rule = DartzeeRuleDto(DartzeeDartRuleEven(), DartzeeDartRuleOdd(), DartzeeDartRuleInner(), DartzeeTotalRulePrime(), true, false)
        rule.runStrengthCalculation()

        val dao = rule.toEntity(5, "Game", "foo")

        dao.rowId.shouldNotBeEmpty()
        dao.entityName shouldBe "Game"
        dao.entityId shouldBe "foo"
        dao.ordinal shouldBe 5
        dao.calculationResult shouldBe rule.calculationResult!!.toDbString()
        dao.inOrder shouldBe true
        dao.allowMisses shouldBe false
        dao.dart1Rule shouldBe DartzeeDartRuleEven().toDbString()
        dao.dart2Rule shouldBe DartzeeDartRuleOdd().toDbString()
        dao.dart3Rule shouldBe DartzeeDartRuleInner().toDbString()
        dao.aggregateRule shouldBe DartzeeTotalRulePrime().toDbString()
    }

    private val dartsForTotal = listOf(makeDart(20, 1), makeDart(20, 1), makeDart(5, 2))

    @Test
    fun `Should just sum the darts if there are no dart rules`()
    {
        val dto = makeDartzeeRuleDto()
        dto.getSuccessTotal(dartsForTotal) shouldBe 50
    }

    @Test
    fun `Should just sum the darts if there are three dart rules`()
    {
        val dto = makeDartzeeRuleDto(DartzeeDartRuleEven(), DartzeeDartRuleEven(), DartzeeDartRuleOdd())
        dto.getSuccessTotal(dartsForTotal) shouldBe 50
    }

    @Test
    fun `Should only sum the valid darts when there is only one dart rule`()
    {
        val dto = makeDartzeeRuleDto(DartzeeDartRuleScore())
        dto.getSuccessTotal(dartsForTotal) shouldBe 40

        val otherScoreRule = DartzeeDartRuleScore()
        otherScoreRule.score = 5

        val otherDto = makeDartzeeRuleDto(otherScoreRule)
        otherDto.getSuccessTotal(dartsForTotal) shouldBe 10
    }
}