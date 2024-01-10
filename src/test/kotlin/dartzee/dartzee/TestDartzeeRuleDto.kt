package dartzee.dartzee

import dartzee.dartzee.aggregate.DartzeeAggregateRuleRepeats
import dartzee.dartzee.aggregate.DartzeeTotalRuleGreaterThan
import dartzee.dartzee.aggregate.DartzeeTotalRulePrime
import dartzee.dartzee.dart.DartzeeDartRuleAny
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.dartzee.dart.DartzeeDartRuleInner
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.dartzee.dart.DartzeeDartRuleOuter
import dartzee.dartzee.dart.DartzeeDartRuleScore
import dartzee.db.EntityName
import dartzee.doubleNineteen
import dartzee.doubleTwenty
import dartzee.helper.AbstractTest
import dartzee.helper.getFakeValidSegment
import dartzee.helper.makeColourRule
import dartzee.helper.makeDart
import dartzee.helper.makeDartzeeRuleCalculationResult
import dartzee.helper.makeDartzeeRuleDto
import dartzee.helper.makeScoreRule
import dartzee.`object`.Dart
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.junit.jupiter.api.Test

class TestDartzeeRuleDto : AbstractTest() {
    @Test
    fun `Dart rule list should return all 3 rules when set`() {
        val dartRule1 = DartzeeDartRuleInner()
        val dartRule2 = DartzeeDartRuleEven()
        val dartRule3 = DartzeeDartRuleOdd()
        val rule = makeDartzeeRuleDto(dartRule1, dartRule2, dartRule3)

        rule.getDartRuleList().shouldContainExactly(dartRule1, dartRule2, dartRule3)
    }

    @Test
    fun `Dart rule list should just contain the first rule if the others arent set`() {
        val dartRule1 = DartzeeDartRuleOuter()
        val rule = makeDartzeeRuleDto(dartRule1)

        rule.getDartRuleList().shouldContainExactly(dartRule1)
    }

    @Test
    fun `Dart rule list should be null if no dart rules have been set`() {
        val rule = makeDartzeeRuleDto(aggregateRule = DartzeeTotalRulePrime())

        rule.getDartRuleList() shouldBe null
    }

    @Test
    fun `Should run a calculation and cache the result`() {
        val dto = makeDartzeeRuleDto()
        dto.runStrengthCalculation()

        val result = dto.calculationResult
        result.shouldNotBeNull()
        result.validSegments.shouldContainExactly(getFakeValidSegment(0))
    }

    @Test
    fun `Should return the difficulty + difficulty desc of its calculation result`() {
        val dto = makeDartzeeRuleDto()
        dto.calculationResult = makeDartzeeRuleCalculationResult(50)

        dto.getDifficulty() shouldBe 50.0
        dto.getDifficultyDesc() shouldBe "Very Easy"
    }

    @Test
    fun `Should describe total rules correctly`() {
        val rule = makeDartzeeRuleDto(aggregateRule = DartzeeTotalRulePrime())
        rule.generateRuleDescription() shouldBe "Total is prime"

        val rule2 = makeDartzeeRuleDto(aggregateRule = DartzeeTotalRuleGreaterThan())
        rule2.generateRuleDescription() shouldBe "Total > 20"
    }

    @Test
    fun `Should describe in-order dart rules`() {
        val rule =
            makeDartzeeRuleDto(
                DartzeeDartRuleEven(),
                DartzeeDartRuleOdd(),
                DartzeeDartRuleEven(),
                inOrder = true
            )
        rule.generateRuleDescription() shouldBe "Even → Odd → Even"
    }

    @Test
    fun `Should condense the same rules if order isn't required`() {
        val rule =
            makeDartzeeRuleDto(
                DartzeeDartRuleInner(),
                DartzeeDartRuleOuter(),
                DartzeeDartRuleOuter(),
                inOrder = false
            )
        rule.generateRuleDescription() shouldBe "{ 2x Outer, 1x Inner }"
    }

    @Test
    fun `Should ignore Any rules if order isn't required`() {
        val rule =
            makeDartzeeRuleDto(
                DartzeeDartRuleInner(),
                DartzeeDartRuleOuter(),
                DartzeeDartRuleAny(),
                inOrder = false
            )
        rule.generateRuleDescription() shouldBe "{ 1x Inner, 1x Outer }"
    }

    @Test
    fun `Should return Anything for a totally empty rule`() {
        val rule = makeDartzeeRuleDto()
        rule.generateRuleDescription() shouldBe "Anything"
    }

    @Test
    fun `Should return Anything for a rule with Any dart rules`() {
        val unorderedRule =
            makeDartzeeRuleDto(
                DartzeeDartRuleAny(),
                DartzeeDartRuleAny(),
                DartzeeDartRuleAny(),
                inOrder = false
            )
        val orderedRule =
            makeDartzeeRuleDto(
                DartzeeDartRuleAny(),
                DartzeeDartRuleAny(),
                DartzeeDartRuleAny(),
                inOrder = false
            )

        unorderedRule.generateRuleDescription() shouldBe "Anything"
        orderedRule.generateRuleDescription() shouldBe "Anything"
    }

    @Test
    fun `Should describe 'score' dart rules`() {
        val scoreRule = DartzeeDartRuleScore()
        scoreRule.score = 15

        val rule = makeDartzeeRuleDto(scoreRule)
        rule.generateRuleDescription() shouldBe "Score 15s"
    }

    @Test
    fun `Dart and total rules should be concatenated if both are present`() {
        val rule =
            makeDartzeeRuleDto(DartzeeDartRuleEven(), aggregateRule = DartzeeTotalRuleGreaterThan())
        rule.generateRuleDescription() shouldBe "Score Evens, Total > 20"
    }

