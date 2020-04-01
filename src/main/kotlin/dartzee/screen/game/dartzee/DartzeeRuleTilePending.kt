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

    fun getSegmentStatus(darts: List<Dart>) = SegmentStatus(getScoringSegments(darts), getValidSegments(darts))

    fun getScoringSegments(darts: List<Dart>): Set<DartboardSegment>
    {
        if (darts.isEmpty())
        {
            return dto.calculationResult!!.scoringSegments.toSet()
        }
        else
        {
            val result = InjectedThings.dartzeeCalculator.getValidSegments(dto, darts)
            return result.scoringSegments.toSet()
        }
    }

    fun getValidSegments(darts: List<Dart>): Set<DartboardSegment>
    {
        if (darts.isEmpty())
        {
            return dto.calculationResult!!.validSegments.toSet()
        }
        else
        {
            val result = InjectedThings.dartzeeCalculator.getValidSegments(dto, darts)
            return result.validSegments.toSet()
        }
    }

    override fun getScoreForHover() = pendingScore
}