package burlton.dartzee.code.screen.stats.player;

import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.obj.HashMapCount;
import burlton.core.code.util.MathsUtil;
import burlton.dartzee.code.object.Dart;
import burlton.dartzee.code.stats.GameWrapper;
import burlton.desktopcore.code.bean.ScrollTable;
import burlton.desktopcore.code.util.ComponentUtil;
import burlton.desktopcore.code.util.TableUtil.DefaultModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StatisticsTabFinishBreakdown extends AbstractStatisticsTab
										  implements ListSelectionListener
{
	private Integer selectedScore = null;
	
	public StatisticsTabFinishBreakdown()
	{
		super();
		
		setLayout(new GridLayout(1, 3, 0, 0));
		
		tableFavouriteDoublesOther.setTableForeground(Color.RED);
		tableFavouriteDoubles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableFavouriteDoublesOther.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tablePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Double Finishes", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(tablePanel);
		tablePanel.setLayout(new GridLayout(2, 1, 0, 0));
		tablePanel.add(tableFavouriteDoubles);
		tablePanel.add(tableFavouriteDoublesOther);
		add(pieChartPanel);
		
		ListSelectionModel model = tableFavouriteDoubles.getSelectionModel();
		model.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		model.addListSelectionListener(this);
	}
	
	private final ScrollTable tableFavouriteDoubles = new ScrollTable();
	private final ScrollTable tableFavouriteDoublesOther = new ScrollTable();
	private final JPanel tablePanel = new JPanel();
	private final ChartPanel pieChartPanel = new ChartPanel(null);
	
	@Override
	public void populateStats()
	{
		setTableVisibility();
		
		DefaultPieDataset dataset = buildFavouriteDoublesData(tableFavouriteDoubles, filteredGames);
		if (includeOtherComparison())
		{
			buildFavouriteDoublesData(tableFavouriteDoublesOther, filteredGamesOther);
		}
		
		JFreeChart pieChart = ChartFactory.createPieChart("Finishes", dataset, true, true, false);
		PiePlot plot = (PiePlot)pieChart.getPlot();
		plot.setLabelGenerator(null);
		pieChartPanel.setChart(pieChart);
	}
	
	private DefaultPieDataset buildFavouriteDoublesData(ScrollTable table, HandyArrayList<GameWrapper> filteredGames)
	{
		DefaultModel model = new DefaultModel();
		model.addColumn("Double");
		model.addColumn("Finishes");
		model.addColumn("%");
		
		DefaultPieDataset dataset = populateFavouriteDoubles(model, filteredGames);
		
		table.setModel(model);
		table.sortBy(2, true);
		
		return dataset;
	}
	
	private DefaultPieDataset populateFavouriteDoubles(DefaultModel model, ArrayList<GameWrapper> filteredGames)
	{
		HashMapCount<Integer> hmScoreToCount = new HashMapCount<>();
		for (int i=0; i<filteredGames.size(); i++)
		{
			GameWrapper game = filteredGames.get(i);
			if (!game.isFinished())
			{
				continue;
			}
			
			List<Dart> darts = game.getDartsForFinalRound();
			Dart finalDart = darts.get(darts.size() - 1);
			int score = finalDart.getScore();
			
			hmScoreToCount.incrementCount(score);
		}
		
		ArrayList<Integer> doubles = hmScoreToCount.getKeysAsVector();
		for (int i=0; i<doubles.size(); i++)
		{
			int score = doubles.get(i);
			int count = hmScoreToCount.get(score);
			
			double total = hmScoreToCount.getTotalCount();
			double percent = MathsUtil.getPercentage(count, total);	
			
			//Object[] row = {score, count, percent, color};
			Object[] row = {score, count, percent};
			model.addRow(row);
		}
		
		//Build up the pie set. Unlike the table, we need ALL values
		DefaultPieDataset dataset = new DefaultPieDataset();
		for (int i=1; i<=20; i++)
		{
			int count = hmScoreToCount.getCount(i);
			dataset.setValue(Integer.valueOf(i), count);
		}
		
		dataset.setValue(Integer.valueOf(25), hmScoreToCount.getCount(25));
		return dataset;
	}
	
	private void setTableVisibility()
	{
		if (!includeOtherComparison())
		{
			tablePanel.setLayout(new GridLayout(1, 1, 0, 0));
			tablePanel.remove(tableFavouriteDoublesOther);
		}
		else if (!ComponentUtil.containsComponent(tablePanel, tableFavouriteDoublesOther))
		{
			tablePanel.setLayout(new GridLayout(2, 1, 0, 0));
			tablePanel.add(tableFavouriteDoublesOther);
		}
		
		tablePanel.repaint();
	}
	
	@Override
	public void valueChanged(ListSelectionEvent arg0)
	{
		JFreeChart pieChart = pieChartPanel.getChart();
		PiePlot plot = (PiePlot)pieChart.getPlot();
		if (selectedScore != null)
		{
			plot.setExplodePercent(selectedScore, 0);
		}
		
		int selectedRow = tableFavouriteDoubles.getSelectedModelRow();
		if (selectedRow == -1)
		{
			return;
		}
		
		selectedScore = (Integer)tableFavouriteDoubles.getValueAt(selectedRow, 0);
		plot.setExplodePercent(selectedScore, 0.2);
	}
}
