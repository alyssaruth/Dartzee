package dartzee.screen

import dartzee.core.util.doBadLuck
import dartzee.core.util.doChucklevision
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

class TestWindow : JFrame(), ActionListener
{
    private val dartboard = GameplayDartboard()
    private val btnClear = JButton("Clear darts")
    private val btnRepaint = JButton("Repaint dartboard")
    private val btnChucklevision = JButton("Chucklevision")
    private val btnBadLuck = JButton("Bad luck")

    init
    {
        contentPane.layout = BorderLayout(0, 0)
        size = Dimension(1000, 800)
        preferredSize = Dimension(1000, 800)

        contentPane.add(dartboard, BorderLayout.CENTER)

        val panelSouth = JPanel()
        contentPane.add(panelSouth, BorderLayout.SOUTH)

        panelSouth.add(btnClear)
        panelSouth.add(btnRepaint)
        panelSouth.add(btnChucklevision)
        panelSouth.add(btnBadLuck)

        btnClear.addActionListener(this)
        btnRepaint.addActionListener(this)
        btnChucklevision.addActionListener(this)
        btnBadLuck.addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        when (e?.source) {
            btnClear -> dartboard.clearDarts()
            btnRepaint -> dartboard.repaint()
            btnChucklevision -> dartboard.doChucklevision()
            btnBadLuck -> dartboard.doBadLuck()
        }
    }
}