package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.utils.InjectedThings
import burlton.dartzee.code.utils.setColoursForDartzeeResult
import burlton.desktopcore.code.util.setFontSize
import org.jfree.chart.imagemap.ImageMapUtilities
import java.awt.Dimension
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JButton
import kotlin.math.abs

open class DartzeeRuleTile(val dto: DartzeeRuleDto, val ruleNumber: Int): JButton(), MouseListener
{
    var pendingResult: Boolean? = null
    var pendingScore: Int? = null

    init
    {
        preferredSize = Dimension(150, 80)
        text = getButtonText(false)

        addMouseListener(this)
    }

    fun setPendingResult(success: Boolean, score: Int)
    {
        pendingResult = success
        pendingScore = score

        text = getButtonText()

        repaint()

        setColoursForDartzeeResult(success)
    }

    private fun getButtonText(hovered: Boolean = false) =
        if (hovered) "<html><center><b>${getScoreText()}</b></center></html>"
        else
        {
            val ruleDesc = ImageMapUtilities.htmlEscape(dto.generateRuleDescription())
            "<html><center><b>#$ruleNumber <br /><br /> $ruleDesc</b></center></html>"
        }

    private fun getScoreText(): String
    {
        val score = pendingScore ?: return ""

        val prefix = if (pendingResult == true) "+" else "-"

        return "$prefix ${abs(score)}"
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

    override fun mouseEntered(e: MouseEvent?)
    {
        if (pendingScore != null) {
            text = getButtonText(true)
            setFontSize(24)
        }

    }

    override fun mouseExited(e: MouseEvent?)
    {
        text = getButtonText()
        setFontSize(12)
    }

    override fun mouseClicked(e: MouseEvent?) {}
    override fun mouseReleased(e: MouseEvent?) {}
    override fun mousePressed(e: MouseEvent?) {}
}