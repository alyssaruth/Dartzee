package code.screen.ai;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import code.ai.AbstractDartsModel;
import code.bean.SpinnerSingleSelector;
import code.object.Dart;
import object.SuperHashMap;

public class AIConfigurationX01 extends AbstractAIConfigurationSubPanel
								implements ActionListener
{
	private SuperHashMap<Integer, Dart> hmScoreToDart = new SuperHashMap<>();
	
	public AIConfigurationX01() 
	{
		setBorder(null);
		setLayout(null);
		lblScoringDart.setBounds(20, 20, 120, 25);
		add(lblScoringDart);
		spinnerScoringDart.setBounds(140, 20, 50, 25);
		add(spinnerScoringDart);
		btnConfigureSetupDarts.setBounds(20, 92, 150, 25);
		add(btnConfigureSetupDarts);
		chckbxMercyRule.setBounds(20, 55, 120, 25);
		add(chckbxMercyRule);
		spinnerMercyThreshold.setBounds(272, 55, 50, 25);
		spinnerMercyThreshold.setModel(new SpinnerNumberModel(10, 4, 40, 2));
		add(spinnerMercyThreshold);
		lblWhenScoreLess.setBounds(140, 55, 118, 24);
		add(lblWhenScoreLess);
		
		//Listeners
		btnConfigureSetupDarts.addActionListener(this);
		chckbxMercyRule.addActionListener(this);
	}
	
	private final JLabel lblScoringDart = new JLabel("Scoring Dart");
	private final SpinnerSingleSelector spinnerScoringDart = new SpinnerSingleSelector();
	private final JButton btnConfigureSetupDarts = new JButton("Configure Setup...");
	private final JCheckBox chckbxMercyRule = new JCheckBox("Mercy Rule");
	private final JLabel lblWhenScoreLess = new JLabel("when score less than");
	private final JSpinner spinnerMercyThreshold = new JSpinner();
	
	@Override
	public boolean valid()
	{
		return true;
	}
	
	@Override
	public void populateModel(AbstractDartsModel model)
	{
		model.setHmScoreToDart(hmScoreToDart);
		
		int scoringDart = (int)spinnerScoringDart.getValue();
		model.setScoringDart(scoringDart);
		
		boolean mercyRule = chckbxMercyRule.isSelected();
		if (mercyRule)
		{
			int mercyThreshold = (int)spinnerMercyThreshold.getValue();
			model.setMercyThreshold(mercyThreshold);
		}
	}
	
	@Override
	public void initialiseFromModel(AbstractDartsModel model)
	{
		int scoringDart = model.getScoringDart();
		spinnerScoringDart.setValue(scoringDart);
		
		int mercyThreshold = model.getMercyThreshold();
		boolean mercyRule = mercyThreshold > -1;
		chckbxMercyRule.setSelected(mercyRule);
		spinnerMercyThreshold.setEnabled(mercyRule);
		lblWhenScoreLess.setEnabled(mercyRule);
		
		if (mercyRule)
		{
			spinnerMercyThreshold.setValue(mercyThreshold);
		}
		else
		{
			spinnerMercyThreshold.setValue(10);
		}
		
		hmScoreToDart = model.getHmScoreToDart();
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (arg0.getSource() == btnConfigureSetupDarts)
		{
			AISetupConfigurationDialog.configureSetups(hmScoreToDart);
		}
		else if (arg0.getSource() == chckbxMercyRule)
		{
			boolean mercyRule = chckbxMercyRule.isSelected();
			spinnerMercyThreshold.setEnabled(mercyRule);
			lblWhenScoreLess.setEnabled(mercyRule);
		}
	}
}
