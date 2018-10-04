package code.screen.stats.player;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import bean.RowSelectionListener;
import bean.ScrollTable;
import code.screen.game.DartsScorerGolf;
import code.stats.GameWrapper;
import object.HandyArrayList;
import object.SuperHashMap;
import util.ComponentUtil;
import util.Debug;
import util.TableUtil.DefaultModel;

public class StatisticsTabGolfHoleBreakdown extends AbstractStatisticsTab
										  implements RowSelectionListener
{
	public StatisticsTabGolfHoleBreakdown()
	{
		super();
		
		setLayout(new GridLayout(1, 3, 0, 0));
		
		tableHoleBreakdownOther.setTableForeground(Color.RED);
		tableHoleBreakdown.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableHoleBreakdownOther.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tablePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Double Finishes", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(tablePanel);
		tablePanel.setLayout(new GridLayout(2, 1, 0, 0));
		tablePanel.add(tableHoleBreakdown);
		tablePanel.add(tableHoleBreakdownOther);
		add(pieChartPanel);
		pieChartPanel.setLayout(new GridLayout(2, 1, 0, 0));
		
		pieChartPanel.add(myPieChartPanel);
		
		pieChartPanel.add(otherPieChartPanel);
		
		tableHoleBreakdown.addRowSelectionListener(this);
		tableHoleBreakdownOther.addRowSelectionListener(this);
	}
	
	private final ScrollTable tableHoleBreakdown = new ScrollTable();
	private final ScrollTable tableHoleBreakdownOther = new ScrollTable();
	private final JPanel tablePanel = new JPanel();
	private final JPanel pieChartPanel = new JPanel();
	private final ChartPanel myPieChartPanel = new ChartPanel(null);
	private final ChartPanel otherPieChartPanel = new ChartPanel(null);
	
	@Override
	public void populateStats()
	{
		setTableVisibility();
		
		populateHoleBreakdown(tableHoleBreakdown, myPieChartPanel, filteredGames);
		if (includeOtherComparison())
		{
			populateHoleBreakdown(tableHoleBreakdownOther, otherPieChartPanel, filteredGamesOther);
		}
	}
	
	private void populateHoleBreakdown(ScrollTable table, ChartPanel chartPanel, HandyArrayList<GameWrapper> filteredGames)
	{
		DefaultModel model = new DefaultModel();
		model.addColumn("Hole");
		model.addColumn("1");
		model.addColumn("2");
		model.addColumn("3");
		model.addColumn("4");
		model.addColumn("5");
		model.addColumn("Avg");
		table.setModel(model);
		
		populateModel(table, filteredGames);
		
		table.sortBy(0, false);
		table.disableSorting();
		table.selectFirstRow();
		
		updatePieChart(table, chartPanel);
	}
	private void populateModel(ScrollTable table, HandyArrayList<GameWrapper> filteredGames)
	{
		SuperHashMap<Integer, HoleBreakdownWrapper> hm = new SuperHashMap<>();
		for (GameWrapper game : filteredGames)
		{
			game.updateHoleBreakdowns(hm);
		}
		
		HoleBreakdownWrapper overall = hm.remove(-1);
		ArrayList<Integer> holes = hm.getKeysAsVector();
		for (int hole : holes)
		{
			HoleBreakdownWrapper bd = hm.get(hole);
			
			Object[] row = bd.getAsTableRow(hole);
			table.addRow(row);
		}
		
		//Handle 0 games
		if (overall != null)
		{
			Object[] totalRow = overall.getAsTableRow("Overall");
			table.addFooterRow(totalRow);
		}
	}
	
	private void setTableVisibility()
	{
		if (!includeOtherComparison())
		{
			pieChartPanel.setLayout(new GridLayout(1, 1, 0, 0));
			pieChartPanel.remove(otherPieChartPanel);
			tablePanel.setLayout(new GridLayout(1, 1, 0, 0));
			tablePanel.remove(tableHoleBreakdownOther);
		}
		else if (!ComponentUtil.containsComponent(tablePanel, tableHoleBreakdownOther))
		{
			pieChartPanel.setLayout(new GridLayout(2, 1, 0, 0));
			pieChartPanel.add(otherPieChartPanel);
			tablePanel.setLayout(new GridLayout(2, 1, 0, 0));
			tablePanel.add(tableHoleBreakdownOther);
		}
		
		tablePanel.repaint();
	}
	
	private void updatePieChart(ScrollTable table, ChartPanel panel)
	{
		int selectedRow = table.getSelectedModelRow();
		if (selectedRow == -1)
		{
			//Do nothing
			return;
		}
		
		Object selectedHole = table.getValueAt(selectedRow, 0);
		
		DefaultPieDataset dataset = new DefaultPieDataset();
		JFreeChart pieChart = ChartFactory.createPieChart("" + selectedHole, dataset, true, true, false);
		PiePlot plot = (PiePlot)pieChart.getPlot();
		
		for (int i=1; i<=5; i++)
		{
			dataset.setValue(Integer.valueOf(i), (int)table.getValueAt(selectedRow, i));
			
			Color colour = DartsScorerGolf.getScorerColour(i, 1);
			plot.setSectionPaint(Integer.valueOf(i), colour);
		}
		
		
		plot.setLabelGenerator(null);
		panel.setChart(pieChart);
	}
	
	private void updateSelection(ScrollTable src, ScrollTable dest)
	{
		int row = src.getSelectedModelRow();
		if (row < dest.getRowCount())
		{
			dest.selectRow(row);
		}
	}
	
	@Override
	public void selectionChanged(ScrollTable src)
	{
		if (src == tableHoleBreakdown)
		{
			updateSelection(tableHoleBreakdown, tableHoleBreakdownOther);
			updatePieChart(tableHoleBreakdown, myPieChartPanel);
		}
		else if (src == tableHoleBreakdownOther)
		{
			updateSelection(tableHoleBreakdownOther, tableHoleBreakdown);
			updatePieChart(tableHoleBreakdownOther, otherPieChartPanel);
		}
		else
		{
			Debug.stackTrace("Unexpected valueChange [" + src + "]");
		}
	}
}
