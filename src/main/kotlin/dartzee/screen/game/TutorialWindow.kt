package dartzee.screen.game

import dartzee.listener.DartboardListener
import dartzee.`object`.Dart
import dartzee.screen.GameplayDartboard
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

class TutorialWindow : JFrame(), ActionListener, DartboardListener {
    private val dartsThrown = mutableListOf<Dart>()

    private val dartboard = GameplayDartboard()

    private val btnConfirm = JButton("")
    private val btnReset = JButton("")
    private val btnResign = JButton("")

    init {
        contentPane.layout = BorderLayout(0, 0)
        size = Dimension(1000, 800)
        preferredSize = Dimension(1000, 800)

        val panelNorth = JPanel()
        contentPane.add(panelNorth, BorderLayout.NORTH)

        contentPane.add(dartboard, BorderLayout.CENTER)

        val panelSouth = JPanel()
        contentPane.add(panelSouth, BorderLayout.SOUTH)


        panelSouth.add(btnConfirm)
        panelSouth.add(btnReset)

        btnConfirm.preferredSize = Dimension(80, 80)
        btnConfirm.icon = ImageIcon(javaClass.getResource("/buttons/Confirm.png"))
        btnConfirm.toolTipText = "Confirm round"
        btnReset.preferredSize = Dimension(80, 80)
        btnReset.icon = ImageIcon(javaClass.getResource("/buttons/Reset.png"))
        btnReset.toolTipText = "Reset round"

        btnResign.preferredSize = Dimension(80, 80)
        btnResign.icon = ImageIcon(javaClass.getResource("/buttons/resign.png"))
        btnResign.toolTipText = "Resign"

        dartboard.addDartboardListener(this)
        btnConfirm.addActionListener(this)
        btnReset.addActionListener(this)

    }

    override fun actionPerformed(e: ActionEvent?) {
        when (e?.source) {
            btnReset -> clearDarts()
            btnConfirm -> clearDarts()
        }
    }

    private fun clearDarts() {
        dartboard.clearDarts()
        dartsThrown.clear()

        btnReset.isVisible = false
        btnConfirm.isVisible = false

        dartboard.ensureListening()
    }

    override fun dartThrown(dart: Dart) {
        dartsThrown.add(dart)
        btnReset.isVisible = true
        btnConfirm.isVisible = true

        if (dartsThrown.size == 3) {
            dartboard.stopListening()
        }
    }
}
