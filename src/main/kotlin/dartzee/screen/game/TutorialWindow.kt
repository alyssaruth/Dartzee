package dartzee.screen.game

import dartzee.core.util.setFontSize
import dartzee.listener.DartboardListener
import dartzee.`object`.Dart
import dartzee.screen.GameplayDartboard
import dartzee.utils.sumScore
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import net.miginfocom.swing.MigLayout

class TutorialWindow : JFrame(), ActionListener, DartboardListener {
    private val dartsThrown = mutableListOf<Dart>()
    private var scoreRemaining = 301

    private val dartboard = GameplayDartboard()

    private val btnConfirm = JButton("")
    private val btnReset = JButton("")
    private val btnResign = JButton("")

    private val lblRemaining = JLabel("301")
    private val lblScored = JLabel("0")

    init {
        contentPane.layout = BorderLayout(0, 0)
        size = Dimension(1000, 800)
        preferredSize = Dimension(1000, 800)

        // West Pane - the rules
        val panelWest = JPanel()
        panelWest.layout = MigLayout("al center top")
        panelWest.preferredSize = Dimension(300, 50)
        contentPane.add(panelWest, BorderLayout.WEST)

        val lblRules = JLabel("The Rules")
        lblRules.horizontalAlignment = SwingConstants.CENTER
        lblRules.setFontSize(30)
        panelWest.add(lblRules, "cell 0 0, growx")

        btnResign.preferredSize = Dimension(80, 80)
        btnResign.icon = ImageIcon(javaClass.getResource("/buttons/resign.png"))
        btnResign.toolTipText = "Resign"

        // Center Pane - Dartboard etc
        val panelCenter = JPanel()
        panelCenter.layout = BorderLayout(0, 0)
        contentPane.add(panelCenter, BorderLayout.CENTER)
        panelCenter.add(dartboard, BorderLayout.CENTER)

        // Center-South - Dartboard buttons
        val panelSouth = JPanel()
        panelCenter.add(panelSouth, BorderLayout.SOUTH)
        panelSouth.add(btnConfirm)
        panelSouth.add(btnReset)
        btnConfirm.preferredSize = Dimension(80, 80)
        btnConfirm.icon = ImageIcon(javaClass.getResource("/buttons/Confirm.png"))
        btnConfirm.toolTipText = "Confirm round"
        btnReset.preferredSize = Dimension(80, 80)
        btnReset.icon = ImageIcon(javaClass.getResource("/buttons/Reset.png"))
        btnReset.toolTipText = "Reset round"
        btnConfirm.isEnabled = false
        btnReset.isEnabled = false

        // Center-East - Score indicators
        val panelEast = JPanel()
        panelEast.preferredSize = Dimension(160, 50)
        contentPane.add(panelEast, BorderLayout.EAST)
        panelEast.layout = MigLayout("al center center")
        val lblRemainingText = JLabel("Remaining:")
        lblRemainingText.horizontalAlignment = SwingConstants.CENTER
        panelEast.add(lblRemainingText, "cell 0 0,growx")
        panelEast.add(lblRemaining, "cell 0 1,growx")
        val lblScoredText = JLabel("Scored:")
        lblScoredText.horizontalAlignment = SwingConstants.CENTER
        panelEast.add(lblScoredText, "cell 0 3,growx")
        panelEast.add(lblScored, "cell 0 4,growx")
        lblRemaining.horizontalAlignment = SwingConstants.CENTER
        lblScored.horizontalAlignment = SwingConstants.CENTER
        lblRemainingText.setFontSize(24)
        lblScoredText.setFontSize(24)
        lblRemaining.setFontSize(18)
        lblScored.setFontSize(18)

        dartboard.addDartboardListener(this)
        btnConfirm.addActionListener(this)
        btnReset.addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        when (e?.source) {
            btnReset -> clearDarts()
            btnConfirm -> confirmScore()
        }
    }

    private fun confirmScore() {
        if (!isBust()) {
            scoreRemaining -= sumScore(dartsThrown)
            lblRemaining.text = scoreRemaining.toString()
        }

        clearDarts()
    }

    private fun clearDarts() {
        dartboard.clearDarts()
        dartsThrown.clear()

        lblScored.text = "0"

        btnReset.isEnabled = false
        btnConfirm.isEnabled = false

        dartboard.ensureListening()
    }

    override fun dartThrown(dart: Dart) {
        dartsThrown.add(dart)

        lblScored.text = sumScore(dartsThrown).toString()

        btnReset.isEnabled = true
        btnConfirm.isEnabled = true

        if (dartsThrown.size == 3 || sumScore(dartsThrown) >= scoreRemaining) {
            dartboard.stopListening()
        }
    }

    private fun isBust(): Boolean = (scoreRemaining - sumScore(dartsThrown)) < 0
}
