package dartzee.bean

import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.border.EmptyBorder
import javax.swing.event.ChangeListener

const val AI_SPEED_MINIMUM = 0 //0s
const val AI_SPEED_MAXIMUM = 2000 //2s

class SliderAiSpeed : JPanel()
{
    private val labelMin = JLabel("Slow")
    private val labelMax = JLabel("Fast")
    private val slider = JSlider()
    var value
        get() = slider.value
        set(value) { slider.value = value }

    init
    {
        layout = BorderLayout()
        add(slider, BorderLayout.CENTER)
        add(labelMin, BorderLayout.WEST)
        add(labelMax, BorderLayout.EAST)

        labelMin.verticalAlignment = JLabel.CENTER
        labelMin.border = EmptyBorder(0, 0, 0, 10)
        labelMax.verticalAlignment = JLabel.CENTER
        labelMax.border = EmptyBorder(0, 10, 0, 0)

        slider.minimum = AI_SPEED_MINIMUM
        slider.maximum = AI_SPEED_MAXIMUM
        slider.inverted = true
    }

    fun addChangeListener(listener: ChangeListener)
    {
        slider.addChangeListener(listener)
    }
}