package burlton.dartzee.test.helper

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.`object`.SEGMENT_TYPE_INNER_SINGLE
import burlton.dartzee.code.dartzee.AbstractDartzeeCalculator
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.dartzee.DartzeeRuleCalculationResult
import burlton.dartzee.code.screen.Dartboard
import burlton.dartzee.code.screen.dartzee.DartboardRuleVerifier

class FakeDartzeeCalculator: AbstractDartzeeCalculator()
{
    override fun getValidSegments(rule: DartzeeRuleDto, dartboard: Dartboard, dartsSoFar: List<Dart>): DartzeeRuleCalculationResult
    {
        val validSegment = dartboard.getFakeValidSegment(dartsSoFar.size)
        return DartzeeRuleCalculationResult(listOf(validSegment), 10, 10, 1.0, 1.0)
    }

    override fun isValidCombination(combination: List<DartboardSegment>, rule: DartzeeRuleDto): Boolean {
        return true
    }
}

fun Dartboard.getFakeValidSegment(dartsThrown: Int) = when (dartsThrown) {
    0 -> getSegment(1, SEGMENT_TYPE_INNER_SINGLE)!!
    1 -> getSegment(2, SEGMENT_TYPE_INNER_SINGLE)!!
    else -> getSegment(3, SEGMENT_TYPE_INNER_SINGLE)!!
}