package burlton.dartzee.code.screen.dartzee

import burlton.dartzee.code.dartzee.DartzeeRuleDto
import burlton.dartzee.code.core.bean.AbstractTableRenderer
import java.awt.Color
import java.awt.Rectangle
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.SwingConstants

class DartzeeTemplateRuleRenderer : AbstractTableRenderer<List<DartzeeRuleDto>>()
{
    override fun getReplacementValue(value: List<DartzeeRuleDto>): Any
    {
        val width = (20 * value.size) + (5 * (value.size))

        val bi = BufferedImage(width + 2, 22, BufferedImage.TYPE_INT_ARGB)
        val g = bi.createGraphics()

        value.forEachIndexed { ix, dto ->
            val x = (ix * 25) + 5

            //Fill
            g.color = dto.calculationResult!!.getForeground()
            g.fill(Rectangle(x, 0, 20, 20))

            //Border
            g.color = Color.BLACK
            g.drawRect(x, 0, 20, 20)
        }

        g.dispose()
        icon = ImageIcon(bi)
        text = ""
        horizontalAlignment = SwingConstants.LEFT
        toolTipText = "${value.size} rules"

        return this
    }

    override fun toString() = ""
}