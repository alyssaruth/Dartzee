package burlton.dartzee.code.screen.preference;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import burlton.dartzee.code.utils.DartsColour;
import burlton.dartzee.code.utils.PreferenceUtil;
import burlton.desktopcore.code.util.ComponentUtil;
import burlton.desktopcore.code.util.DialogUtil;

public class PreferencesPanelScorer extends AbstractPreferencesPanel
									implements ChangeListener
{
	public PreferencesPanelScorer()
	{
		setLayout(null);
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Colour Scheme", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setBounds(10, 11, 449, 116);
		add(panel_1);
		panel_1.setLayout(null);
		JLabel label = new JLabel("0");
		label.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setPreferredSize(new Dimension(30, 30));
		panelScorerPreview.setBounds(20, 20, 408, 40);
		panel_1.add(panelScorerPreview);
		panelScorerPreview.add(label);
		JLabel label_1 = new JLabel("10");
		label_1.setPreferredSize(new Dimension(30, 30));
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
		panelScorerPreview.add(label_1);
		JLabel label_2 = new JLabel("20");
		label_2.setPreferredSize(new Dimension(30, 30));
		label_2.setHorizontalAlignment(SwingConstants.CENTER);
		label_2.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
		panelScorerPreview.add(label_2);
		JLabel label_3 = new JLabel("30");
		label_3.setPreferredSize(new Dimension(30, 30));
		label_3.setHorizontalAlignment(SwingConstants.CENTER);
		label_3.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
		panelScorerPreview.add(label_3);
		JLabel label_4 = new JLabel("40");
		label_4.setPreferredSize(new Dimension(30, 30));
		label_4.setHorizontalAlignment(SwingConstants.CENTER);
		label_4.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
		panelScorerPreview.add(label_4);
		JLabel label_5 = new JLabel("60");
		label_5.setPreferredSize(new Dimension(30, 30));
		label_5.setHorizontalAlignment(SwingConstants.CENTER);
		label_5.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
		panelScorerPreview.add(label_5);
		JLabel label_6 = new JLabel("80");
		label_6.setPreferredSize(new Dimension(30, 30));
		label_6.setHorizontalAlignment(SwingConstants.CENTER);
		label_6.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
		panelScorerPreview.add(label_6);
		JLabel label_7 = new JLabel("100");
		label_7.setPreferredSize(new Dimension(30, 30));
		label_7.setHorizontalAlignment(SwingConstants.CENTER);
		label_7.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
		panelScorerPreview.add(label_7);
		JLabel label_8 = new JLabel("120");
		label_8.setPreferredSize(new Dimension(30, 30));
		label_8.setHorizontalAlignment(SwingConstants.CENTER);
		label_8.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
		panelScorerPreview.add(label_8);
		JLabel label_9 = new JLabel("140");
		label_9.setPreferredSize(new Dimension(30, 30));
		label_9.setHorizontalAlignment(SwingConstants.CENTER);
		label_9.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
		panelScorerPreview.add(label_9);
		JLabel label_10 = new JLabel("180");
		label_10.setPreferredSize(new Dimension(30, 30));
		label_10.setHorizontalAlignment(SwingConstants.CENTER);
		label_10.setFont(new Font("Trebuchet MS", Font.BOLD, 15));
		panelScorerPreview.add(label_10);
		JLabel lblHueFactor = new JLabel("Hue Factor");
		lblHueFactor.setBounds(48, 70, 65, 29);
		panel_1.add(lblHueFactor);
		spinnerHueFactor.setBounds(113, 72, 60, 25);
		panel_1.add(spinnerHueFactor);
		spinnerHueFactor.setPreferredSize(new Dimension(60, 25));
		spinnerHueFactor.setModel(new SpinnerNumberModel(0.8, -1.0, 1.0, 0.05));
		JLabel lblBrightnessFg = new JLabel("FG");
		lblBrightnessFg.setBounds(193, 70, 25, 29);
		panel_1.add(lblBrightnessFg);
		spinnerFgBrightness.setBounds(218, 72, 60, 25);
		panel_1.add(spinnerFgBrightness);
		spinnerFgBrightness.setPreferredSize(new Dimension(60, 25));
		spinnerFgBrightness.setModel(new SpinnerNumberModel(0.3, 0.1, 1.0, 0.05));
		JLabel lblBgBrightness = new JLabel("BG");
		lblBgBrightness.setBounds(298, 70, 25, 29);
		panel_1.add(lblBgBrightness);
		spinnerBgBrightness.setBounds(323, 72, 60, 25);
		panel_1.add(spinnerBgBrightness);
		spinnerBgBrightness.setPreferredSize(new Dimension(60, 25));
		spinnerBgBrightness.setModel(new SpinnerNumberModel(1.0, 0.1, 1.0, 0.05));
		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_2.setBorder(new TitledBorder(null, "Other Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setBounds(10, 149, 449, 68);
		add(panel_2);
		JLabel lblDartNotation = new JLabel("Dart Notation");
		panel_2.add(lblDartNotation);
		Component horizontalStrut = Box.createHorizontalStrut(20);
		panel_2.add(horizontalStrut);
		panel_2.add(rdbtnD20);
		panel_2.add(rdbtn40);
		ButtonGroup bg = new ButtonGroup();
		bg.add(rdbtnD20);
		bg.add(rdbtn40);
		
		spinnerHueFactor.addChangeListener(this);
		spinnerBgBrightness.addChangeListener(this);
		spinnerFgBrightness.addChangeListener(this);
	}
	
	private final JPanel panelScorerPreview = new JPanel();
	private final JSpinner spinnerHueFactor = new JSpinner();
	private final JSpinner spinnerFgBrightness = new JSpinner();
	private final JSpinner spinnerBgBrightness = new JSpinner();
	private final JRadioButton rdbtnD20 = new JRadioButton("D20");
	private final JRadioButton rdbtn40 = new JRadioButton("40");
	
	@Override
	public void refresh(boolean useDefaults)
	{
		double hueFactor = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_HUE_FACTOR, useDefaults);
		double bgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_BG_BRIGHTNESS, useDefaults);
		double fgBrightness = PreferenceUtil.getDoubleValue(PREFERENCES_DOUBLE_FG_BRIGHTNESS, useDefaults);
		
		spinnerHueFactor.setValue(hueFactor);
		spinnerBgBrightness.setValue(bgBrightness);
		spinnerFgBrightness.setValue(fgBrightness);
		
		boolean totalScore = PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_DISPLAY_DART_TOTAL_SCORE, useDefaults);
		rdbtn40.setSelected(totalScore);
		rdbtnD20.setSelected(!totalScore);
		
		repaintScorerPreview();
	}
	private void repaintScorerPreview()
	{
		double hueFactor = (double)spinnerHueFactor.getValue();
		double fgBrightness = (double)spinnerFgBrightness.getValue();
		double bgBrightness = (double)spinnerBgBrightness.getValue();
		
		ArrayList<JLabel> scoreLabels = ComponentUtil.getAllChildComponentsForType(panelScorerPreview, JLabel.class);
		for (int i=0; i<scoreLabels.size(); i++)
		{
			JLabel scoreLabel = scoreLabels.get(i);
			int score = Integer.parseInt(scoreLabel.getText());
			
			Color fg = DartsColour.getScorerColour(score, hueFactor, fgBrightness);
			Color bg = DartsColour.getScorerColour(score, hueFactor, bgBrightness);
			
			scoreLabel.setOpaque(true);
			scoreLabel.setForeground(fg);
			scoreLabel.setBackground(bg);
			scoreLabel.repaint();
		}
	}

	@Override
	public boolean valid()
	{
		double fgBrightness = (double)spinnerFgBrightness.getValue();
		double bgBrightness = (double)spinnerBgBrightness.getValue();
		
		if (fgBrightness == bgBrightness)
		{
			DialogUtil.showError("BG and FG brightness cannot have the same value.");
			return false;
		}
		
		return true;
	}

	@Override
	public void save()
	{
		double hueFactor = (double)spinnerHueFactor.getValue();
		double fgBrightness = (double)spinnerFgBrightness.getValue();
		double bgBrightness = (double)spinnerBgBrightness.getValue();
		
		boolean showTotal = rdbtn40.isSelected();
		
		PreferenceUtil.saveDouble(PREFERENCES_DOUBLE_BG_BRIGHTNESS, bgBrightness);
		PreferenceUtil.saveDouble(PREFERENCES_DOUBLE_FG_BRIGHTNESS, fgBrightness);
		PreferenceUtil.saveDouble(PREFERENCES_DOUBLE_HUE_FACTOR, hueFactor);
		
		PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_DISPLAY_DART_TOTAL_SCORE, showTotal);
	}
	
	@Override
	public void stateChanged(ChangeEvent arg0)
	{
		repaintScorerPreview();
	}
}
