package burlton.dartzee.code.screen.ai;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import burlton.desktopcore.code.bean.ComboBoxItem;
import burlton.dartzee.code.ai.AbstractDartsModel;
import burlton.dartzee.code.object.DartboardSegment;

public class AIConfigurationGolfDartPanel extends JPanel
										  implements ChangeListener
{
	private int dartNo = -1;
	
	public AIConfigurationGolfDartPanel(int dartNo) 
	{
		this.dartNo = dartNo;
		
		setBorder(new TitledBorder(null, "Dart #" + dartNo, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JLabel lblAimAt = new JLabel("Aim at");
		add(lblAimAt);
		
		
		add(comboBox);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		add(horizontalStrut);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		add(horizontalStrut_1);
		
		
		add(panelStoppingPoint);
		
		JLabel lblStopIfScored = new JLabel("Stop if scored ");
		panelStoppingPoint.add(lblStopIfScored);
		
		
		panelStoppingPoint.add(spinner);
		
		
		panelStoppingPoint.add(lblOrBelow);
		spinner.addChangeListener(this);
		
		setModels();
		setComponentVisibility();
	}
	
	private final JComboBox<ComboBoxItem<Integer>> comboBox = new JComboBox<>();
	private final JPanel panelStoppingPoint = new JPanel();
	private final JSpinner spinner = new JSpinner();
	private final JLabel lblOrBelow = new JLabel("or better");
	
	
	private void setModels()
	{
		comboBox.addItem(new ComboBoxItem<>(DartboardSegment.SEGMENT_TYPE_DOUBLE, "Double (1)"));
		comboBox.addItem(new ComboBoxItem<>(DartboardSegment.SEGMENT_TYPE_TREBLE, "Treble (2)"));
		comboBox.addItem(new ComboBoxItem<>(DartboardSegment.SEGMENT_TYPE_INNER_SINGLE, "Inner Single (3)"));
		comboBox.addItem(new ComboBoxItem<>(DartboardSegment.SEGMENT_TYPE_OUTER_SINGLE, "Outer Single (4)"));
		
		spinner.setModel(new SpinnerNumberModel(2, 1, 4, 1));
	}
	
	private void setComponentVisibility()
	{
		panelStoppingPoint.setVisible(dartNo < 3);
		
		int value = (int)spinner.getValue();
		lblOrBelow.setEnabled(value > 1);
	}
	
	public void initialiseFromModel(AbstractDartsModel model)
	{
		//Combo box selection
		int segmentType = model.getSegmentTypeForDartNo(dartNo);
		for (int i=0; i<comboBox.getItemCount(); i++)
		{
			ComboBoxItem<Integer> item = comboBox.getItemAt(i);
			if (item.getHiddenData() == segmentType)
			{
				comboBox.setSelectedItem(item);
			}
		}
		
		if (spinner.isVisible())
		{
			int stopThreshold = model.getStopThresholdForDartNo(dartNo);
			spinner.setValue(stopThreshold);
		}
	}
	
	public void populateModel(AbstractDartsModel model)
	{
		int ix = comboBox.getSelectedIndex();
		ComboBoxItem<Integer> item = comboBox.getItemAt(ix);
		int segmentType = item.getHiddenData();
		
		model.setSegmentTypeForDartNo(dartNo, segmentType);
		
		if (spinner.isVisible())
		{
			int stopThreshold = (int)spinner.getValue();
			model.setStopThresholdForDartNo(dartNo, stopThreshold);
		}
	}

	@Override
	public void stateChanged(ChangeEvent arg0)
	{
		setComponentVisibility();
	}
}
