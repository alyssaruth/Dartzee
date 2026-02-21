package dartzee.bean

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.paintMedalCommon
import dartzee.screen.ScreenCache
import dartzee.screen.stats.player.PlayerAchievementBreakdown
import dartzee.screen.stats.player.PlayerAchievementsScreen
import dartzee.utils.InjectedThings.gameLauncher
import dartzee.utils.ResourceCache
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JLabel

private const val SIZE = 132

class AchievementMedal(val achievement: AbstractAchievement) : JComponent(), IMouseListener {
    private var highlighted = false

    init {
        preferredSize = Dimension(SIZE, SIZE)

        addMouseListener(this)
        addMouseMotionListener(this)
    }

    override fun paint(g: Graphics?) {
        if (g is Graphics2D) {
            paintMedalCommon(g, achievement, SIZE, highlighted)

            if (!highlighted || achievement.isLocked()) {
                val y = (SIZE - 72) / 2
                val icon = achievement.getIcon()
                icon?.let {
                    val x = (SIZE / 2) - (icon.width / 2)
                    g.drawImage(icon, null, x, y)
                }
            } else {
                val y = (SIZE - 24) / 2
                val label = JLabel(achievement.getProgressDesc())
                label.setSize(SIZE, 24)
                label.font = ResourceCache.BASE_FONT.deriveFont(Font.PLAIN, 24f)
                label.horizontalAlignment = JLabel.CENTER
                label.foreground = achievement.getColor(highlighted).darker()

                g.translate(0, y)
                label.paint(g)
            }
        }
    }

    private fun updateForMouseOver(e: MouseEvent) {
        val pt = e.point
        highlighted = pt.distance(Point(SIZE / 2, SIZE / 2)) < SIZE / 2

        val currentScreen = ScreenCache.currentScreen()
        if (currentScreen is PlayerAchievementsScreen) {
            currentScreen.toggleAchievementDesc(highlighted, achievement)
        }

        cursor =
            if (highlighted && achievement.isClickable()) {
                Cursor(Cursor.HAND_CURSOR)
            } else {
                Cursor(Cursor.DEFAULT_CURSOR)
            }

        repaint()
    }

    override fun mouseReleased(e: MouseEvent) {
        if (achievement.tmBreakdown != null) {
            val scrn = ScreenCache.get<PlayerAchievementBreakdown>()
            scrn.setState(achievement)

            ScreenCache.switch(scrn)
        } else if (achievement.gameIdEarned.isNotEmpty()) {
            gameLauncher.loadAndDisplayGame(achievement.gameIdEarned)
        }
    }

    override fun mouseEntered(e: MouseEvent) {
        updateForMouseOver(e)
    }

    override fun mouseExited(e: MouseEvent) {
        updateForMouseOver(e)
    }

    override fun mouseMoved(e: MouseEvent) {
        updateForMouseOver(e)
    }
}
