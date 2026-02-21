package dartzee.dartzee.aggregate

import dartzee.bullseye
import dartzee.dartzee.AbstractDartzeeRuleTest
import dartzee.dartzee.parseAggregateRule
import dartzee.helper.double
import dartzee.helper.miss
import dartzee.helper.outerSingle
import dartzee.helper.treble
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.text.ParseException
import org.junit.jupiter.api.Test

class TestDartzeeAggregateRuleCluster : AbstractDartzeeRuleTest<DartzeeAggregateRuleCluster>() {
    override fun factory() = DartzeeAggregateRuleCluster()

    @Test
    fun `Should initialise with a spread of 1`() {
        val rule = factory()
        rule.spinner.value shouldBe 1
    }

    @Test
    fun `Should be possible to set values between 1 and 5`() {
        val rule = factory()
        rule.spinner.value = 1
        rule.spinner.commitEdit()
        rule.spinner.value shouldBe 1

        rule.spinner.value = 5
        rule.spinner.commitEdit()
        rule.spinner.value shouldBe 5
    }

    @Test
    fun `Should not be possible to set values lower than 1 or higher than 5`() {
        val rule = factory()

        shouldThrow<ParseException> {
            rule.spinner.value = 0
            rule.spinner.commitEdit()
        }

        shouldThrow<ParseException> {
            rule.spinner.value = 6
            rule.spinner.commitEdit()
        }
    }

    @Test
    fun `Should have the correct description`() {
        val rule = factory()
        rule.getDescription() shouldBe "Darts spaced by at most 1"

        rule.spinner.value = 4
        rule.getDescription() shouldBe "Darts spaced by at most 4"
    }

    @Test
    fun `Should write and read XML correctly`() {
        val rule = factory()
        rule.spinner.value = 3

        val xml = rule.toDbString()
        val deserialisedRule = parseAggregateRule(xml)

        deserialisedRule.shouldBeInstanceOf<DartzeeAggregateRuleCluster>()
        deserialisedRule.spinner.value shouldBe 3
    }

    @Test
    fun `Should not be valid if a dart has missed`() {
        factory().isValidRound(listOf(outerSingle(20), outerSingle(20), miss(20))) shouldBe false
    }

    @Test
    fun `Should not be valid if bullseye was hit`() {
        factory().isValidRound(listOf(outerSingle(20), outerSingle(20), bullseye)) shouldBe false
    }

    @Test
    fun `Should not be valid if a number is repeated`() {
        factory().isValidRound(listOf(outerSingle(20), outerSingle(20), outerSingle(3))) shouldBe
            false
    }

    private val oneApart = listOf(outerSingle(20), outerSingle(18), outerSingle(1))
    private val twoApart = listOf(outerSingle(20), double(4), treble(10))
    private val threeApart = listOf(outerSingle(19), treble(11), outerSingle(5))
    private val fourApart = listOf(outerSingle(10), outerSingle(1), outerSingle(14))
    private val fiveApart = listOf(outerSingle(7), double(12), outerSingle(13))

    @Test
    fun `Should validate correctly with spread of 1`() {
        factory().isValidRound(listOf(outerSingle(19), outerSingle(3), outerSingle(19))) shouldBe
            true
        factory().isValidRound(listOf(outerSingle(19), outerSingle(7), outerSingle(7))) shouldBe
            true
        factory().isValidRound(oneApart) shouldBe false
        factory().isValidRound(twoApart) shouldBe false
        factory().isValidRound(threeApart) shouldBe false
        factory().isValidRound(fourApart) shouldBe false
        factory().isValidRound(fiveApart) shouldBe false
    }

    @Test
    fun `Should validate correctly with spread of 2`() {
        val rule = factory().also { it.spinner.value = 2 }

        rule.isValidRound(listOf(outerSingle(1), outerSingle(20), outerSingle(5))) shouldBe true
        rule.isValidRound(listOf(outerSingle(5), outerSingle(20), outerSingle(5))) shouldBe true
        rule.isValidRound(listOf(outerSingle(12), outerSingle(20), outerSingle(18))) shouldBe false
        rule.isValidRound(listOf(outerSingle(5), outerSingle(20), outerSingle(18))) shouldBe false
    }

    @Test
    fun `Should validate correctly with spread of 4`() {
        val rule = factory().also { it.spinner.value = 3 }

        rule.isValidRound(listOf(outerSingle(1), outerSingle(20), outerSingle(5))) shouldBe true
        rule.isValidRound(listOf(outerSingle(5), outerSingle(20), outerSingle(5))) shouldBe true
        rule.isValidRound(listOf(outerSingle(12), outerSingle(20), outerSingle(18))) shouldBe false
        rule.isValidRound(listOf(outerSingle(5), outerSingle(20), outerSingle(18))) shouldBe true
    }
}
