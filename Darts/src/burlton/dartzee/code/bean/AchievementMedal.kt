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
    var achievement = achievement
    var angle = 0.0
    var highlighted = false
    var gameIdEarned = -1L

    init
    {
        preferredSize = Dimension(200, 200)
        angle = achievement.getAngle()
        gameIdEarned = achievement.gameIdEarned

        addMouseListener(this)
        addMouseMotionListener(this)
    }

    override fun paint(g: Graphics?)
    {
        if (g is Graphics2D)
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            //Draw the track
            g.color = Color.DARK_GRAY.brighter()
            g.fillArc(0, 0, 200, 200, 0, 360)

            //Mark the levels
            markThreshold(g, Color.MAGENTA, achievement.pinkThreshold)
            markThreshold(g, Color.CYAN, achievement.blueThreshold)
            markThreshold(g, Color.GREEN, achievement.greenThreshold)
            markThreshold(g, Color.YELLOW, achievement.yellowThreshold)
            markThreshold(g, Color.ORANGE, achievement.orangeThreshold)
            markThreshold(g, Color.RED, achievement.redThreshold)

            //Draw an inner track?
            //g.color = Color.DARK_GRAY.brighter()
            //g.fillArc(13, 13, 174, 174, 0, 360)

            //Draw the actual progress
            g.color = achievement.getColor(highlighted).darker()
            g.fillArc(0, 0, 200, 200, 90, -angle.toInt())

            //Inner circle
            g.color = achievement.getColor(highlighted)

            g.fillArc(15, 15, 170, 170, 0, 360)
        }
    }

    private fun markThreshold(g : Graphics2D, color : Color, threshold : Int)
    {
        g.color = color
        val thresholdAngle = (360 * threshold.toDouble() / achievement.maxValue)
        //g.fillArc(2, 2, 196, 196, 90 - thresholdAngle.toInt(), 3)
        g.fillArc(0, 0, 200, 200, 90 - thresholdAngle.toInt(), 3)
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
