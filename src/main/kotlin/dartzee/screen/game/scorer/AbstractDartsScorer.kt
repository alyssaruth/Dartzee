package dartzee.screen.game.scorer

import dartzee.`object`.Dart
import dartzee.achievements.AbstractAchievement
import dartzee.bean.AchievementMedal
import dartzee.game.state.AbstractPlayerState
import dartzee.game.state.PlayerStateListener
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
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder

const val SCORER_WIDTH = 210

abstract class AbstractDartsScorer<PlayerState: AbstractPlayerState<PlayerState>>: AbstractScorer(), PlayerStateListener<PlayerState> {

    private val overlays = mutableListOf<AchievementOverlay>()

    init
    {
        preferredSize = Dimension(SCORER_WIDTH, 600)
        panelAvatar.border = EmptyBorder(5, 30, 5, 30)
    }

    override fun stateChanged(state: PlayerState)
    {
        model.clear()

        stateChangedImpl(state)

        tableScores.scrollToBottom()
        tableScores.repaint()
        lblResult.repaint()
        repaint()
    }

    protected fun setScoreAndFinishingPosition(state: PlayerState)
    {
        val scoreSoFar = state.getScoreSoFar()
        lblResult.text = if (scoreSoFar > 0) "$scoreSoFar" else ""
        updateResultColourForPosition(state.pt.finishingPosition)
    }

    protected open fun stateChangedImpl(state: PlayerState) {}

    protected fun addDartRound(darts: List<Dart>)
    {
        addRow(makeEmptyRow())

        darts.forEach(::addDart)
    }

    /**
     * Add a dart to the scorer.
     */
    protected fun addDart(drt: Dart)
    {
        addDartToRow(model.rowCount - 1, drt)
    }

    /**
     * Default method, overridden by Round the Clock
     */
    open fun getNumberOfColumnsForAddingNewDart() = getNumberOfColumns() - 1

    private fun addDartToRow(rowNumber: Int, drt: Dart)
    {
        for (i in 0 until getNumberOfColumnsForAddingNewDart())
        {
            val currentVal = model.getValueAt(rowNumber, i)
            if (currentVal == null)
            {
                model.setValueAt(drt, rowNumber, i)
                return
            }
        }

        throw Exception("Trying to add dart to row $rowNumber but it's already full.")
    }

    fun achievementUnlocked(achievement: AbstractAchievement)
    {
        val overlay = AchievementOverlay(achievement)

        overlays.add(overlay)

        //Let's just only ever have one thing at a time on display. Actually layering them sometimes worked but
        //sometimes caused weird bollocks when things happened close together
        layeredPane.removeAll()
        layeredPane.add(overlay, BorderLayout.CENTER)
        layeredPane.revalidate()
        layeredPane.repaint()
    }

    private inner class AchievementOverlay(achievement: AbstractAchievement) : JPanel(), ActionListener, MouseListener
    {
        private val btnClose = JButton("X")
        private val fillColor = achievement.getColor(false).brighter()
        private val borderColor = fillColor.darker()

        init
        {
            layout = BorderLayout(0, 0)
            border = LineBorder(borderColor, 6)

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

            val lbName = factoryTextLabel(achievement.name, 20)
            panelCenter.add(lbName, "cell 0 3")

            btnClose.addMouseListener(this)
            btnClose.addActionListener(this)
        }

        private fun factoryTextLabel(text: String, fontSize: Int = 24) : JLabel
        {
            val lbl = JLabel(text)
            lbl.background = fillColor
            lbl.foreground = borderColor.darker()
            lbl.horizontalAlignment = JLabel.CENTER
            lbl.font = Font("Trebuchet MS", Font.BOLD, fontSize)
            lbl.preferredSize = Dimension(200, 30)

            return lbl
        }

        override fun actionPerformed(e: ActionEvent)
        {
            layeredPane.removeAll()
            overlays.remove(this)

            //If there are more overlays stacked 'beneath', show the next one of them now
            if (!overlays.isEmpty())
            {
                layeredPane.add(overlays.last(), BorderLayout.CENTER)
            }
            else
            {
                layeredPane.add(tableScores)
            }

            layeredPane.revalidate()
            layeredPane.repaint()
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

}
