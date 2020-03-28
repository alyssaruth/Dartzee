package dartzee.screen.game.dartzee

import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.dartzee.DartzeeRuleDto
import dartzee.utils.InjectedThings
import dartzee.utils.setColoursForDartzeeResult

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