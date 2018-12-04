package burlton.dartzee.code.bean

import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.screen.game.DartsGameScreen
import burlton.dartzee.code.utils.GeometryUtil
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.JComponent

class AchievementMedal (achievement : AbstractAchievement) : JComponent(), MouseListener, MouseMotionListener
{
    var color: Color = Color.GRAY
    var angle = 0.0
    var highlighted = false
    var gameIdEarned = -1L

    init
    {
        preferredSize = Dimension(200, 200)
        color = achievement.getColor()
        angle = achievement.getAngle()
        gameIdEarned = achievement.gameIdEarned

        addMouseListener(this)
        addMouseMotionListener(this)
    }

    override fun paint(g: Graphics?)
    {
        if (g is Graphics2D)
        {
            g.color = color.darker()

            if (highlighted)
            {
                g.color = g.color.darker()
            }

            g.fillArc(0, 0, 200, 200, 90, -angle.toInt())

            g.color = color

            if (highlighted)
            {
                g.color = g.color.darker()
            }

            g.fillArc(15, 15, 170, 170, 0, 360)
        }
    }

    /**
     * MouseListener
     */
    override fun mouseReleased(e: MouseEvent?) {}
    override fun mouseClicked(e: MouseEvent?)
    {
        if (gameIdEarned > -1)
        {
            DartsGameScreen.loadAndDisplayGame(gameIdEarned)
        }
    }
    override fun mousePressed(e: MouseEvent?) {}

    override fun mouseEntered(e: MouseEvent?)
    {
        val pt = e?.point
        highlighted = GeometryUtil.getDistance(pt, Point(100, 100)) < 100

        repaint()
    }

    override fun mouseExited(e: MouseEvent?)
    {
        highlighted = false
        repaint()
    }

    /**
     * MouseMotionListener
     */
    override fun mouseMoved(e: MouseEvent?)
    {
        val pt = e?.point
        highlighted = GeometryUtil.getDistance(pt, Point(100, 100)) < 100

        repaint()
    }

    override fun mouseDragged(e: MouseEvent?) {}
}