    @Test
    fun `Should return all validSegments as scoring segments if there are no dart rules`() {
        val validSegments = listOf(doubleNineteen, doubleTwenty)

        val rule = makeDartzeeRuleDto(aggregateRule = DartzeeTotalRulePrime())
        rule
            .getScoringSegments(emptyList(), validSegments)
            .shouldContainExactly(doubleNineteen, doubleTwenty)
    }

    @Test
    fun `Should return all validSegments as scoring segments if there are three dart rules`() {
        val validSegments = listOf(doubleNineteen, doubleTwenty)

        val rule =
            makeDartzeeRuleDto(makeScoreRule(19), DartzeeDartRuleOdd(), DartzeeDartRuleEven())
        rule
            .getScoringSegments(emptyList(), validSegments)
            .shouldContainExactly(doubleNineteen, doubleTwenty)
    }

    @Test
    fun `Should return all validSegments as scoring segments if fewer than 2 darts thrown for aggregate rule`() {
        val validSegments = listOf(doubleNineteen, doubleTwenty)
        val rule = makeDartzeeRuleDto(aggregateRule = DartzeeAggregateRuleRepeats())
        val dartsSoFar = listOf(Dart(20, 1))
        rule
            .getScoringSegments(dartsSoFar, validSegments)
            .shouldContainExactly(doubleNineteen, doubleTwenty)
    }

    @Test
    fun `Should only return the segments that score if there is a single dart rule`() {
        val validSegments = listOf(doubleNineteen, doubleTwenty)

        val rule = makeDartzeeRuleDto(makeScoreRule(19))
        rule.getScoringSegments(emptyList(), validSegments).shouldContainExactly(doubleNineteen)
    }

    @Test
    fun `Should only return the segments that score if we are 2 darts into an aggregate rule`() {
        val validSegments = listOf(doubleNineteen, doubleTwenty)
        val rule = makeDartzeeRuleDto(aggregateRule = DartzeeAggregateRuleRepeats())
        val dartsSoFar = listOf(Dart(20, 1), Dart(20, 1))
        rule.getScoringSegments(dartsSoFar, validSegments).shouldContainExactly(doubleTwenty)
    }

    @Test
    fun `Should convert to an entity correctly`() {
        val rule =
            DartzeeRuleDto(
                DartzeeDartRuleEven(),
                DartzeeDartRuleOdd(),
                DartzeeDartRuleInner(),
                DartzeeTotalRulePrime(),
                true,
                false,
                "foobar"
            )
        rule.runStrengthCalculation()

        val dao = rule.toEntity(5, EntityName.Game, "foo")

        dao.rowId.shouldNotBeEmpty()
        dao.entityName shouldBe EntityName.Game
        dao.entityId shouldBe "foo"
        dao.ordinal shouldBe 5
        dao.calculationResult shouldBe rule.calculationResult!!.toDbString()
        dao.inOrder shouldBe true
        dao.allowMisses shouldBe false
        dao.dart1Rule shouldBe DartzeeDartRuleEven().toDbString()
        dao.dart2Rule shouldBe DartzeeDartRuleOdd().toDbString()
        dao.dart3Rule shouldBe DartzeeDartRuleInner().toDbString()
        dao.aggregateRule shouldBe DartzeeTotalRulePrime().toDbString()
        dao.ruleName shouldBe "foobar"
    }

    @Test
    fun `Should convert null ruleName and rules to empty strings`() {
        val rule = DartzeeRuleDto(DartzeeDartRuleEven(), null, null, null, true, false, null)
        rule.runStrengthCalculation()

        val dao = rule.toEntity(5, EntityName.Game, "foo")
        dao.dart2Rule shouldBe ""
        dao.dart3Rule shouldBe ""
        dao.aggregateRule shouldBe ""
        dao.ruleName shouldBe ""
    }

    private val dartsForTotal = listOf(makeDart(20, 1), makeDart(20, 1), makeDart(5, 2))

    @Test
    fun `Should just sum the darts if there are no dart rules`() {
        val dto = makeDartzeeRuleDto()
        dto.getSuccessTotal(dartsForTotal) shouldBe 50
    }

    @Test
    fun `Should just sum the darts if there are three dart rules`() {
        val dto =
            makeDartzeeRuleDto(DartzeeDartRuleEven(), DartzeeDartRuleEven(), DartzeeDartRuleOdd())
        dto.getSuccessTotal(dartsForTotal) shouldBe 50
    }

    @Test
    fun `Should only sum the valid darts when there is only one dart rule`() {
        val dto = makeDartzeeRuleDto(DartzeeDartRuleScore())
        dto.getSuccessTotal(dartsForTotal) shouldBe 40

        val otherScoreRule = DartzeeDartRuleScore()
        otherScoreRule.score = 5

        val otherDto = makeDartzeeRuleDto(otherScoreRule)
        otherDto.getSuccessTotal(dartsForTotal) shouldBe 10
    }

    @Test
    fun `Should only sum the valid darts when tehre is an aggregate rule`() {
        val dto = makeDartzeeRuleDto(aggregateRule = DartzeeAggregateRuleRepeats())
        dto.getSuccessTotal(dartsForTotal) shouldBe 40
    }

    @Test
    fun `Should sum the intersection of valid darts when there is a score and aggregate rule`() {
        val dto =
            makeDartzeeRuleDto(
                makeColourRule(red = true),
                aggregateRule = DartzeeAggregateRuleRepeats()
            )
        val darts = listOf(makeDart(20, 1), makeDart(20, 3), makeDart(18, 3))
        dto.getSuccessTotal(darts) shouldBe 60
    }
}
