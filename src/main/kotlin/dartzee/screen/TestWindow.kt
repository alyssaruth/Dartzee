package dartzee.screen

import dartzee.core.bean.NumberField
import dartzee.theme.Snowfall
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

class TestWindow : JFrame(), ActionListener {
    val snowfall = Snowfall()

    private val btnClear = JButton("Clear")
    private val flakeChance = NumberField(0, 100)
    private val sleepTime = NumberField()

    init {
        contentPane.layout = BorderLayout(0, 0)
        size = Dimension(1000, 800)
        preferredSize = Dimension(1000, 800)

        contentPane.add(snowfall, BorderLayout.CENTER)

        val panelSouth = JPanel()
        contentPane.add(panelSouth, BorderLayout.SOUTH)

        panelSouth.add(flakeChance)
        panelSouth.add(sleepTime)
        panelSouth.add(btnClear)

        //        snowfall.flakeChance = flakeChance.getNumber()
        //        flakeChance.addPropertyChangeListener { snowfall.flakeChance =
        // flakeChance.getNumber() }
        //        snowfall.rate = (sleepTime.value as Number).toLong()
        //        sleepTime.addPropertyChangeListener { snowfall.rate = (sleepTime.value as
        // Number).toLong() }
        btnClear.addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        when (e?.source) {
            btnClear -> clear()
        }
    }

    private fun clear() {
        snowfall.reset()
    }
}
