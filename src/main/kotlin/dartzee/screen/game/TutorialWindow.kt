package dartzee.screen.game

import dartzee.core.util.append
import dartzee.core.util.setFontSize
import dartzee.listener.DartboardListener
import dartzee.`object`.Dart
import dartzee.screen.GameplayDartboard
import dartzee.utils.ResourceCache
import dartzee.utils.sumScore
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.JTextPane
import javax.swing.SwingConstants
import javax.swing.UIDefaults
import javax.swing.border.EmptyBorder
import javax.swing.border.EtchedBorder
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
        extendedState = Frame.MAXIMIZED_BOTH

        // West Pane - the rules
        val panelWest = JPanel()
        panelWest.border = EtchedBorder(EtchedBorder.RAISED, null, null)
        panelWest.layout = MigLayout("al center top")
        panelWest.preferredSize = Dimension(500, 50)
        contentPane.add(panelWest, BorderLayout.WEST)

        val lblRules = JLabel("The Rules")
        lblRules.border = EmptyBorder(10, 10, 10, 0)
        lblRules.horizontalAlignment = SwingConstants.CENTER
        lblRules.setFontSize(30)
        panelWest.add(lblRules, "cell 0 0, growx")

        panelWest.add(makeDivider(), "cell 0 1, alignx center")
        panelWest.add(makeRulesPane(), "cell 0 2")
        panelWest.add(makeDivider(), "cell 0 3, alignx center")

        val panelLeaving = JPanel()
        panelLeaving.layout = BorderLayout(0, 0)
        val lblLeaving = makeTextPane()
        lblLeaving.append("Need to leave? Use this button to remove the current player.")
        lblLeaving.setFontSize(24)
        panelLeaving.add(lblLeaving, BorderLayout.CENTER)
        panelLeaving.add(btnResign, BorderLayout.EAST)

        panelWest.add(panelLeaving, "cell 0 4")

        val lblQuitting = makeTextPane()
        lblQuitting.setFontSize(24)
        lblQuitting.append("To abandon the game completely, just close this window.")
        panelWest.add(lblQuitting, "cell 0 5")
        panelWest.add(makeDivider(), "cell 0 6, alignx center")

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

    private fun makeTextPane() =
        JTextPane().apply {
            val uiDefault = UIDefaults()
            uiDefault["EditorPane[Enabled].backgroundPainter"] = null
            putClientProperty("Nimbus.Overrides", uiDefault)
            putClientProperty("Nimbus.Overrides.InheritDefaults", false)
            background = null
            isEditable = false
        }

    private fun makeRulesPane() =
        makeTextPane().apply {
            border = EmptyBorder(10, 0, 20, 0)
            font = ResourceCache.UNICODE_FONT
            setFontSize(24)

            append("\uD83D\uDCC9 Score down from 301. First to hit 0 wins.")
            append("\n\n")
            append("\uD83C\uDFAF You must finish")
            append(" exactly", bold = true)
            append(" - score too much and you'll lose your score for the round!")
            append("\n\n")
            append(
                "\uD83D\uDDB1\uFE0F Input your score by clicking on the Dartboard. Use the ✅ to confirm."
            )
            append("\n\n")
            append("❎ Use the reset button if you mis-click.")
            append("\n\n")
            append("\uD83C\uDFB2 Give it a try using this screen!")
        }

    private fun makeDivider() =
        JSeparator(SwingConstants.HORIZONTAL).apply {
            border = EmptyBorder(10, 0, 10, 0)
            preferredSize = Dimension(200, 2)
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
