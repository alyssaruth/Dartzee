package dartzee.dartzee.dart

import dartzee.bullseye
import dartzee.dartzee.AbstractDartzeeRuleTest
import dartzee.dartzee.parseDartRule
import dartzee.doubleNineteen
import dartzee.doubleTwenty
import dartzee.helper.makeColourRule
import dartzee.missTwenty
import dartzee.outerBull
import dartzee.singleNineteen
import dartzee.singleTwenty
import dartzee.trebleNineteen
import dartzee.trebleTwenty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import javax.swing.JCheckBox
import org.junit.jupiter.api.Test

class TestDartzeeDartRuleColour : AbstractDartzeeRuleTest<DartzeeDartRuleColour>() {
    override val emptyIsValid = false

    override fun factory() = DartzeeDartRuleColour()

    @Test
    fun `segment validation - black`() {
        val rule = DartzeeDartRuleColour()
        rule.black = true

        rule.isValidSegment(singleTwenty) shouldBe true
        rule.isValidSegment(doubleTwenty) shouldBe false
        rule.isValidSegment(trebleTwenty) shouldBe false
        rule.isValidSegment(singleNineteen) shouldBe false
        rule.isValidSegment(doubleNineteen) shouldBe false
        rule.isValidSegment(trebleNineteen) shouldBe false
        rule.isValidSegment(missTwenty) shouldBe false
        rule.isValidSegment(bullseye) shouldBe false
        rule.isValidSegment(outerBull) shouldBe false
    }

    @Test
    fun `segment validation - white`() {
        val rule = DartzeeDartRuleColour()
        rule.white = true

        rule.isValidSegment(singleTwenty) shouldBe false
        rule.isValidSegment(doubleTwenty) shouldBe false
        rule.isValidSegment(trebleTwenty) shouldBe false
        rule.isValidSegment(singleNineteen) shouldBe true
        rule.isValidSegment(doubleNineteen) shouldBe false
        rule.isValidSegment(trebleNineteen) shouldBe false
        rule.isValidSegment(missTwenty) shouldBe false
        rule.isValidSegment(bullseye) shouldBe false
        rule.isValidSegment(outerBull) shouldBe false
    }

    @Test
    fun `segment validation - green`() {
        val rule = DartzeeDartRuleColour()
        rule.green = true

        rule.isValidSegment(singleTwenty) shouldBe false
        rule.isValidSegment(doubleTwenty) shouldBe false
        rule.isValidSegment(trebleTwenty) shouldBe false
        rule.isValidSegment(singleNineteen) shouldBe false
        rule.isValidSegment(doubleNineteen) shouldBe true
        rule.isValidSegment(trebleNineteen) shouldBe true
        rule.isValidSegment(missTwenty) shouldBe false
        rule.isValidSegment(bullseye) shouldBe false
        rule.isValidSegment(outerBull) shouldBe true
    }

    @Test
    fun `segment validation - red`() {
        val rule = DartzeeDartRuleColour()
        rule.red = true

        rule.isValidSegment(singleTwenty) shouldBe false
        rule.isValidSegment(doubleTwenty) shouldBe true
        rule.isValidSegment(trebleTwenty) shouldBe true
        rule.isValidSegment(singleNineteen) shouldBe false
        rule.isValidSegment(doubleNineteen) shouldBe false
        rule.isValidSegment(trebleNineteen) shouldBe false
        rule.isValidSegment(missTwenty) shouldBe false
        rule.isValidSegment(bullseye) shouldBe true
        rule.isValidSegment(outerBull) shouldBe false
    }

    @Test
    fun `segment validation - combination`() {
        val rule = DartzeeDartRuleColour()
        rule.red = true
        rule.black = true

        rule.isValidSegment(singleTwenty) shouldBe true
        rule.isValidSegment(doubleTwenty) shouldBe true
        rule.isValidSegment(trebleTwenty) shouldBe true
        rule.isValidSegment(singleNineteen) shouldBe false
        rule.isValidSegment(doubleNineteen) shouldBe false
        rule.isValidSegment(trebleNineteen) shouldBe false
        rule.isValidSegment(missTwenty) shouldBe false
        rule.isValidSegment(bullseye) shouldBe true
        rule.isValidSegment(outerBull) shouldBe false
    }

    @Test
    fun `colour config panel updates rule correctly`() {
        val rule = DartzeeDartRuleColour()
        val panel = rule.configPanel

        val checkBoxes: List<JCheckBox> = panel.components.filterIsInstance(JCheckBox::class.java)

        val cbBlack = checkBoxes.find { it.text == "Black" }
        val cbWhite = checkBoxes.find { it.text == "White" }
        val cbGreen = checkBoxes.find { it.text == "Green" }
        val cbRed = checkBoxes.find { it.text == "Red" }

        cbBlack.shouldNotBeNull()
        cbWhite.shouldNotBeNull()
        cbGreen.shouldNotBeNull()
        cbRed.shouldNotBeNull()

        for (i in 0..15) {
            cbBlack.isSelected = (i and 1) > 0
            cbWhite.isSelected = (i and 2) > 0
            cbRed.isSelected = (i and 4) > 0
            cbGreen.isSelected = (i and 8) > 0

            rule.actionPerformed(null)

            cbBlack.isSelected shouldBe rule.black
            cbWhite.isSelected shouldBe rule.white
            cbRed.isSelected shouldBe rule.red
            cbGreen.isSelected shouldBe rule.green
        }
    }

    @Test
    fun `read and write XML`() {
        val rules = getAllValidColorPermutations()
        rules.forEach {
            val red = it.red
            val green = it.green
            val black = it.black
            val white = it.white

            val xml = it.toDbString()

            val rule = parseDartRule(xml) as DartzeeDartRuleColour
            rule.red shouldBe red
            rule.green shouldBe green
            rule.black shouldBe black
            rule.white shouldBe white

            rule.cbRed.isSelected shouldBe red
            rule.cbGreen.isSelected shouldBe green
            rule.cbBlack.isSelected shouldBe black
            rule.cbWhite.isSelected shouldBe white
        }
    }

    @Test
    fun `all non-empty colour rules are valid`() {
        val rules = getAllValidColorPermutations()

        rules.forEach { it.validate().shouldBeEmpty() }
    }

    private fun getAllValidColorPermutations(): MutableList<DartzeeDartRuleColour> {
        val list = mutableListOf<DartzeeDartRuleColour>()

        for (i in 1..15) {
            val rule = DartzeeDartRuleColour()
            rule.black = (i and 1) > 0
            rule.white = (i and 2) > 0
            rule.red = (i and 4) > 0
            rule.green = (i and 8) > 0

            list.add(rule)
        }

        return list
    }

    @Test
    fun `rule description`() {
        val rule = makeColourRule(red = true, black = true)
        rule.getDescription() shouldBe "R/B"

        rule.white = true
        rule.green = true
        rule.getDescription() shouldBe "Any"

        rule.red = false
        rule.getDescription() shouldBe "G/B/W"
    }

    @Test
    fun `should update from UI interaction`() {
        val rule = makeColourRule()
        rule.cbBlack.doClick()
        rule.black shouldBe true

        rule.cbWhite.doClick()
        rule.white shouldBe true

        rule.cbRed.doClick()
        rule.red shouldBe true

        rule.cbGreen.doClick()
        rule.green shouldBe true
    }
}
