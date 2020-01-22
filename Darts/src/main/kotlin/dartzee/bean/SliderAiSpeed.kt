package dartzee.bean

import java.awt.Dimension
import java.awt.Graphics
import java.awt.Rectangle

import javax.swing.JSlider
import javax.swing.plaf.synth.SynthContext
import javax.swing.plaf.synth.SynthSliderUI

class SliderAiSpeed(custom: Boolean) : JSlider()
{
    init
    {
        minimum = AI_SPEED_MINIMUM
        maximum = AI_SPEED_MAXIMUM
        inverted = true
        setMajorTickSpacing(100)
        isOpaque = false

        if (custom)
        {
            setUI(CustomUI(this))
        }
    }

    private class CustomUI(arg0: JSlider) : SynthSliderUI(arg0)
    {
        override fun getThumbSize() = Dimension(30, 30)

        override fun paintTrack(arg0: SynthContext, arg1: Graphics,
                                arg2: Rectangle)
        {
            arg2.setSize(30, 200)

            super.paintTrack(arg0, arg1, arg2)
        }
    }

    companion object
    {
        private const val AI_SPEED_MINIMUM = 0 //0s
        private const val AI_SPEED_MAXIMUM = 2000 //2s
    }
}
