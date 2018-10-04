package code.screen.ai;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import bean.NumberField;
import code.ai.AbstractDartsModel;
import code.ai.DartsModelNormalDistribution;
import net.miginfocom.swing.MigLayout;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class AIConfigurationNormalDistribution extends AbstractAIConfigurationPanel
											   implements ActionListener
{
	public AIConfigurationNormalDistribution() 
	{
		super();
		
		setLayout(new BorderLayout(0, 0));
		panelNorth.setBorder(new TitledBorder(null, "Variables", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panelNorth, BorderLayout.CENTER);
		panelNorth.setLayout(new MigLayout("", "[125px,grow][100px,grow]", "[25px][][][][grow]"));
		lblStandardDeviation.setBorder(new EmptyBorder(0, 5, 0, 0));
		lblStandardDeviation.setPreferredSize(new Dimension(125, 25));
		panelNorth.add(lblStandardDeviation, "cell 0 0,alignx left,aligny top");
		nfStandardDeviation.setPreferredSize(new Dimension(100, 25));
		nfStandardDeviation.setValue(100);
		panelNorth.add(nfStandardDeviation, "cell 1 0,alignx left,aligny top");
		
		panelNorth.add(chckbxStandardDeviationDoubles, "cell 0 1");
		nfStandardDeviationDoubles.setPreferredSize(new Dimension(100, 25));
		
		panelNorth.add(nfStandardDeviationDoubles, "cell 1 1,alignx left,aligny top");
		
		panelNorth.add(chckbxCenterBias, "cell 0 2");
		nfCentralBias.setPreferredSize(new Dimension(100, 25));
		panelNorth.add(nfCentralBias, "cell 1 2");
		
		panelNorth.add(chckbxRadiusAverage, "cell 0 3");
		
		panelNorth.add(spinnerAverageCount, "cell 1 3");
		spinnerAverageCount.setModel(new SpinnerNumberModel(2, 2, 5, 1));
		
		chckbxStandardDeviationDoubles.addActionListener(this);
		chckbxCenterBias.addActionListener(this);
		chckbxRadiusAverage.addActionListener(this);
	}
	
	private final JPanel panelNorth = new JPanel();
	private final JLabel lblStandardDeviation = new JLabel("Standard Deviation");
	private final NumberField nfStandardDeviation = new NumberField(1);
	private final JCheckBox chckbxStandardDeviationDoubles = new JCheckBox("Standard Deviation (Doubles)");
	private final NumberField nfStandardDeviationDoubles = new NumberField(1);
	private final NumberField nfCentralBias = new NumberField(1, 200);
	private final JCheckBox chckbxCenterBias = new JCheckBox("Standard Deviation (skew towards center)");
	private final JCheckBox chckbxRadiusAverage = new JCheckBox("Radius as average of multiple throws");
	private final JSpinner spinnerAverageCount = new JSpinner();

	@Override
	public boolean valid() 
	{
		return true;
	}
	
	@Override
	public AbstractDartsModel initialiseModel()
	{
		DartsModelNormalDistribution model = new DartsModelNormalDistribution();
		
		String standardDeviationStr = "" + nfStandardDeviation.getValue();
		double standardDeviation = Double.parseDouble(standardDeviationStr);
		
		double standardDeviationDoubles = 0;
		if (chckbxStandardDeviationDoubles.isSelected())
		{
			standardDeviationStr = "" + nfStandardDeviationDoubles.getValue();
			standardDeviationDoubles = Double.parseDouble(standardDeviationStr);
		}
		
		double standardDeviationCentral = 0;
		if (chckbxCenterBias.isSelected())
		{
			standardDeviationStr = "" + nfCentralBias.getValue();
			standardDeviationCentral = Double.parseDouble(standardDeviationStr);
		}
		
		int averageCount = 1;
		if (chckbxRadiusAverage.isSelected())
		{
			averageCount = (int)spinnerAverageCount.getValue();
		}
		
		model.populate(standardDeviation, standardDeviationDoubles, standardDeviationCentral, averageCount);
		return model;
	}

	@Override
	public void initialiseFromModel(AbstractDartsModel model)
	{
		DartsModelNormalDistribution normalModel = (DartsModelNormalDistribution)model;
		double standardDeviation = normalModel.getStandardDeviation();
		nfStandardDeviation.setValue(standardDeviation);
		
		double standardDeviationDoubles = normalModel.getStandardDeviationDoubles();
		if (standardDeviationDoubles > 0)
		{
			chckbxStandardDeviationDoubles.setSelected(true);
			nfStandardDeviationDoubles.setEnabled(true);
			nfStandardDeviationDoubles.setValue(standardDeviationDoubles);
		}
		else
		{
			chckbxStandardDeviationDoubles.setSelected(false);
			nfStandardDeviationDoubles.setEnabled(false);
			nfStandardDeviationDoubles.setValue(50);
		}
		
		double sdCentral = normalModel.getStandardDeviationCentral();
		if (sdCentral > 0)
		{
			chckbxCenterBias.setSelected(true);
			nfCentralBias.setEnabled(true);
			nfCentralBias.setValue(sdCentral);
		}
		else
		{
			chckbxCenterBias.setSelected(false);
			nfCentralBias.setEnabled(false);
			nfCentralBias.setValue(50);
		}
		
		int avgCount = normalModel.getRadiusAverageCount();
		chckbxRadiusAverage.setSelected(avgCount > 1);
		spinnerAverageCount.setEnabled(avgCount > 1);
		spinnerAverageCount.setValue(Math.max(2, avgCount));
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (arg0.getSource() == chckbxStandardDeviationDoubles)
		{
			boolean selected = chckbxStandardDeviationDoubles.isSelected();
			nfStandardDeviationDoubles.setEnabled(selected);
		}
		else if (arg0.getSource() == chckbxCenterBias)
		{
			boolean selected = chckbxCenterBias.isSelected();
			nfCentralBias.setEnabled(selected);
		}
		else if (arg0.getSource() == chckbxRadiusAverage)
		{
			boolean selected = chckbxRadiusAverage.isSelected();
			spinnerAverageCount.setEnabled(selected);
		}
	}
	
}