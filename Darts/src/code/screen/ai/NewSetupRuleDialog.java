package code.screen.ai;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;
import object.SuperHashMap;
import screen.SimpleDialog;
import util.DialogUtil;
import bean.NumberField;
import code.ai.AbstractDartsModel;
import code.bean.SpinnerSingleSelector;
import code.object.Dart;
import code.screen.ScreenCache;

public class NewSetupRuleDialog extends SimpleDialog
{
	private SuperHashMap<Integer, Dart> hmScoreToDart = null;
	
	public NewSetupRuleDialog(SuperHashMap<Integer, Dart> hmScoreToDart) 
	{
		setTitle("Add Rule");
		setSize(200, 200);
		setResizable(false);
		setModal(true);
		
		this.hmScoreToDart = hmScoreToDart;
		
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[][][]", "[][][][]"));
		panel.add(lblScore, "cell 0 0,alignx trailing");
		panel.add(nfScore, "cell 1 0,growx");
		panel.add(lblAimFor, "cell 0 1");
		rdbtnSingle.setPreferredSize(new Dimension(53, 25));
		panel.add(rdbtnSingle, "cell 1 1");
		panel.add(spinner, "cell 2 1");
		rdbtnDouble.setPreferredSize(new Dimension(59, 25));
		panel.add(rdbtnDouble, "cell 1 2");
		rdbtnTreble.setPreferredSize(new Dimension(55, 25));
		panel.add(rdbtnTreble, "cell 1 3");
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(rdbtnSingle);
		bg.add(rdbtnDouble);
		bg.add(rdbtnTreble);
		
		rdbtnSingle.setSelected(true);
		
		rdbtnSingle.addActionListener(this);
		rdbtnDouble.addActionListener(this);
		rdbtnTreble.addActionListener(this);
		
		pack();
	}
	
	private final JPanel panel = new JPanel();
	private final JLabel lblScore = new JLabel("Score");
	private final NumberField nfScore = new NumberField(4, 501);
	private final JLabel lblAimFor = new JLabel("Aim for");
	private final JRadioButton rdbtnSingle = new JRadioButton("Single");
	private final JRadioButton rdbtnDouble = new JRadioButton("Double");
	private final JRadioButton rdbtnTreble = new JRadioButton("Treble");
	private final SpinnerSingleSelector spinner = new SpinnerSingleSelector();

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (arg0.getSource() == rdbtnSingle
		  || arg0.getSource() == rdbtnDouble
		  || arg0.getSource() == rdbtnTreble)
		{
			panel.remove(spinner);
			
			int multiplier = getMultiplier();
			panel.add(spinner, "cell 2 " + multiplier);
			
			pack();
		}
		else
		{
			super.actionPerformed(arg0);
		}
	}
	
	@Override
	public void okPressed()
	{
		if (valid())
		{
			int score = Integer.parseInt(nfScore.getText());
			Dart drt = getDartFromSelections();
			
			hmScoreToDart.put(score, drt);
			
			dispose();
		}
	}
	
	private Dart getDartFromSelections()
	{
		int multiplier = getMultiplier();
		int value = (int)spinner.getValue();
		return new Dart(value, multiplier);
	}
	
	private int getMultiplier()
	{
		if (rdbtnSingle.isSelected())
		{
			return 1;
		}
		else if (rdbtnDouble.isSelected())
		{
			return 2;
		}
		
		return 3;
	}
	
	private boolean valid()
	{
		String scoreStr = nfScore.getText();
		if (scoreStr.isEmpty())
		{
			DialogUtil.showError("You must enter a score for this rule to apply to.");
			return false;
		}
		
		Dart drt = getDartFromSelections();
		if (drt.getScore() == 25
		  && drt.getMultiplier() == 3)
		{
			DialogUtil.showError("Treble 25 is not a valid dart!");
			return false;
		}
		
		//If we're specifying a rule for under 60, validate whether what we're setting up is 
		//already the default
		int score = Integer.parseInt(scoreStr);
		if (score <= 60)
		{
			Dart defaultDart = AbstractDartsModel.getDefaultDartToAimAt(score);
			if (defaultDart.equals(drt))
			{
				DialogUtil.showError("The selected dart is already the default for this starting score.");
				return false;
			}
		}
		
		return true;
	}
	
	public static void addNewSetupRule(SuperHashMap<Integer, Dart> hmScoreToDart) 
	{
		NewSetupRuleDialog dlg = new NewSetupRuleDialog(hmScoreToDart);
		dlg.setLocationRelativeTo(ScreenCache.getMainScreen());
		dlg.setVisible(true);
	}
}
