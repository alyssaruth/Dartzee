package dartzee.screen

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

    init
    {
        contentPane.layout = BorderLayout(0, 0)
        size = Dimension(400, 400)
        preferredSize = Dimension(400, 400)

        contentPane.add(dartboard, BorderLayout.CENTER)

        val panelSouth = JPanel()
        contentPane.add(panelSouth, BorderLayout.SOUTH)

        panelSouth.add(btnClear)
        btnClear.addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        dartboard.clearDarts()
    }
}