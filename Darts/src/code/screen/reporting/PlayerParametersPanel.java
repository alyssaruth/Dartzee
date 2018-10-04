package code.screen.reporting;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import bean.ComboBoxNumberComparison;
import code.db.PlayerEntity;
import code.reporting.IncludedPlayerParameters;
import net.miginfocom.swing.MigLayout;
import util.Debug;
import util.DialogUtil;

public class PlayerParametersPanel extends JPanel
								   implements ActionListener
{
	public PlayerParametersPanel()
	{
		super();
		
		setLayout(new MigLayout("", "[][][]", "[][]"));
		
		comboBox.addOption(IncludedPlayerParameters.COMPARATOR_SCORE_UNSET);
		
		add(chckbxFinalScore, "cell 0 0");
		add(comboBox, "width 80:80:80, cell 1 0,growx");
		add(chckbxPosition, "cell 0 1");
		add(cbFirst, "flowx,cell 1 1");
		add(cbSecond, "cell 1 1");
		add(cbThird, "cell 1 1");
		add(cbFourth, "cell 1 1");
		add(cbUndecided, "cell 1 1");
		add(spinner, "cell 1 0");
		
		updatePlayerOptionsEnabled();
		
		chckbxFinalScore.addActionListener(this);
		chckbxPosition.addActionListener(this);
		comboBox.addActionListener(this);
	}
	
	private final JCheckBox chckbxFinalScore = new JCheckBox("Game Score");
	private final ComboBoxNumberComparison comboBox = new ComboBoxNumberComparison();
	private final JSpinner spinner = new JSpinner(new SpinnerNumberModel(3, 3, 200, 1));
	private final JCheckBox chckbxPosition = new JCheckBox("Position");
	private final JCheckBox cbFirst = new JCheckBox("1st");
	private final JCheckBox cbSecond = new JCheckBox("2nd");
	private final JCheckBox cbThird = new JCheckBox("3rd");
	private final JCheckBox cbFourth = new JCheckBox("4th");
	private final JCheckBox cbUndecided = new JCheckBox("Undecided");
	
	public boolean valid(PlayerEntity player)
	{
		if (chckbxPosition.isSelected()
		  && getFinishingPositions().isEmpty())
		{
			DialogUtil.showError("You must select at least one finishing position for Player " + player.getName());
			return false;
		}
		
		return true;
	}
	
	public IncludedPlayerParameters generateParameters()
	{
		IncludedPlayerParameters parms = new IncludedPlayerParameters();
		
		if (chckbxFinalScore.isSelected())
		{
			int finalScore = (int)spinner.getValue();
			String comparator = (String)comboBox.getSelectedItem();
			
			parms.setFinalScore(finalScore);
			parms.setFinalScoreComparator(comparator);
		}
		
		if (chckbxPosition.isSelected())
		{
			ArrayList<Integer> finishingPositions = getFinishingPositions();
			parms.setFinishingPositions(finishingPositions);
		}
		
		return parms;
	}
	
	private ArrayList<Integer> getFinishingPositions()
	{
		ArrayList<Integer> ret = new ArrayList<>();
		
		addValueIfSelected(ret, cbFirst, 1);
		addValueIfSelected(ret, cbSecond, 2);
		addValueIfSelected(ret, cbThird, 3);
		addValueIfSelected(ret, cbFourth, 4);
		addValueIfSelected(ret, cbUndecided, -1);
		
		return ret;
	}
	private void addValueIfSelected(ArrayList<Integer> v, JCheckBox cb, int value)
	{
		if (cb.isSelected())
		{
			v.add(value);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		Object src = arg0.getSource();
		if (src == chckbxFinalScore
		  || src == chckbxPosition
		  || src == comboBox)
		{
			updatePlayerOptionsEnabled();
		}
		else
		{
			Debug.stackTrace("Unexpected actionPerformed: [" + src + "]");
		}
	}
	
	public void disableAll()
	{
		chckbxFinalScore.setSelected(false);
		chckbxFinalScore.setEnabled(false);
		chckbxPosition.setSelected(false);
		chckbxPosition.setEnabled(false);
		
		updatePlayerOptionsEnabled();
	}
	
	private void updatePlayerOptionsEnabled()
	{
		cbFirst.setEnabled(chckbxPosition.isSelected());
		cbSecond.setEnabled(chckbxPosition.isSelected());
		cbThird.setEnabled(chckbxPosition.isSelected());
		cbFourth.setEnabled(chckbxPosition.isSelected());
		cbUndecided.setEnabled(chckbxPosition.isSelected() && !chckbxFinalScore.isSelected());
		
		comboBox.setEnabled(chckbxFinalScore.isSelected());
		
		String comboSelection = (String)comboBox.getSelectedItem();
		boolean unset = comboSelection.equals(IncludedPlayerParameters.COMPARATOR_SCORE_UNSET);
		spinner.setEnabled(chckbxFinalScore.isSelected() && !unset);
		
		if (chckbxFinalScore.isSelected())
		{
			cbUndecided.setSelected(false);
		}
	}
}
