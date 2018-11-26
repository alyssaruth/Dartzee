package code.screen.ai;

import bean.ScrollTable;
import bean.SuperTextPane;
import code.object.Dart;
import code.screen.ScreenCache;
import object.SuperHashMap;
import screen.SimpleDialog;
import util.DialogUtil;
import util.TableUtil;
import util.TableUtil.SimpleRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Dialog to specify setup darts that override defaults. Some examples:
 *  - On 48, the default is to aim for 8 (D20). But you might want to override this to aim for 16 (D16).
 *  - On 10, the default is to aim for D5. But if an AI is bad, you might want to override this to aim for 2.
 *  - On 35, the default is to aim for 3 (D16). But you might want to aim for 19 (D8).
 */
public class AISetupConfigurationDialog extends SimpleDialog
{
	private SuperHashMap<Integer, Dart> hmScoreToSingle = new SuperHashMap<>();
	
	public AISetupConfigurationDialog()
	{
		setTitle("Setup Configuration");
		setSize(500, 500);
		setResizable(false);
		setModal(true);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		info.setEditable(false);
		panel.add(info);
		
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		panel_1.add(tableScores);
		
		panel_1.add(panel_2, BorderLayout.NORTH);
		
		panel_2.add(btnAddRule);
		
		panel_2.add(btnRemove);
		
		btnAddRule.addActionListener(this);
		btnRemove.addActionListener(this);
	}
	
	private final SuperTextPane info = new SuperTextPane();
	private final ScrollTable tableScores = new ScrollTable();
	private final JPanel panel_1 = new JPanel();
	private final JPanel panel_2 = new JPanel();
	private final JButton btnAddRule = new JButton("Add Rule...");
	private final JButton btnRemove = new JButton("Remove");
	
	private void init(SuperHashMap<Integer, Dart> hmScoreToSingle)
	{
		this.hmScoreToSingle = hmScoreToSingle;
		
		initInfo();
		buildTable(hmScoreToSingle);
	}
	
	private void initInfo()
	{
		String txt = info.getText();
		if (!txt.isEmpty())
		{
			return;
		}
		
		info.append("By default, the AI strategy is as follows:");
		info.append("\n\n");
		
		info.append(" - ", true);
		info.append("s", true, true);
		info.append(" > 60: ", true);
		info.append("Throws a scoring dart");
		
		info.append("\n");
		
		info.append(" - 40 < ", true);
		info.append("s", true, true);
		info.append(" <= 60:", true);
		info.append(" Aim for the single (");
		info.append("s", false, true);
		info.append(" - 40)");
		
		info.append("\n");
		
		info.append(" - ", true);
		info.append("s", true, true);
		info.append(" <= 40, ", true);
		info.append("s", true, true);
		info.append(" even:", true);
		info.append(" Aim for the finish");
		
		info.append("\n");
		
		info.append(" - ", true);
		info.append("s", true, true);
		info.append(" <= 40, ", true);
		info.append("s", true, true);
		info.append(" odd:", true);
		info.append(" Aim for the single that leaves the highest power of 2 remaining");
		info.append("\n\n");
		
		info.append("Below you can further configure the dart aimed at for any individual score.");
	}
	
	private void buildTable(SuperHashMap<Integer, Dart> hmRules)
	{
		ArrayList<Integer> allValues = hmRules.getKeysAsVector();
		
		TableUtil.DefaultModel tm = new TableUtil.DefaultModel();
		tm.addColumn("Score");
		tm.addColumn("Dart to aim for");
		tm.addColumn("Result");
		
		for (Integer score : allValues)
		{
			Dart dartToAimAt = hmRules.get(score);
			
			int result = score - dartToAimAt.getTotal();
			
			Object[] row = {score, dartToAimAt, Integer.valueOf(result)};
			tm.addRow(row);
		}
		
		
		
		tableScores.setModel(tm);
		tableScores.setRenderer(1, new SimpleRenderer(SwingConstants.RIGHT, null));
		tableScores.sortBy(0, false);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (arg0.getSource() == btnAddRule)
		{
			SuperHashMap<Integer, Dart> hmCurrentRules = new SuperHashMap<>();
			fillHashMapFromTable(hmCurrentRules);
			NewSetupRuleDialog.addNewSetupRule(hmCurrentRules);
			
			buildTable(hmCurrentRules);
		}
		else if (arg0.getSource() == btnRemove)
		{
			removeScores();
		}
		else
		{
			super.actionPerformed(arg0);
		}
	}
	
	private void removeScores()
	{
		int[] rows = tableScores.getSelectedModelRows();
		if (rows.length == 0)
		{
			DialogUtil.showError("You must select row(s) to remove.");
			return;
		}
		
		SuperHashMap<Integer, Dart> hmCurrentRules = new SuperHashMap<>();
		fillHashMapFromTable(hmCurrentRules);
		
		for (int i=0; i<rows.length; i++)
		{
			int score = (int)tableScores.getValueAt(rows[i], 0);
			hmCurrentRules.remove(score);
		}
		
		buildTable(hmCurrentRules);
	}
	
	@Override
	public void okPressed()
	{
		hmScoreToSingle.clear();
		fillHashMapFromTable(hmScoreToSingle);
		
		dispose();
	}
	
	private void fillHashMapFromTable(SuperHashMap<Integer, Dart> hm)
	{
		DefaultTableModel tm = tableScores.getModel();
		int rows = tm.getRowCount();
		for (int i=0; i<rows; i++)
		{
			Integer score = (Integer)tm.getValueAt(i, 0);
			Dart drt = (Dart)tm.getValueAt(i, 1);
			
			hm.put(score, drt);
		}
	}
	
	public static void configureSetups(SuperHashMap<Integer, Dart> hmScoreToSingle)
	{
		AISetupConfigurationDialog dlg = new AISetupConfigurationDialog();
		dlg.setLocationRelativeTo(ScreenCache.getMainScreen());
		dlg.init(hmScoreToSingle);
		dlg.setVisible(true);
	}
}
