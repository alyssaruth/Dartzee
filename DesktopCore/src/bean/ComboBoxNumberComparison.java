package bean;

import java.awt.Dimension;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class ComboBoxNumberComparison extends JComboBox<String>
{
	public static final String FILTER_MODE_EQUAL_TO = "=";
	public static final String FILTER_MODE_GREATER_THAN = ">";
	public static final String FILTER_MODE_LESS_THAN = "<";
	
	private final String[] filterModes = {FILTER_MODE_EQUAL_TO, FILTER_MODE_GREATER_THAN, FILTER_MODE_LESS_THAN};
	
	public ComboBoxNumberComparison()
	{
		super();
		setModel(comboModel);
		setPreferredSize(new Dimension(40, 30));
		setMaximumSize(new Dimension(40, 30));
	}
	
	private final DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<>(filterModes);
	
	public void addOption(String option)
	{
		comboModel.addElement(option);
	}
}
