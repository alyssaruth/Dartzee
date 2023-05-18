package dartzee.screen.game.dartzee

import dartzee.dartzee.DartzeeRuleDto
import dartzee.`object`.Dart
import dartzee.screen.game.SegmentStatuses
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
        isVisible = getSegmentStatus(darts).validSegments.isNotEmpty()
    }

    fun getSegmentStatus(darts: List<Dart>): SegmentStatuses
    {
        val result = if (darts.isEmpty()) dto.calculationResult!! else InjectedThings.dartzeeCalculator.getValidSegments(dto, darts)
        return result.getSegmentStatus()
    }

    override fun getScoreForHover() = pendingScore
}