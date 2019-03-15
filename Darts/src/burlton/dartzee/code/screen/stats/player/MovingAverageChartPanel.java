package burlton.dartzee.code.screen.stats.player;

import burlton.dartzee.code.utils.DartsColour;
import burlton.desktopcore.code.util.ComponentUtilKt;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class MovingAverageChartPanel extends JPanel
									 implements ActionListener
{
	private XYSeriesCollection graphCollection = new XYSeriesCollection();
	private JFreeChart chart = null;
	private AbstractStatisticsTab parentTab = null;
	
	public MovingAverageChartPanel(AbstractStatisticsTab parentTab) 
	{
		this.parentTab = parentTab;
		
		setLayout(new BorderLayout(0, 0));
		panelGraph.setLayout(new BorderLayout(0, 0));
		
		add(panelGraph, BorderLayout.CENTER);
		add(panelCheckBoxes, BorderLayout.SOUTH);
	}
	
	private final ChartPanel panelGraph = new ChartPanel(null);
	private final JPanel panelCheckBoxes = new JPanel();
	
	public void init(String title, String yLabel)
	{
		graphCollection = new XYSeriesCollection();
		
		chart = ChartFactory.createXYLineChart(title, "Game", yLabel, graphCollection);
		panelGraph.setChart(chart);
	}
	
	public void finalise()
	{
		adjustTickboxes();
		setPlotColours(chart);
	}
	
	private void adjustTickboxes()
	{
		List<JCheckBox> checkBoxes = ComponentUtilKt.getAllChildComponentsForType(panelCheckBoxes, JCheckBox.class);
		
		//Remove any bad ones
		for (JCheckBox checkBox : checkBoxes)
		{
			if (getGraphSeriesIndexForCheckBox(checkBox) == -1)
			{
				panelCheckBoxes.remove(checkBox);
			}
		}
		
		//Update this variable
		checkBoxes = ComponentUtilKt.getAllChildComponentsForType(panelCheckBoxes, JCheckBox.class);
		
		//Go through each series in the graph and ensure it has a checkbox
		List<XYSeries> allSeries = graphCollection.getSeries();
		for (int i=0; i<allSeries.size(); i++)
		{
			XYSeries series = allSeries.get(i);
			String key = (String)series.getKey();
			if (matchingCheckBoxExists(checkBoxes, key))
			{
				continue;
			}
			
			JCheckBox checkBox = new JCheckBox(key);
			checkBox.setSelected(true);
			checkBox.addActionListener(this);
			panelCheckBoxes.add(checkBox);
		}
		
		//Now for any checkBox that remains and is unticked, remove the graph series
		for (JCheckBox checkBox : checkBoxes)
		{
			if (checkBox.isSelected())
			{
				continue;
			}
			
			int graphSeries = getGraphSeriesIndexForCheckBox(checkBox);
			graphCollection.removeSeries(graphSeries);
		}
	}
	private boolean matchingCheckBoxExists(List<JCheckBox> checkBoxes, String graphKey)
	{
		for (JCheckBox checkBox : checkBoxes)
		{
			String cbText = checkBox.getText();
			if (cbText.equals(graphKey))
			{
				return true;
			}
		}
		
		return false;
	}
	private int getGraphSeriesIndexForCheckBox(JCheckBox checkBox)
	{
		String text = checkBox.getText();
		
		List<XYSeries> allSeries = graphCollection.getSeries();
		for (int i=0; i<allSeries.size(); i++)
		{
			XYSeries series = allSeries.get(i);
			String key = (String)series.getKey();
			
			if (key.equals(text))
			{
				return i;
			}
		}
		
		return -1;
	}
	
	private void setPlotColours(JFreeChart chart)
	{
		XYPlot plot = (XYPlot)chart.getPlot();
		
		List<XYSeries> allSeries = graphCollection.getSeries();
		for (int i=0; i<allSeries.size(); i++)
		{
			XYSeries series = allSeries.get(i);
			String key = (String)series.getKey();
			
			Color colour = Color.blue;
			if (key.contains("Other"))
			{
				colour = Color.red;
			}
			
			if (key.contains("Moving"))
			{
				colour = DartsColour.getBrightenedColour(colour);
			}
			
			plot.getRenderer().setSeriesPaint(i, colour);
		}
	}
	
	public void addSeries(XYSeries series, String key, int avgThreshold)
	{
		XYSeries movingAvgSeries = createMovingAverage(series, key, avgThreshold);
		graphCollection.addSeries(series);
		graphCollection.addSeries(movingAvgSeries);
	}
	private XYSeries createMovingAverage(XYSeries original, String key, int avgThreshold)
	{
		XYSeriesCollection collection = new XYSeriesCollection(original);
		XYSeriesCollection movingAvgCollection = (XYSeriesCollection)MovingAverage.createMovingAverage(collection, "", avgThreshold, avgThreshold-1);
		
		XYSeries movingAvgSeries = movingAvgCollection.getSeries(0);
		movingAvgSeries.setKey("Moving avg " + key);
		return movingAvgSeries;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		parentTab.populateStats();
	}
}
