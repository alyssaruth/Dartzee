package burlton.dartzee.code.bean

import burlton.dartzee.code.achievements.AbstractAchievement
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JComponent

class AchievementMedal (achievement : AbstractAchievement) : JComponent()
{
    var color : Color = Color.GRAY
    var angle = 0.0

    init
    {
        preferredSize = Dimension(200, 200)
        color = achievement.getColor()
        angle = achievement.getAngle()
    }

    override fun paint(g: Graphics?)
    {
        if (g is Graphics2D)
        {
            g.color = color
            g.fillArc(0, 0, 200, 200, 90, -angle.toInt())
            g.color = Color.BLACK
            g.drawArc(0, 0, 200, 200, 90, -angle.toInt())
        }
    }

}
