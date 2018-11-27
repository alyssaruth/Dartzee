package burlton.dartzee.code.bean;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JSlider;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthSliderUI;

public class SliderAiSpeed extends JSlider
{
	private static final int AI_SPEED_MINIMUM = 0; //0s
	private static final int AI_SPEED_MAXIMUM = 2000; //2s
	
	public SliderAiSpeed(boolean custom)
	{
		setMinimum(AI_SPEED_MINIMUM);
		setMaximum(AI_SPEED_MAXIMUM);
		setInverted(true);
		setMajorTickSpacing(100);
		setOpaque(false);
		
		if (custom)
		{
			setUI(new CustomUI(this));
		}
	}
	
	private static class CustomUI extends SynthSliderUI
	{
		protected CustomUI(JSlider arg0)
		{
			super(arg0);
		}
		
		@Override
		protected Dimension getThumbSize()
		{
			return new Dimension(30, 30);
		}
		
		@Override
		protected void paintTrack(SynthContext arg0, Graphics arg1,
				Rectangle arg2)
		{
			arg2.setSize(30, 200);
			
			super.paintTrack(arg0, arg1, arg2);
		}
	}
}
