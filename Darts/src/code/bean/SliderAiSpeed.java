package code.bean;

import javax.swing.JSlider;

public class SliderAiSpeed extends JSlider
{
	private static final int AI_SPEED_MINIMUM = 0; //0s
	private static final int AI_SPEED_MAXIMUM = 2000; //2s
	
	public SliderAiSpeed()
	{
		setMinimum(AI_SPEED_MINIMUM);
		setMaximum(AI_SPEED_MAXIMUM);
		setInverted(true);
		setMajorTickSpacing(100);
	}
}
