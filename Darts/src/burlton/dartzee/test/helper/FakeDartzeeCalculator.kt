package burlton.dartzee.test.helper

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.dartzee.AbstractDartzeeCalculator
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.dartzee.ValidSegmentCalculationResult
import burlton.dartzee.code.screen.Dartboard

class FakeDartzeeCalculator: AbstractDartzeeCalculator()
{
    override fun getValidSegments(rule: DartzeeRuleDto, dartboard: Dartboard, dartsSoFar: List<Dart>): ValidSegmentCalculationResult
    {
        return ValidSegmentCalculationResult(dartboard.getAllSegments(), 10, 10, 1.0, 1.0)
    }
}