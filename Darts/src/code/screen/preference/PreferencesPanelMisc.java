package code.screen.preference;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import bean.NumberField;
import code.bean.SliderAiSpeed;
import code.utils.PreferenceUtil;
import net.miginfocom.swing.MigLayout;

public class PreferencesPanelMisc extends AbstractPreferencesPanel
{
	public PreferencesPanelMisc()
	{
		nfLeaderboardSize.setColumns(10);
		setLayout(new MigLayout("", "[][grow][]", "[][][][][]"));
		
		add(lblDefaultAiSpeed, "cell 0 0");
		add(slider, "cell 1 0");
		
		add(lblRowsToShow, "cell 0 1,alignx leading");
		
		add(nfLeaderboardSize, "cell 1 1,alignx leading");
		add(chckbxAiAutomaticallyFinish, "flowx,cell 0 2");
		
		add(chckbxCheckForUpdates, "flowx,cell 0 3");
		
		add(chckbxShowAnimations, "cell 0 4");
	}
	
	private final JLabel lblDefaultAiSpeed = new JLabel("Default AI speed");
	private final SliderAiSpeed slider = new SliderAiSpeed();
	private final JCheckBox chckbxAiAutomaticallyFinish = new JCheckBox("AI automatically finish");
	private final JCheckBox chckbxCheckForUpdates = new JCheckBox("Automatically check for updates");
	private final JLabel lblRowsToShow = new JLabel("Rows to show on Leaderboards");
	private final NumberField nfLeaderboardSize = new NumberField(10, 200);
	private final JCheckBox chckbxShowAnimations = new JCheckBox("Show animations");
	
	@Override
	public void refresh(boolean useDefaults)
	{
		int aiSpd = PreferenceUtil.getIntValue(PREFERENCES_INT_AI_SPEED, useDefaults);
		slider.setValue(aiSpd);
		
		int leaderboardSize = PreferenceUtil.getIntValue(PREFERENCES_INT_LEADERBOARD_SIZE, useDefaults);
		nfLeaderboardSize.setValue(leaderboardSize);
		
		boolean aiAuto = PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE, useDefaults);
		chckbxAiAutomaticallyFinish.setSelected(aiAuto);
		
		boolean checkForUpdates = PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES, useDefaults);
		chckbxCheckForUpdates.setSelected(checkForUpdates);
		
		boolean showAnimations = PreferenceUtil.getBooleanValue(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS, useDefaults);
		chckbxShowAnimations.setSelected(showAnimations);
	}

	@Override
	public boolean valid()
	{
		return true;
	}

	@Override
	public void save()
	{
		int aiSpd = slider.getValue();
		PreferenceUtil.saveInt(PREFERENCES_INT_AI_SPEED, aiSpd);
		
		int leaderboardSize = nfLeaderboardSize.getNumber();
		PreferenceUtil.saveInt(PREFERENCES_INT_LEADERBOARD_SIZE, leaderboardSize);
		
		boolean aiAuto = chckbxAiAutomaticallyFinish.isSelected();
		PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_AI_AUTO_CONTINUE, aiAuto);
		
		boolean checkForUpdates = chckbxCheckForUpdates.isSelected();
		PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES, checkForUpdates);
		
		boolean showAnimations = chckbxShowAnimations.isSelected();
		PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_SHOW_ANIMATIONS, showAnimations);
	}

}
