package dartzee.dartzee.dart

import dartzee.dartzee.AbstractDartzeeRuleTest
import dartzee.dartzee.parseDartRule
import dartzee.doubleTwenty
import dartzee.missTwenty
import dartzee.singleTwenty
import dartzee.trebleNineteen
import dartzee.trebleTwenty
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldNotBeInRange
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartzeeDartRuleScore : AbstractDartzeeRuleTest<DartzeeDartRuleScore>() {
    override fun factory() = DartzeeDartRuleScore()

    @Test
    fun `segment validation`() {
        val rule = DartzeeDartRuleScore()
        rule.score = 20

        rule.isValidSegment(singleTwenty) shouldBe true
        rule.isValidSegment(doubleTwenty) shouldBe true
        rule.isValidSegment(trebleTwenty) shouldBe true
        rule.isValidSegment(trebleNineteen) shouldBe false
        rule.isValidSegment(missTwenty) shouldBe false
    }

    @Test
    fun `Score config panel updates rule correctly`() {
        val rule = DartzeeDartRuleScore()
        rule.spinner.value shouldBe rule.score
        rule.score shouldBeGreaterThan -1

        for (i in 1..25) {
            rule.spinner.value = i

            rule.spinner.value shouldBe rule.score
            rule.score.shouldNotBeInRange(21..24)
        }
    }

    @Test
    fun `Read and write XML`() {
        val rule = DartzeeDartRuleScore()
        rule.score = 18

        val xml = rule.toDbString()
        val parsedRule = parseDartRule(xml) as DartzeeDartRuleScore

        parsedRule.score shouldBe 18
        parsedRule.spinner.value shouldBe 18
    }

    @Test
    fun `rule description`() {
        val rule = DartzeeDartRuleScore()
        rule.getDescription() shouldBe "20"

        rule.score = 15
        rule.getDescription() shouldBe "15"
    }
}
