package dartzee.screen.game

import dartzee.core.util.append
import dartzee.core.util.setFontSize
import dartzee.listener.DartboardListener
import dartzee.`object`.Dart
import dartzee.screen.GameplayDartboard
import dartzee.utils.DartsColour
import dartzee.utils.ResourceCache
import dartzee.utils.sumScore
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.JTextPane
import javax.swing.SwingConstants
import javax.swing.UIDefaults
import javax.swing.border.EmptyBorder
import javax.swing.border.EtchedBorder
import net.miginfocom.swing.MigLayout
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
import javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED

class TutorialPanel(private val parent: DartsGameScreen) :
    JPanel(), ActionListener, DartboardListener {
    private val dartsThrown = mutableListOf<Dart>()
    private var scoreRemaining = 301

    private val dartboard = GameplayDartboard()

    private val btnConfirm = JButton("")
    private val btnReset = JButton("")
    private val btnResign = JButton("")
    private val btnStartGame = JButton("I'm ready - let's play!")

    private val lblRemaining = JLabel("301")
    private val lblScored = JLabel("0")

    init {
        layout = BorderLayout(0, 0)

        // West Pane - the rules
        val panelWest = JPanel()
        val scrollPane = JScrollPane(panelWest, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER)
        scrollPane.setViewportView(panelWest)
        panelWest.border = EtchedBorder(EtchedBorder.RAISED, null, null)
        panelWest.layout = MigLayout("al center top")
        panelWest.preferredSize = Dimension(500, 1000)
        add(scrollPane, BorderLayout.WEST)

        val lblRules = makeTitleLabel("The Rules")
        panelWest.add(lblRules, "cell 0 0, growx")

        panelWest.add(makeDivider(), "cell 0 1, alignx center")
        panelWest.add(makeRulesPane(), "cell 0 2")
        panelWest.add(makeDivider(), "cell 0 3, alignx center")

        val lblLeaving = makeTextPane()
        lblLeaving.append("Someone need to leave? Use this button to remove the current player.")
        panelWest.add(lblLeaving, "cell 0 4, alignx center")
        btnResign.preferredSize = Dimension(80, 80)
        btnResign.icon = ImageIcon(javaClass.getResource("/buttons/resign.png"))
        btnResign.toolTipText = "Resign"
        panelWest.add(btnResign, "cell 0 5, alignx center")

        val lblQuitting = makeTextPane()
        lblQuitting.append("To abandon the game completely, just close this window.")
        panelWest.add(lblQuitting, "cell 0 6, alignx center")
        panelWest.add(makeDivider(), "cell 0 7, alignx center")

        btnStartGame.icon = ImageIcon(javaClass.getResource("/buttons/newGame.png"))
        btnStartGame.preferredSize = Dimension(300, 150)
        btnStartGame.setFontSize(30)
        panelWest.add(btnStartGame, "cell 0 8, alignx center")

        // Center Pane - Dartboard etc
        val panelCenter = JPanel()
        panelCenter.layout = BorderLayout(0, 0)
        add(panelCenter, BorderLayout.CENTER)
        panelCenter.add(dartboard, BorderLayout.CENTER)

        // Center-North - Give it a Try! title
        val panelNorth = JPanel()
        panelCenter.add(panelNorth, BorderLayout.NORTH)
        panelNorth.layout = MigLayout("al center top")

        val lblGiveItATry = makeTitleLabel("\uD83C\uDFB2 Give it a try!")
        panelNorth.add(lblGiveItATry, "cell 0 0, growx")
        panelNorth.add(makeDivider(), "cell 0 1, alignx center")

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
        panelEast.preferredSize = Dimension(200, 50)
        add(panelEast, BorderLayout.EAST)
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
        lblRemaining.name = "RemainingLabel"
        lblScored.name = "ScoredLabel"

        dartboard.addDartboardListener(this)
        btnStartGame.addActionListener(this)
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
            font = ResourceCache.UNICODE_FONT
            setFontSize(24)
            border = EmptyBorder(10, 5, 20, 5)
        }

    private fun makeTitleLabel(text: String) =
        JLabel(text).apply {
            font = ResourceCache.UNICODE_FONT
            border = EmptyBorder(10, 10, 10, 0)
            horizontalAlignment = SwingConstants.CENTER
            setFontSize(30)
        }

    private fun makeRulesPane() =
        makeTextPane().apply {
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
        }

    private fun makeDivider() =
        JSeparator(SwingConstants.HORIZONTAL).apply {
            border = EmptyBorder(10, 0, 10, 0)
            preferredSize = Dimension(200, 2)
        }

    override fun actionPerformed(e: ActionEvent?) {
        when (e?.source) {
            btnStartGame -> parent.tutorialFinished()
            btnReset -> clearDarts()
            btnConfirm -> confirmScore()
        }
    }

    private fun confirmScore() {
        if (!isBust()) {
            scoreRemaining -= sumScore(dartsThrown)
            lblRemaining.text = if (scoreRemaining > 0) "$scoreRemaining" else "You win!"
        }

        clearDarts()
    }

    private fun clearDarts() {
        dartboard.clearDarts()
        dartsThrown.clear()

        lblScored.text = "0"
        lblScored.foreground = Color.BLACK

        btnReset.isEnabled = false
        btnConfirm.isEnabled = false

        if (scoreRemaining > 0) {
            dartboard.ensureListening()
        }
    }

    override fun dartThrown(dart: Dart) {
        dartsThrown.add(dart)

        lblScored.text = getScoredDesc()

        btnReset.isEnabled = true
        btnConfirm.isEnabled = true

        if (dartsThrown.size == 3 || sumScore(dartsThrown) >= scoreRemaining) {
            dartboard.stopListening()
        }

        if (isBust()) {
            lblScored.foreground = DartsColour.DARK_RED
        } else if (sumScore(dartsThrown) == scoreRemaining) {
            lblScored.foreground = DartsColour.DARK_GREEN
        }
    }

    private fun getScoredDesc() =
        if (dartsThrown.size > 1) {
            "${dartsThrown.joinToString(" + ")} = ${sumScore(dartsThrown)}"
        } else {
            sumScore(dartsThrown).toString()
        }

    private fun isBust(): Boolean = (scoreRemaining - sumScore(dartsThrown)) < 0
}
