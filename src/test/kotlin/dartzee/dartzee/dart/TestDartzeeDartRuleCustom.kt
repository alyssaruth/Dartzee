package dartzee.dartzee.dart

import dartzee.dartzee.AbstractDartzeeRuleTest
import dartzee.dartzee.parseDartRule
import dartzee.doubleTwenty
import dartzee.helper.FakeDartzeeSegmentFactory
import dartzee.outerBull
import dartzee.singleTwenty
import dartzee.trebleNineteen
import dartzee.trebleTwenty
import dartzee.utils.InjectedThings
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import io.mockk.verify
import java.awt.event.ActionListener
import org.junit.jupiter.api.Test

class TestDartzeeDartRuleCustom : AbstractDartzeeRuleTest<DartzeeDartRuleCustom>() {
    override val emptyIsValid = false

    override fun factory() = DartzeeDartRuleCustom()

    @Test
    fun `a custom rule with at least one segment is valid`() {
        val rule = DartzeeDartRuleCustom()
        rule.segments.add(doubleTwenty)

        rule.validate().shouldBeEmpty()
    }

    @Test
    fun `description should return Custom or the specified name`() {
        val rule = factory()
        rule.getDescription() shouldBe "Custom"

        rule.name = "Foo"
        rule.getDescription() shouldBe "Foo"
    }

    @Test
    fun `Should update the name variable when a new one is typed in`() {
        val rule = factory()
        rule.tfName.text = "Foo"

        rule.actionPerformed(null)
        rule.name shouldBe "Foo"
    }

    @Test
    fun `segment validation`() {
        val rule = DartzeeDartRuleCustom()
        rule.segments.addAll(setOf(doubleTwenty, trebleNineteen))

        rule.isValidSegment(doubleTwenty) shouldBe true
        rule.isValidSegment(trebleNineteen) shouldBe true
        rule.isValidSegment(trebleTwenty) shouldBe false
    }

    @Test
    fun `Read and write XML`() {
        val rule = DartzeeDartRuleCustom()

        rule.segments.addAll(setOf(doubleTwenty, outerBull, trebleNineteen))
        rule.name = "Foo"

        val xml = rule.toDbString()
        val parsedRule = parseDartRule(xml)

        parsedRule.shouldBeInstanceOf<DartzeeDartRuleCustom>()
        parsedRule.segments shouldHaveSize (3)

        parsedRule.isValidSegment(doubleTwenty) shouldBe true
        parsedRule.isValidSegment(singleTwenty) shouldBe false
        parsedRule.name shouldBe "Foo"
        parsedRule.tfName.text shouldBe "Foo"
    }

    @Test
    fun `Should propagate action events to other listeners`() {
        val rule = DartzeeDartRuleCustom()
        val listener = mockk<ActionListener>(relaxed = true)

        rule.btnConfigure.addActionListener(listener)

        rule.actionPerformed(null)

        verify { listener.actionPerformed(null) }
    }

    @Test
    fun `Should pop up segment selector and update accordingly`() {
        val fakeFactory = FakeDartzeeSegmentFactory(hashSetOf(trebleTwenty, trebleNineteen))
        InjectedThings.dartzeeSegmentFactory = fakeFactory

        val rule = DartzeeDartRuleCustom()
        rule.btnConfigure.doClick()

        fakeFactory.segmentsPassedIn.shouldBeEmpty()
        rule.segments.shouldContainExactlyInAnyOrder(trebleTwenty, trebleNineteen)

        val fakeFactoryTwo = FakeDartzeeSegmentFactory(hashSetOf(singleTwenty))
        InjectedThings.dartzeeSegmentFactory = fakeFactoryTwo

        rule.btnConfigure.doClick()
        rule.segments.shouldContainExactly(singleTwenty)
        fakeFactoryTwo.segmentsPassedIn.shouldContainExactlyInAnyOrder(trebleTwenty, trebleNineteen)
    }
}
