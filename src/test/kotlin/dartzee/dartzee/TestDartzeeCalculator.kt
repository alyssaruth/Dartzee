package dartzee.dartzee

import dartzee.core.util.getAllPermutations
import dartzee.dartzee.aggregate.DartzeeTotalRuleEqualTo
import dartzee.dartzee.aggregate.DartzeeTotalRuleEven
import dartzee.dartzee.aggregate.DartzeeTotalRuleLessThan
import dartzee.dartzee.dart.DartzeeDartRuleAny
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.dartzee.dart.DartzeeDartRuleOdd
import dartzee.dartzee.dart.DartzeeDartRuleOuter
import dartzee.doubleNineteen
import dartzee.helper.AbstractTest
import dartzee.helper.getOuterSegments
import dartzee.helper.makeDart
import dartzee.helper.makeDartzeeRuleDto
import dartzee.helper.makeScoreRule
import dartzee.helper.makeTotalScoreRule
import dartzee.missTwenty
import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.outerBull
import dartzee.singleEighteen
import dartzee.singleFive
import dartzee.singleNineteen
import dartzee.singleTen
import dartzee.singleTwenty
import dartzee.utils.getAllNonMissSegments
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.doubles.shouldBeBetween
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestAllPossibilities : AbstractTest() {
    @Test
    fun `Should generate the right number of possibilities if given no darts`() {
        val possibilities = DartzeeCalculator().generateAllPossibilities(listOf())
        possibilities.size shouldBe 83 * 83 * 83
        possibilities.all { it.size == 3 } shouldBe true
    }

    @Test
    fun `Should generate the right number of possibilities if given 1 starting dart`() {
        val dart = Dart(20, 3, segmentType = SegmentType.TREBLE)

        val possibilities = DartzeeCalculator().generateAllPossibilities(listOf(dart))
        possibilities.size shouldBe 83 * 83
        possibilities.all { it.size == 3 } shouldBe true
        possibilities.forEach { it.first().score shouldBe 20 }
        possibilities.forEach { it.first().type shouldBe SegmentType.TREBLE }
    }

    @Test
    fun `Should generate the right number of possibilities if given 2 starting darts`() {
        val dartOne = Dart(20, 3, segmentType = SegmentType.TREBLE)
        val dartTwo = Dart(19, 2, segmentType = SegmentType.DOUBLE)

        val possibilities = DartzeeCalculator().generateAllPossibilities(listOf(dartOne, dartTwo))
        possibilities.size shouldBe 83
        possibilities.all { it.size == 3 } shouldBe true
        possibilities.all { it[0].score == 20 && it[0].type == SegmentType.TREBLE } shouldBe true
        possibilities.all { it[1].score == 19 && it[1].type == SegmentType.DOUBLE } shouldBe true
    }
}

class TestValidSegments : AbstractTest() {
    @Test
    fun `getValidSegments should return the right results for the first dart`() {
        val rule =
            makeDartzeeRuleDto(
                DartzeeDartRuleEven(),
                DartzeeDartRuleOdd(),
                DartzeeDartRuleOuter(),
                inOrder = true
            )

        val expectedSegments =
            getAllNonMissSegments().filter { DartzeeDartRuleEven().isValidSegment(it) }

        val firstSegments = DartzeeCalculator().getValidSegments(rule, listOf())
        firstSegments.validSegments.shouldContainExactlyInAnyOrder(expectedSegments)
        firstSegments.scoringSegments.shouldContainExactlyInAnyOrder(expectedSegments)
    }

    @Test
    fun `should return the right results for the second dart`() {
        val rule =
            makeDartzeeRuleDto(
                DartzeeDartRuleEven(),
                DartzeeDartRuleOdd(),
                DartzeeDartRuleOuter(),
                inOrder = true
            )

        val expectedSegments =
            getAllNonMissSegments().filter { DartzeeDartRuleOdd().isValidSegment(it) }
        val secondSegments =
            DartzeeCalculator()
                .getValidSegments(rule, listOf(makeDart(2, 1, SegmentType.OUTER_SINGLE)))
        secondSegments.validSegments.shouldContainExactlyInAnyOrder(expectedSegments)
        secondSegments.scoringSegments.shouldContainExactlyInAnyOrder(expectedSegments)
    }

