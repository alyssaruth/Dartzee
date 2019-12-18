package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.utils.InjectedThings
import burlton.dartzee.code.utils.setColoursForDartzeeResult

class DartzeeRuleTilePending(dto: DartzeeRuleDto, ruleNumber: Int): DartzeeRuleTile(dto, ruleNumber)
{
    var pendingResult: Boolean? = null
    var pendingScore: Int? = null

    fun setPendingResult(success: Boolean, score: Int)
    {
        pendingResult = success
        pendingScore = score

        repaint()

        setColoursForDartzeeResult(success)
    }

    fun updateState(darts: List<Dart>)
    {
        isVisible = getValidSegments(darts).isNotEmpty()
    }

    fun getValidSegments(darts: List<Dart>): List<DartboardSegment>
    {
        if (darts.isEmpty())
        {
            return dto.calculationResult!!.validSegments
        }
        else
        {
            val result = InjectedThings.dartzeeCalculator.getValidSegments(dto, darts)
            return result.validSegments
        }
    }

    override fun getScoreForHover() = pendingScore
}