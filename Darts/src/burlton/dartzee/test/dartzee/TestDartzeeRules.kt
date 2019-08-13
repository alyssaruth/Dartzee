package burlton.dartzee.test.dartzee

import burlton.dartzee.code.`object`.*
import burlton.dartzee.code.bean.SpinnerSingleSelector
import burlton.dartzee.code.dartzee.*
import burlton.dartzee.code.dartzee.dart.*
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.Test
import javax.swing.JCheckBox
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestDartzeeRules: AbstractDartsTest()
{
    private val bullseye = DartboardSegmentKt("25_$SEGMENT_TYPE_DOUBLE")
    private val outerBull = DartboardSegmentKt("25_$SEGMENT_TYPE_OUTER_SINGLE")
    private val innerSingle = DartboardSegmentKt("20_$SEGMENT_TYPE_INNER_SINGLE")
    private val outerSingle = DartboardSegmentKt("15_$SEGMENT_TYPE_OUTER_SINGLE")
    private val miss = DartboardSegmentKt("20_$SEGMENT_TYPE_MISS")
    private val missedBoard = DartboardSegmentKt("15_$SEGMENT_TYPE_MISSED_BOARD")

    private val singleTwenty = DartboardSegmentKt("20_$SEGMENT_TYPE_INNER_SINGLE")
    private val doubleTwenty = DartboardSegmentKt("20_$SEGMENT_TYPE_DOUBLE")
    private val trebleTwenty = DartboardSegmentKt("20_$SEGMENT_TYPE_TREBLE")
    private val singleNineteen = DartboardSegmentKt("19_$SEGMENT_TYPE_OUTER_SINGLE")
    private val doubleNineteen = DartboardSegmentKt("19_$SEGMENT_TYPE_DOUBLE")
    private val trebleNineteen = DartboardSegmentKt("19_$SEGMENT_TYPE_TREBLE")

    @Test
    fun `segment validation for inner rule`()
    {
        val rule = DartzeeDartRuleInner()

        assertTrue(rule.isValidSegment(bullseye))
        assertTrue(rule.isValidSegment(outerBull))
        assertTrue(rule.isValidSegment(innerSingle))
        assertTrue(rule.isValidSegment(trebleNineteen))
        assertFalse(rule.isValidSegment(outerSingle))
        assertFalse(rule.isValidSegment(doubleTwenty))
        assertFalse(rule.isValidSegment(miss))
        assertFalse(rule.isValidSegment(missedBoard))
    }

    @Test
    fun `segment validation for outer rule`()
    {
        val rule = DartzeeDartRuleOuter()

        assertFalse(rule.isValidSegment(bullseye))
        assertFalse(rule.isValidSegment(outerBull))
        assertFalse(rule.isValidSegment(innerSingle))
        assertFalse(rule.isValidSegment(trebleNineteen))
        assertTrue(rule.isValidSegment(outerSingle))
        assertTrue(rule.isValidSegment(doubleTwenty))
        assertFalse(rule.isValidSegment(miss))
        assertFalse(rule.isValidSegment(missedBoard))
    }

    @Test
    fun `segment validation for even rule`()
    {
        val rule = DartzeeDartRuleEven()

        val doubleNineteen = DartboardSegmentKt("19_$SEGMENT_TYPE_DOUBLE")
        val trebleTwenty = DartboardSegmentKt("20_$SEGMENT_TYPE_TREBLE")
        val singleNineteen = DartboardSegmentKt("19_$SEGMENT_TYPE_OUTER_SINGLE")
        val miss = DartboardSegmentKt("20_$SEGMENT_TYPE_MISS")
        val missedBoard = DartboardSegmentKt("16_$SEGMENT_TYPE_MISSED_BOARD")

        assertFalse(rule.isValidSegment(doubleNineteen))
        assertTrue(rule.isValidSegment(trebleTwenty))
        assertFalse(rule.isValidSegment(outerBull))
        assertFalse(rule.isValidSegment(singleNineteen))
        assertFalse(rule.isValidSegment(miss))
        assertFalse(rule.isValidSegment(missedBoard))
    }

    @Test
    fun `segment validation for odd rule`()
    {
        val rule = DartzeeDartRuleOdd()

        val doubleNineteen = DartboardSegmentKt("19_$SEGMENT_TYPE_DOUBLE")
        val trebleTwenty = DartboardSegmentKt("20_$SEGMENT_TYPE_TREBLE")
        val singleNineteen = DartboardSegmentKt("19_$SEGMENT_TYPE_OUTER_SINGLE")
        val miss = DartboardSegmentKt("19_$SEGMENT_TYPE_MISS")
        val missedBoard = DartboardSegmentKt("13_$SEGMENT_TYPE_MISSED_BOARD")

        assertTrue(rule.isValidSegment(doubleNineteen))
        assertFalse(rule.isValidSegment(trebleTwenty))
        assertTrue(rule.isValidSegment(singleNineteen))
        assertFalse(rule.isValidSegment(miss))
        assertFalse(rule.isValidSegment(missedBoard))
        assertTrue(rule.isValidSegment(outerBull))
    }

    @Test
    fun `simple rules are always valid`()
    {
        val rule = DartzeeDartRuleOdd()

        assertTrue(rule.validate().isEmpty())
    }

    @Test
    fun `can't create empty colour rule`()
    {
        val rule = DartzeeDartRuleColour()

        assertFalse(rule.validate().isEmpty())
    }

    @Test
    fun `can't create empty custom rule`()
    {
        val rule = DartzeeDartRuleCustom()

        assertFalse(rule.validate().isEmpty())
    }

    @Test
    fun `a custom rule with at least one segment is valid`()
    {
        val rule = DartzeeDartRuleCustom()
        rule.segments = hashSetOf(doubleTwenty)

        assertTrue(rule.validate().isEmpty())
    }


    @Test
    fun `all non-empty colour rules are valid`()
    {
        val rules = getAllValidColorPermutations()

        rules.forEach{
            assertTrue(it.validate().isEmpty())
        }
    }

    private fun getAllValidColorPermutations(): MutableList<DartzeeDartRuleColour>
    {
        val list = mutableListOf<DartzeeDartRuleColour>()

        for (i in 1..15)
        {
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
    fun `segment validation - black`()
    {
        val rule = DartzeeDartRuleColour()
        rule.black = true

        assertTrue(rule.isValidSegment(singleTwenty))
        assertFalse(rule.isValidSegment(doubleTwenty))
        assertFalse(rule.isValidSegment(trebleTwenty))
        assertFalse(rule.isValidSegment(singleNineteen))
        assertFalse(rule.isValidSegment(doubleNineteen))
        assertFalse(rule.isValidSegment(trebleNineteen))
        assertFalse(rule.isValidSegment(miss))
        assertFalse(rule.isValidSegment(missedBoard))
        assertFalse(rule.isValidSegment(bullseye))
        assertFalse(rule.isValidSegment(outerBull))
    }

    @Test
    fun `segment validation - white`()
    {
        val rule = DartzeeDartRuleColour()
        rule.white = true

        assertFalse(rule.isValidSegment(singleTwenty))
        assertFalse(rule.isValidSegment(doubleTwenty))
        assertFalse(rule.isValidSegment(trebleTwenty))
        assertTrue(rule.isValidSegment(singleNineteen))
        assertFalse(rule.isValidSegment(doubleNineteen))
        assertFalse(rule.isValidSegment(trebleNineteen))
        assertFalse(rule.isValidSegment(miss))
        assertFalse(rule.isValidSegment(missedBoard))
        assertFalse(rule.isValidSegment(bullseye))
        assertFalse(rule.isValidSegment(outerBull))
    }

    @Test
    fun `segment validation - green`()
    {
        val rule = DartzeeDartRuleColour()
        rule.green = true

        assertFalse(rule.isValidSegment(singleTwenty))
        assertFalse(rule.isValidSegment(doubleTwenty))
        assertFalse(rule.isValidSegment(trebleTwenty))
        assertFalse(rule.isValidSegment(singleNineteen))
        assertTrue(rule.isValidSegment(doubleNineteen))
        assertTrue(rule.isValidSegment(trebleNineteen))
        assertFalse(rule.isValidSegment(miss))
        assertFalse(rule.isValidSegment(missedBoard))
        assertFalse(rule.isValidSegment(bullseye))
        assertTrue(rule.isValidSegment(outerBull))
    }

    @Test
    fun `segment validation - red`()
    {
        val rule = DartzeeDartRuleColour()
        rule.red = true

        assertFalse(rule.isValidSegment(singleTwenty))
        assertTrue(rule.isValidSegment(doubleTwenty))
        assertTrue(rule.isValidSegment(trebleTwenty))
        assertFalse(rule.isValidSegment(singleNineteen))
        assertFalse(rule.isValidSegment(doubleNineteen))
        assertFalse(rule.isValidSegment(trebleNineteen))
        assertFalse(rule.isValidSegment(miss))
        assertFalse(rule.isValidSegment(missedBoard))
        assertTrue(rule.isValidSegment(bullseye))
        assertFalse(rule.isValidSegment(outerBull))
    }

    @Test
    fun `segment validation - score`()
    {
        val rule = DartzeeDartRuleScore()
        rule.score = 20

        assertTrue(rule.isValidSegment(singleTwenty))
        assertTrue(rule.isValidSegment(doubleTwenty))
        assertTrue(rule.isValidSegment(trebleTwenty))
        assertFalse(rule.isValidSegment(miss))
    }

    @Test
    fun `no rules should have overlapping identifiers`()
    {
        val rules = getAllDartRules()

        val nameCount = rules.map{ it.getRuleIdentifier() }.distinct().count()

        assertEquals(nameCount, rules.size)
    }

    @Test
    fun `sensible toString implementation`()
    {
        val rule = DartzeeDartRuleOuter()

        rule.getRuleIdentifier() shouldBe "$rule"
    }

    @Test
    fun `invalid XML should return null rule`()
    {
        val rule = parseDartRule("BAD")
        rule shouldBe null
    }

    @Test
    fun `invalid identifier in XML should return null rule`()
    {
        val rule = parseDartRule("<Broken/>")
        rule shouldBe null
    }

    @Test
    fun `parse all rules from atomic tags`()
    {
        val rules = getAllDartRules()
        rules.forEach{
            val identifier = it.getRuleIdentifier()
            val tag = "<$identifier/>"

            val rule = parseDartRule(tag)!!
            rule.getRuleIdentifier() shouldBe identifier
        }
    }

    @Test
    fun `write colour XML`()
    {
        val rules = getAllValidColorPermutations()
        rules.forEach{
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
        }
    }

    @Test
    fun `write score XML`()
    {
        val rule = DartzeeDartRuleScore()
        rule.score = 20

        val xml = rule.toDbString()
        val parsedRule = parseDartRule(xml) as DartzeeDartRuleScore

        parsedRule.score shouldBe 20
    }

    @Test
    fun `write custom XML`()
    {
        val rule = DartzeeDartRuleCustom()

        rule.segments = hashSetOf(doubleTwenty, outerBull, trebleNineteen)

        val xml = rule.toDbString()
        val parsedRule = parseDartRule(xml)

        assertTrue(parsedRule is DartzeeDartRuleCustom)

        parsedRule.segments shouldHaveSize(3)

        parsedRule.isValidSegment(doubleTwenty) shouldBe true
        parsedRule.isValidSegment(singleTwenty) shouldBe false
    }

    @Test
    fun `write simple XML`()
    {
        val rule = DartzeeDartRuleEven()
        val xml = rule.toDbString()
        val parsedRule = parseDartRule(xml)!!

        parsedRule.shouldBeInstanceOf<DartzeeDartRuleEven>()
    }

    @Test
    fun `colour config panel updates rule correctly`()
    {
        val rule = DartzeeDartRuleColour()
        val panel = rule.configPanel

        val checkBoxes: List<JCheckBox> = panel.components.filterIsInstance(JCheckBox::class.java)

        val cbBlack = checkBoxes.find{it.text == "Black"}
        val cbWhite = checkBoxes.find{it.text == "White"}
        val cbGreen = checkBoxes.find{it.text == "Green"}
        val cbRed = checkBoxes.find{it.text == "Red"}

        assertNotNull(cbBlack)
        assertNotNull(cbWhite)
        assertNotNull(cbGreen)
        assertNotNull(cbRed)

        for (i in 0..15)
        {
            cbBlack.isSelected = (i and 1) > 0
            cbWhite.isSelected = (i and 2) > 0
            cbRed.isSelected = (i and 4) > 0
            cbGreen.isSelected = (i and 8) > 0

            rule.actionPerformed(null)

            assertEquals(cbBlack.isSelected, rule.black)
            assertEquals(cbWhite.isSelected, rule.white)
            assertEquals(cbRed.isSelected, rule.red)
            assertEquals(cbGreen.isSelected, rule.green)
        }
    }

    @Test
    fun `Score config panel updates rule correctly`()
    {
        val rule = DartzeeDartRuleScore()

        val panel = rule.configPanel

        val spinner = panel.components.filterIsInstance(SpinnerSingleSelector::class.java).first()

        assertNotNull(spinner)

        assertEquals(spinner.value, rule.score)
        assertTrue(rule.score > -1)

        for (i in 1..25)
        {
            spinner.value = i
            rule.stateChanged(null)

            assertEquals(spinner.value, rule.score)
            assertFalse(rule.score in 21..24)
        }
    }
}