    @Test
    fun `should return the right results for the third dart`() {
        val rule =
            makeDartzeeRuleDto(
                DartzeeDartRuleEven(),
                DartzeeDartRuleOdd(),
                DartzeeDartRuleOuter(),
                inOrder = true
            )

        val dartsThrown =
            listOf(
                makeDart(2, 1, SegmentType.OUTER_SINGLE),
                makeDart(3, 1, SegmentType.INNER_SINGLE)
            )
        val expectedSegments =
            getAllNonMissSegments().filter { DartzeeDartRuleOuter().isValidSegment(it) }
        val thirdSegments = DartzeeCalculator().getValidSegments(rule, dartsThrown)
        thirdSegments.validSegments.shouldContainExactlyInAnyOrder(expectedSegments)
        thirdSegments.scoringSegments.shouldContainExactlyInAnyOrder(expectedSegments)
    }

    @Test
    fun `should return no results for darts 2 or 3 if the rule is already failed`() {
        val rule =
            makeDartzeeRuleDto(
                DartzeeDartRuleEven(),
                DartzeeDartRuleOdd(),
                DartzeeDartRuleOuter(),
                inOrder = true
            )

        val secondSegments =
            DartzeeCalculator()
                .getValidSegments(rule, listOf(makeDart(3, 1, SegmentType.OUTER_SINGLE)))
        secondSegments.validSegments.shouldBeEmpty()
        secondSegments.scoringSegments.shouldBeEmpty()

        val invalidSecondDart =
            listOf(
                makeDart(2, 1, SegmentType.OUTER_SINGLE),
                makeDart(2, 1, SegmentType.OUTER_SINGLE)
            )
        val thirdSegments = DartzeeCalculator().getValidSegments(rule, invalidSecondDart)
        thirdSegments.validSegments.shouldBeEmpty()
        thirdSegments.scoringSegments.shouldBeEmpty()
    }

    @Test
    fun `Should return empty list if three darts thrown and rule has failed`() {
        val rule =
            makeDartzeeRuleDto(
                DartzeeDartRuleEven(),
                DartzeeDartRuleOdd(),
                DartzeeDartRuleOuter(),
                inOrder = true
            )

        val dartsThrown =
            listOf(
                makeDart(2, 1, SegmentType.DOUBLE),
                makeDart(19, 3, SegmentType.TREBLE),
                makeDart(20, 0, SegmentType.MISS)
            )
        val result = DartzeeCalculator().getValidSegments(rule, dartsThrown)

        result.scoringSegments.shouldBeEmpty()
        result.validSegments.shouldBeEmpty()
        result.percentage shouldBe 0.0
        result.validCombinationProbability shouldBe 0.0
    }

    @Test
    fun `Should return the segments for dart 3 if the rule has been passed`() {
        val rule =
            makeDartzeeRuleDto(
                DartzeeDartRuleEven(),
                DartzeeDartRuleOdd(),
                DartzeeDartRuleOuter(),
                inOrder = true
            )

        val dartsThrown =
            listOf(
                makeDart(2, 1, SegmentType.DOUBLE),
                makeDart(19, 3, SegmentType.TREBLE),
                makeDart(20, 2, SegmentType.DOUBLE)
            )
        val result = DartzeeCalculator().getValidSegments(rule, dartsThrown)

        result.scoringSegments.shouldContainExactlyInAnyOrder(getOuterSegments())
        result.validSegments.shouldContainExactlyInAnyOrder(getOuterSegments())
    }

    @Test
    fun `Should handle dart rules that can be in any order`() {
        val rule =
            makeDartzeeRuleDto(
                makeScoreRule(20),
                makeScoreRule(19),
                makeScoreRule(18),
                inOrder = false
            )

        val expectedSegments =
            getAllNonMissSegments().filter { listOf(20, 19, 18).contains(it.score) }
        val result = DartzeeCalculator().getValidSegments(rule, listOf())
        result.scoringSegments.shouldContainExactlyInAnyOrder(expectedSegments)
        result.validSegments.shouldContainExactlyInAnyOrder(expectedSegments)

        val resultTwo =
            DartzeeCalculator()
                .getValidSegments(rule, listOf(makeDart(20, 1, SegmentType.OUTER_SINGLE)))
        resultTwo.validSegments.shouldContainExactlyInAnyOrder(
            expectedSegments.filter { it.score != 20 }
        )
        resultTwo.scoringSegments.shouldContainExactlyInAnyOrder(
            expectedSegments.filter { it.score != 20 }
        )
    }

