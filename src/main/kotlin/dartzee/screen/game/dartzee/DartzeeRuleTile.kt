package dartzee.screen.game.dartzee

import dartzee.bean.IMouseListener
import dartzee.core.util.setFontSize
import dartzee.dartzee.DartzeeRuleDto
import java.awt.Dimension
import java.awt.event.MouseEvent
import javax.swing.JButton
import kotlin.math.abs
import org.jfree.chart.imagemap.ImageMapUtils

abstract class DartzeeRuleTile(val dto: DartzeeRuleDto, val ruleNumber: Int) :
    JButton(), IMouseListener {
    init {
        preferredSize = Dimension(150, 80)
        text = getButtonText(false)

        addMouseListener(this)
    }

    abstract fun getScoreForHover(): Int?

    private fun getButtonText(hovered: Boolean = false): String {
        val ruleDesc = ImageMapUtils.htmlEscape(dto.generateRuleDescription())
        val ruleNameOrDesc = dto.ruleName?.let { ImageMapUtils.htmlEscape(it) } ?: ruleDesc

        val nonHoverHtml =
            "<html><center><b>#$ruleNumber <br /><br /> $ruleNameOrDesc</b></center></html>"
        val hoverHtml = "<html><center><b>#$ruleNumber <br /><br /> $ruleDesc</b></center></html>"
        val scoreHtml = "<html><center><b>${getScoreText()}</b></center></html>"

        return when {
            hovered && getScoreForHover() != null -> scoreHtml
            hovered -> hoverHtml
            else -> nonHoverHtml
        }
    }

    private fun getScoreText(): String {
        val score = getScoreForHover() ?: return ""

        val prefix = if (score > 0) "+" else "-"

        return "$prefix ${abs(score)}"
    }

    override fun mouseEntered(e: MouseEvent) {
        text = getButtonText(true)

        if (getScoreForHover() != null) {
            setFontSize(24)
        }
    }

    override fun mouseExited(e: MouseEvent) {
        text = getButtonText()
        setFontSize(12)
    }
}
