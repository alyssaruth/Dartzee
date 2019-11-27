package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.utils.InjectedThings
import burlton.dartzee.code.utils.setColoursForDartzeeResult
import org.jfree.chart.imagemap.ImageMapUtilities
import java.awt.Dimension
import javax.swing.JButton
import kotlin.math.abs

open class DartzeeRuleTile(val dto: DartzeeRuleDto, val ruleNumber: Int): JButton()
{
    var pendingResult: Boolean? = null
    var pendingScore: Int? = null

    init
    {
        preferredSize = Dimension(150, 80)
        text = getButtonText()
    }

    fun setPendingResult(success: Boolean, score: Int)
    {
        pendingResult = success
        pendingScore = score
        isFocusable = false

        text = getButtonText()

        repaint()

        setColoursForDartzeeResult(success)
    }

    fun getButtonText(): String
    {
        val ruleDesc = ImageMapUtilities.htmlEscape(dto.generateRuleDescription())

        return "<html><center><b>#$ruleNumber <br /><br /> $ruleDesc ${getScoreText()}</b></center></html>"
    }
    private fun getScoreText(): String
    {
        val score = pendingScore ?: return ""

        val prefix = if (pendingResult == true) "+" else "-"

        return "<br /><br />$prefix ${abs(score)}"
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
}