    @Test
    fun `Should yield sensible probabilities`() {
        val rule =
            makeDartzeeRuleDto(
                DartzeeDartRuleOdd(),
                DartzeeDartRuleAny(),
                DartzeeDartRuleAny(),
                inOrder = true,
                allowMisses = true
            )

        val result = DartzeeCalculator().getValidSegments(rule, listOf())
        result.percentage.shouldBeBetween(49.0, 51.0, 0.0)

        val resultTwo =
            DartzeeCalculator()
                .getValidSegments(rule, listOf(makeDart(13, 1, SegmentType.OUTER_SINGLE)))
        resultTwo.percentage.shouldBeExactly(100.0)
    }

    @Test
    fun `should combine total and darts rules correctly`() {
        val rule =
            makeDartzeeRuleDto(
                DartzeeDartRuleEven(),
                DartzeeDartRuleEven(),
                DartzeeDartRuleEven(),
                makeTotalScoreRule<DartzeeTotalRuleLessThan>(20),
                true
            )

        val segments = DartzeeCalculator().getValidSegments(rule, listOf())

        segments.validSegments.find { it.score == 16 } shouldBe null
        segments.scoringSegments.find { it.score == 16 } shouldBe null
    }

    @Test
    fun `should not cache results between calculations`() {
        val ruleOne =
            makeDartzeeRuleDto(
                makeScoreRule(20),
                makeScoreRule(19),
                makeScoreRule(18),
                inOrder = false
            )
        val ruleTwo =
            makeDartzeeRuleDto(
                makeScoreRule(1),
                makeScoreRule(2),
                makeScoreRule(3),
                inOrder = false
            )

        val calculator = DartzeeCalculator()

        val firstSegments = calculator.getValidSegments(ruleOne, listOf())
        val secondSegments = calculator.getValidSegments(ruleTwo, listOf())

        firstSegments.validSegments.any { it.score == 20 } shouldBe true
        firstSegments.scoringSegments.any { it.score == 20 } shouldBe true
        secondSegments.validSegments.any { it.score == 20 } shouldBe false
        secondSegments.scoringSegments.any { it.score == 20 } shouldBe false
    }

    @Test
    fun `Should distinguish scoring segments from valid ones for Score X rules`() {
        val dartRule = makeScoreRule(20)
        val rule = makeDartzeeRuleDto(dartRule)
        val calculator = DartzeeCalculator()

        val expectedScoringSegments = getAllNonMissSegments().filter { dartRule.isValidSegment(it) }
        val expectedValidSegments = getAllNonMissSegments()

        // 0 darts
        val firstSegments = calculator.getValidSegments(rule, emptyList())
        firstSegments.scoringSegments.shouldContainExactlyInAnyOrder(expectedScoringSegments)
        firstSegments.validSegments.shouldContainExactlyInAnyOrder(expectedValidSegments)

        // 1 dart - non-20s should still be valid
        val secondSegments = calculator.getValidSegments(rule, listOf(makeDart(19, 1)))
        secondSegments.scoringSegments.shouldContainExactlyInAnyOrder(expectedScoringSegments)
        secondSegments.validSegments.shouldContainExactlyInAnyOrder(expectedValidSegments)

        // 2 darts - non-20s should no longer be valid
        val thirdSegments =
            calculator.getValidSegments(rule, listOf(makeDart(19, 1), makeDart(19, 1)))
        thirdSegments.scoringSegments.shouldContainExactlyInAnyOrder(expectedScoringSegments)
        thirdSegments.validSegments.shouldContainExactlyInAnyOrder(expectedScoringSegments)

        // 2 darts, but with a hit - non-20s should be valid
        val thirdSegmentsWithHit =
            calculator.getValidSegments(rule, listOf(makeDart(19, 1), makeDart(20, 1)))
        thirdSegmentsWithHit.scoringSegments.shouldContainExactlyInAnyOrder(expectedScoringSegments)
        thirdSegmentsWithHit.validSegments.shouldContainExactlyInAnyOrder(expectedValidSegments)
    }

