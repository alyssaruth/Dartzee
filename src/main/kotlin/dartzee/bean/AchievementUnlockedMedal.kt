package dartzee.bean

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.paintMedalCommon
import dartzee.utils.ResourceCache
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JComponent
import javax.swing.JLabel

private const val MEDAL_SIZE = 164

class AchievementUnlockedMedal(val achievement : AbstractAchievement): JComponent()
{
    init
    {
        preferredSize = Dimension(MEDAL_SIZE, MEDAL_SIZE)
    }

    override fun paint(g: Graphics?)
    {
        if (g is Graphics2D)
        {
            paintMedalCommon(g, achievement, MEDAL_SIZE, false)

            val icon = achievement.getIcon()
            icon?.let {
                val x = (MEDAL_SIZE / 2) - (icon.width / 2)
                g.drawImage(icon, null, x, 30)
            }

            val label = JLabel(achievement.getProgressDesc())
            label.setSize(MEDAL_SIZE, 25)
            label.font = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 24f)
            label.horizontalAlignment = JLabel.CENTER
            label.foreground = achievement.getColor(false).darker()

            g.translate(0, 100)
            label.paint(g)
        }
    }
}