package dartzee.bean

import dartzee.`object`.DartsClient
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Rectangle
import javax.swing.JSlider
import javax.swing.plaf.synth.SynthContext
import javax.swing.plaf.synth.SynthSliderUI

const val AI_SPEED_MINIMUM = 0 //0s
const val AI_SPEED_MAXIMUM = 2000 //2s

class SliderAiSpeed(thicken: Boolean) : JSlider()
{
    init
    {
        minimum = AI_SPEED_MINIMUM
        maximum = AI_SPEED_MAXIMUM
        inverted = true
        setMajorTickSpacing(100)
        isOpaque = false

        if (thicken && !DartsClient.isAppleOs())
        {
            size = Dimension(200, 50)
            preferredSize = Dimension(200, 50)
            setUI(CustomSliderUI(this))
        }
    }
}

class CustomSliderUI(arg0: JSlider) : SynthSliderUI(arg0)
{
    override fun getThumbSize() = Dimension(30, 30)

    override fun paintTrack(arg0: SynthContext, arg1: Graphics,
                            arg2: Rectangle)
    {
        arg2.setSize(200, 30)
        super.paintTrack(arg0, arg1, arg2)
    }
}