    @Test
    fun `Should return a miss segment as valid if missing is an option`() {
        val totalRule = makeTotalScoreRule<DartzeeTotalRuleLessThan>(20)
        val rule = makeDartzeeRuleDto(aggregateRule = totalRule, allowMisses = true)

        val dartsSoFar = listOf(makeDart(15, 1), makeDart(4, 1))

        val segments = DartzeeCalculator().getValidSegments(rule, dartsSoFar)
        segments.validSegments.shouldContainExactly(DartboardSegment(SegmentType.MISS, 20))
        segments.scoringSegments.shouldContainExactly(DartboardSegment(SegmentType.MISS, 20))
    }
}

class TestValidCombinations : AbstractTest() {
    @Test
    fun `should correctly identify all permutations as valid if no ordering required`() {
        val rule =
            makeDartzeeRuleDto(
                makeScoreRule(20),
                makeScoreRule(19),
                makeScoreRule(18),
                inOrder = false
            )

        val segments = listOf(doubleNineteen, singleTwenty, singleEighteen)

        val permutations = segments.getAllPermutations()

        permutations.forEach { DartzeeCalculator().isValidCombination(it, rule) shouldBe true }
    }

    @Test
    fun `should enforce ordering correctly`() {
        val rule =
            makeDartzeeRuleDto(
                makeScoreRule(20),
                makeScoreRule(19),
                makeScoreRule(18),
                inOrder = true
            )

        val orderedSegments = listOf(singleTwenty, singleNineteen, singleEighteen)

        val permutations = orderedSegments.getAllPermutations()

        permutations.forEach {
            DartzeeCalculator().isValidCombination(it, rule) shouldBe (it == orderedSegments)
        }
    }

    @Test
    fun `should test for both total and dart rules`() {
        val rule =
            makeDartzeeRuleDto(
                makeScoreRule(20),
                makeScoreRule(19),
                makeScoreRule(18),
                DartzeeTotalRuleEven(),
                true
            )

        DartzeeCalculator()
            .isValidCombination(listOf(singleTwenty, singleNineteen, singleEighteen), rule) shouldBe
            false
        DartzeeCalculator()
            .isValidCombination(
                listOf(singleEighteen, singleEighteen, singleEighteen),
                rule
            ) shouldBe false
        DartzeeCalculator()
            .isValidCombination(listOf(singleTwenty, doubleNineteen, singleEighteen), rule) shouldBe
            true
    }

    @Test
    fun `Should test for just a single dart rule`() {
        val rule = makeDartzeeRuleDto(makeScoreRule(20))

        DartzeeCalculator()
            .isValidCombination(listOf(singleTwenty, singleTwenty, singleTwenty), rule) shouldBe
            true
        DartzeeCalculator()
            .isValidCombination(listOf(singleTwenty, singleTwenty, singleNineteen), rule) shouldBe
            true
        DartzeeCalculator()
            .isValidCombination(listOf(singleTwenty, singleNineteen, singleNineteen), rule) shouldBe
            true
        DartzeeCalculator()
            .isValidCombination(
                listOf(singleNineteen, singleNineteen, singleNineteen),
                rule
            ) shouldBe false
    }

    @Test
    fun `should test for just total rules`() {
        val rule =
            makeDartzeeRuleDto(aggregateRule = makeTotalScoreRule<DartzeeTotalRuleEqualTo>(50))

        DartzeeCalculator()
            .isValidCombination(listOf(singleTwenty, singleTwenty, singleTen), rule) shouldBe true
        DartzeeCalculator()
            .isValidCombination(listOf(outerBull, singleTwenty, singleFive), rule) shouldBe true
        DartzeeCalculator()
            .isValidCombination(listOf(singleTwenty, singleTwenty, singleTwenty), rule) shouldBe
            false
    }

    @Test
    fun `should validate misses correctly`() {
        val rule = makeDartzeeRuleDto(allowMisses = false)
        val ruleWithMisses = makeDartzeeRuleDto(allowMisses = true)

        val combination = listOf(singleTwenty, missTwenty, singleTwenty)
        DartzeeCalculator().isValidCombination(combination, rule) shouldBe false
        DartzeeCalculator().isValidCombination(combination, ruleWithMisses) shouldBe true
    }
}
