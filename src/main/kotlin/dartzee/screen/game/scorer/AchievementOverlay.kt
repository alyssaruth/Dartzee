package dartzee.screen.game.scorer

import dartzee.achievements.AbstractAchievement
import dartzee.bean.AchievementMedal
import dartzee.core.bean.SwingLabel
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.BevelBorder
import javax.swing.border.LineBorder

class AchievementOverlay(private val parent: AbstractDartsScorer<*>, achievement: AbstractAchievement, playerName: String?) : JPanel(), ActionListener, MouseListener
{
    private val btnClose = JButton("X")
    private val fillColor = achievement.getColor(false).brighter()
    private val borderColor = fillColor.darker()

    init
    {
        layout = BorderLayout(0, 0)
        border = LineBorder(borderColor, 6)
        size = Dimension(SCORER_WIDTH, 500)

        background = fillColor
        val panelNorth = JPanel()
        panelNorth.background = fillColor

        add(panelNorth, BorderLayout.NORTH)
        val fl = FlowLayout()
        fl.alignment = FlowLayout.TRAILING
        panelNorth.layout = fl

        panelNorth.add(btnClose)

        btnClose.font = Font("Trebuchet MS", Font.BOLD, 16)
        btnClose.preferredSize = Dimension(40, 40)
        btnClose.background = fillColor
        btnClose.foreground = borderColor.darker()
        btnClose.isContentAreaFilled = false
        btnClose.border = BevelBorder(BevelBorder.RAISED, borderColor, borderColor.darker())

        val panelCenter = JPanel()
        add(panelCenter, BorderLayout.CENTER)
        panelCenter.layout = MigLayout("", "[grow]", "[][][][]")
        panelCenter.background = fillColor

        val medal = AchievementMedal(achievement, false)
        medal.preferredSize = Dimension(175, 200)
        panelCenter.add(medal, "cell 0 2, alignx center")

        val lblAchievement = factoryTextLabel("Achievement")
        panelCenter.add(lblAchievement, "cell 0 0")

        val lblUnlocked = factoryTextLabel("Unlocked!")
        lblUnlocked.preferredSize = Dimension(200, 50)
        lblUnlocked.verticalAlignment = JLabel.TOP
        panelCenter.add(lblUnlocked, "cell 0 1")

        if (playerName != null)
        {
            val lblPlayerName = factoryTextLabel(playerName, 20, "playerName")
            panelCenter.add(lblPlayerName, "cell 0 3")
        }

        val lbName = factoryTextLabel(achievement.name, 16, "achievementName")
        panelCenter.add(lbName, "cell 0 4")

        btnClose.addMouseListener(this)
        btnClose.addActionListener(this)
    }

    private fun factoryTextLabel(text: String, fontSize: Int = 24, testId: String = "") : JLabel
    {
        val lbl = SwingLabel(text, testId)
        lbl.background = fillColor
        lbl.foreground = borderColor.darker()
        lbl.horizontalAlignment = JLabel.CENTER
        lbl.font = Font("Trebuchet MS", Font.BOLD, fontSize)
        lbl.preferredSize = Dimension(200, 30)
        return lbl
    }

    override fun actionPerformed(e: ActionEvent)
    {
        parent.achievementClosed(this)
        revalidate()
        repaint()
    }

    override fun mousePressed(e: MouseEvent?)
    {
        btnClose.foreground = borderColor.darker().darker()
        btnClose.background = fillColor.darker()
        btnClose.border = BevelBorder(BevelBorder.LOWERED, borderColor.darker(), borderColor.darker().darker())
        btnClose.repaint()
    }

    override fun mouseReleased(e: MouseEvent?)
    {
        btnClose.background = fillColor
        btnClose.foreground = borderColor.darker()
        btnClose.border = BevelBorder(BevelBorder.RAISED, borderColor, borderColor.darker())
        btnClose.repaint()
    }

    override fun mouseClicked(e: MouseEvent?) {}

    override fun mouseEntered(e: MouseEvent?) {}
    override fun mouseExited(e: MouseEvent?) {}
}