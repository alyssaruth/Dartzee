package dartzee.screen.game.dartzee

import dartzee.core.util.setFontSize
import dartzee.dartzee.DartzeeRuleDto
import org.jfree.chart.imagemap.ImageMapUtilities
import java.awt.Dimension
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JButton
import kotlin.math.abs

abstract class DartzeeRuleTile(val dto: DartzeeRuleDto, val ruleNumber: Int): JButton(), MouseListener
{
    init
    {
        preferredSize = Dimension(150, 80)
        text = getButtonText(false)

        addMouseListener(this)
    }

    abstract fun getScoreForHover(): Int?

    protected fun getButtonText(hovered: Boolean = false) =
        if (hovered) "<html><center><b>${getScoreText()}</b></center></html>"
        else
        {
            val ruleDesc = ImageMapUtilities.htmlEscape(dto.generateRuleDescription())
            "<html><center><b>#$ruleNumber <br /><br /> $ruleDesc</b></center></html>"
        }

    private fun getScoreText(): String
    {
        val score = getScoreForHover() ?: return ""

        val prefix = if (score > 0) "+" else "-"

        return "$prefix ${abs(score)}"
    }

    override fun mouseEntered(e: MouseEvent?)
    {
        if (getScoreForHover() != null) {
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