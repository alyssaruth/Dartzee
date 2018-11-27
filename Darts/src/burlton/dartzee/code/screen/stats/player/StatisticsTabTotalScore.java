package burlton.dartzee.code.screen.stats.player;

import burlton.desktopcore.code.bean.NumberField;
import burlton.dartzee.code.bean.ScrollTableDartsGame;
import burlton.dartzee.code.stats.GameWrapper;
import net.miginfocom.swing.MigLayout;
import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.obj.HashMapCounter;
import burlton.core.code.obj.HashMapCountInteger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.XYSeries;
import burlton.desktopcore.code.util.TableUtil.DefaultModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * A tab to show bar a line representations for the 'FinalScore' column on Participant.
 * Configurable title so it can show "Total Darts" for X01, etc. Also displays mean and median values for FinalScore.
 *
 */
public class StatisticsTabTotalScore extends AbstractStatisticsTab
								  implements ActionListener
{
	private String graphTitle = "";
	
	private int graphMin = Integer.MAX_VALUE;
	private int graphMax = -1;
	
	private DefaultCategoryDataset dataset = null;
	private DefaultBoxAndWhiskerCategoryDataset boxDataset = null;
	
	public StatisticsTabTotalScore(String graphTitle, int outlierMax) 
	{
		nfMedianOther.setPreferredSize(new Dimension(50, 20));
		nfMeanOther.setPreferredSize(new Dimension(50, 20));
		this.graphTitle = graphTitle;
		
		setLayout(new BorderLayout(0, 0));
		
		add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addTab("", new ImageIcon(StatisticsTabTotalScore.class.getResource("/icons/bar chart.png")), panelBar, null);
		panelBar.setLayout(new BorderLayout(0, 0));
		panelBar.add(panel, BorderLayout.CENTER);
		
		panelBar.add(panelBarConfiguration, BorderLayout.EAST);
		panelBarConfiguration.setLayout(new MigLayout("", "[][grow]", "[][][]"));
		
		JLabel lblOutlierCutoff = new JLabel("Outlier cutoff");
		panelBarConfiguration.add(lblOutlierCutoff, "cell 0 0");
		panelBarConfiguration.add(nfOutlier, "cell 1 0");
		nfOutlier.setValue(75);
		nfOutlier.setPreferredSize(new Dimension(50, 20));
		
		nfOutlier.addPropertyChangeListener(this);
		panelBarConfiguration.add(lblGrouping, "cell 0 1");
		panelBarConfiguration.add(nfGroups, "cell 1 1");
		nfGroups.setValue(3);
		
		nfGroups.setPreferredSize(new Dimension(50, 20));
		
		tabbedPane.addTab("", new ImageIcon(StatisticsTabTotalScore.class.getResource("/icons/line chart.png")), panelLine, null);
		panelLine.setLayout(new BorderLayout(0, 0));
		
		panelLine.add(lineChartPanel, BorderLayout.CENTER);
		
		panelLine.add(panelLineConfiguration, BorderLayout.EAST);
		panelLineConfiguration.setLayout(new MigLayout("", "[][]", "[]"));
		
		panelLineConfiguration.add(lblMovingAvg, "flowx,cell 0 0");
		nfAverageThreshold.setValue(5);
		nfAverageThreshold.setPreferredSize(new Dimension(40, 20));
		
		panelLineConfiguration.add(nfAverageThreshold, "cell 1 0");
		
		tabbedPane.addTab("", new ImageIcon(StatisticsTabTotalScore.class.getResource("/icons/boxAndWhisker.png")), panelBoxAndWhisker, null);
		panelBoxAndWhisker.setLayout(new BorderLayout(0, 0));
		
		panelBoxAndWhisker.add(boxAndWhiskerChartPanel, BorderLayout.CENTER);
		nfGroups.addPropertyChangeListener(this);
		
		JPanel panelRawData = new JPanel();
		add(panelRawData, BorderLayout.WEST);
		panelRawData.setLayout(new MigLayout("", "[][]", "[][][][][][grow][][]"));
		panelRawData.add(lblGameType, "cell 0 0");
		panelRawData.add(comboBox, "flowx,cell 1 0");
		
		panelRawData.add(lblMean, "cell 0 2,alignx leading");
		nfMean.setPreferredSize(new Dimension(50, 20));
		
		panelRawData.add(nfMean, "cell 1 2");
		
		nfMean.setEditable(false);
		nfMeanOther.setEditable(false);
		nfMedianOther.setEditable(false);
		
		nfMeanOther.setForeground(Color.RED);
		nfMedianOther.setForeground(Color.RED);
		
		panelRawData.add(lblMedian, "cell 0 3,alignx leading");
		nfMedian.setPreferredSize(new Dimension(50, 20));
		
		panelRawData.add(nfMedian, "flowx,cell 1 3");
		nfMedian.setEditable(false);
		panel_3.setBorder(new TitledBorder(null, "Raw Data", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		panelRawData.add(panel_3, "cell 0 5 2 3,grow");
		panel_3.setLayout(new BorderLayout(0, 0));
		
		panel_3.add(table);
		table.setPreferredScrollableViewportSize(new Dimension(300, 800));
		nfAverageThreshold.addPropertyChangeListener(this);
		panelRawData.add(nfMeanOther, "cell 1 2");
		
		panelRawData.add(nfMedianOther, "cell 1 3");
		
		panelRawData.add(chckbxIncludeUnfinishedGames, "cell 1 0 2 1");
		
		nfOutlier.setMaximum(outlierMax);
		
		chckbxIncludeUnfinishedGames.addActionListener(this);
		comboBox.addActionListener(this);
	}
	
	private final ChartPanel panel = new ChartPanel(null);
	private final NumberField nfOutlier = new NumberField(10, 200);
	private final JLabel lblGrouping = new JLabel("Grouping");
	private final NumberField nfGroups = new NumberField(1, 20);
	private final JPanel panelBar = new JPanel();
	private final JPanel panelBarConfiguration = new JPanel();
	private final JLabel lblMean = new JLabel("Mean");
	private final JLabel lblMedian = new JLabel("Median");
	private final NumberField nfMean = new NumberField();
	private final NumberField nfMedian = new NumberField();
	private final JLabel lblGameType = new JLabel("Game Type");
	private final JPanel panel_3 = new JPanel();
	private final ScrollTableDartsGame table = new ScrollTableDartsGame();
	private final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
	private final JComboBox<String> comboBox = new JComboBox<>();
	private final NumberField nfMeanOther = new NumberField();
	private final NumberField nfMedianOther = new NumberField();
	private final JPanel panelLine = new JPanel();
	private final MovingAverageChartPanel lineChartPanel = new MovingAverageChartPanel(this);
	private final JPanel panelLineConfiguration = new JPanel();
	private final JLabel lblMovingAvg = new JLabel("Moving avg");
	private final NumberField nfAverageThreshold = new NumberField(1, 200);
	private final JPanel panelBoxAndWhisker = new JPanel();
	private final ChartPanel boxAndWhiskerChartPanel = new ChartPanel(null);
	private final JCheckBox chckbxIncludeUnfinishedGames = new JCheckBox("Include unfinished");

	@Override
	public void populateStats()
	{
		String gameParams = initialiseFields();
		
		populateStatsWithoutChangingFields(gameParams);
	}
	private void populateStatsWithoutChangingFields(String gameParams)
	{
		lineChartPanel.init(graphTitle + " (" + gameParams + ")", graphTitle);
		
		//Filter out unfinished games and games with the wrong params
		Predicate<GameWrapper> filter = g -> (g.getGameParams().equals(gameParams)) && (g.isFinished() || chckbxIncludeUnfinishedGames.isSelected());
		HandyArrayList<GameWrapper> gamesToGraph = filteredGames.createFilteredCopy(filter);
		HandyArrayList<GameWrapper> otherGamesToGraph = filteredGamesOther.createFilteredCopy(filter);
		boolean includeOther = !otherGamesToGraph.isEmpty();
		
		//Sort out what the min and max displayed on the graph will be
		adjustGraphMinAndMax(gamesToGraph, otherGamesToGraph);
		
		//Populate the raw data table
		populateTable(gamesToGraph);
		
		dataset = new DefaultCategoryDataset();
		boxDataset = new DefaultBoxAndWhiskerCategoryDataset();
		
		addValuesToDataset(gamesToGraph, "Me", nfMedian, nfMean);
		if (includeOther)
		{
			addValuesToDataset(otherGamesToGraph, "Other", nfMedianOther, nfMeanOther);
		}
		
		finaliseBarChart(gameParams);
		finaliseBoxPlot(gameParams);
		
		nfMeanOther.setVisible(includeOther);
		nfMedianOther.setVisible(includeOther);
		
		lineChartPanel.finalise();
	}
	private String initialiseFields()
	{
		String gameParams = initialiseGameTypeComboBox();
		
		initialiseOutlierCutOffAndGrouping(gameParams);
		
		return gameParams;
	}
	private void initialiseOutlierCutOffAndGrouping(String gameParams)
	{
		Predicate<GameWrapper> filter = g -> (g.getGameParams().equals(gameParams)) && g.isFinished();
		HandyArrayList<GameWrapper> gamesToGraph = filteredGames.createFilteredCopy(filter);
		if (gamesToGraph.isEmpty())
		{
			return;
		}
		
		HandyArrayList<Integer> scores = new HandyArrayList<>();
		for (GameWrapper g : gamesToGraph)
		{
			scores.add(g.getFinalScore());
		}
		
		scores.sort((Integer i1, Integer i2) -> Integer.compare(i1, i2));
		
		int lqIndex = scores.size() / 4;
		int uqIndex = 3 * lqIndex;
		int uq = scores.get(uqIndex);
		int iqr = uq - scores.get(lqIndex);
		
		int outlierThreshold = uq + ((3*iqr)/2);
		nfOutlier.setValue(outlierThreshold);
		
		int min = scores.firstElement();
		int max = scores.lastElement();
		
		//Go for 10 bars, whatever that works out to be
		int grouping = (max - min)/10;
		grouping = Math.max(1, grouping);
		nfGroups.setValue(grouping);
	}
	private void finaliseBarChart(String gameParams)
	{
		JFreeChart barChart = ChartFactory.createBarChart(
				graphTitle + " (" + gameParams + ")",           
		         graphTitle,            
		         "Count",            
		         dataset,          
		         PlotOrientation.VERTICAL,           
		         true, true, false);
		
		CategoryPlot plot = barChart.getCategoryPlot();
		plot.getRenderer().setSeriesPaint(0, Color.BLUE);
		if (includeOtherComparison())
		{
			plot.getRenderer().setSeriesPaint(1, Color.RED);
		}
		
		panel.setChart(barChart);
	}
	private void finaliseBoxPlot(String gameParams)
	{
		JFreeChart boxChart = ChartFactory.createBoxAndWhiskerChart(graphTitle + " (" + gameParams + ")", 
		  "", "", boxDataset, true);
		
		//BoxAndWhiskerRenderer renderer = (BoxAndWhiskerRenderer)boxChart.getCategoryPlot().getRenderer();
		FixedBoxAndWhiskerRenderer renderer = new FixedBoxAndWhiskerRenderer();
		//renderer.setMeanVisible(false);
		renderer.setSeriesPaint(0, Color.BLUE);
		if (includeOtherComparison())
		{
			renderer.setSeriesPaint(1, Color.RED);
		}
		
		CategoryPlot plot = boxChart.getCategoryPlot();
		plot.setOrientation(PlotOrientation.HORIZONTAL);
		
		plot.setRenderer(renderer);
		
		boxAndWhiskerChartPanel.setChart(boxChart);
	}
	private void populateTable(HandyArrayList<GameWrapper> gamesToGraph)
	{
		DefaultModel model = new DefaultModel();
		model.addColumn("Ordinal");
		model.addColumn("Score");
		model.addColumn("Game");
		model.addColumn("!Unfinished");
		
		gamesToGraph.sort((GameWrapper g1, GameWrapper g2) -> g1.compareStartDate(g2));
		
		for (int i=0; i<gamesToGraph.size(); i++)
		{
			GameWrapper g = gamesToGraph.get(i);
			
			boolean unfinished = false;
			int finalScore = g.getFinalScore();
			if (finalScore == -1)
			{
				unfinished = true;
				finalScore = g.getAllDarts().size();
			}
			
			Object[] row = {(i+1), finalScore, g.getGameId(), unfinished};
			model.addRow(row);
		}
		
		table.setModel(model);
		table.setRenderer(1, new TotalScoreRenderer());
		table.removeColumn(3);
	}
	
	/**
	 * No more mentalness with radio buttons (though it was fun...)
	 * Now just have a combo box that we populate. Still try to preserve the previous selection if we can.
	 */
	private String initialiseGameTypeComboBox()
	{
		//Remember what was selected previously.
		Object selectedItem = comboBox.getSelectedItem();
		
		//Now get what scores should now show
		HandyArrayList<String> startingScores = getDistinctGameParams();
		startingScores.sort(Comparator.naturalOrder());
		
		//Handle 0 games
		if (startingScores.isEmpty())
		{
			startingScores.add("N/A");
		}
		
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(startingScores.toGenericArray());
		comboBox.setModel(model);
		
		comboBox.setSelectedItem(selectedItem);
		
		int ix = comboBox.getSelectedIndex();
		if (ix == -1)
		{
			ix = 0;
			comboBox.setSelectedIndex(0);
		}
		
		return comboBox.getItemAt(ix);
	}
	
	
	/**
	 * Get the minimum and maximum number of darts for the graph
	 */
	private void adjustGraphMinAndMax(HandyArrayList<GameWrapper> gamesToGraph, HandyArrayList<GameWrapper> gamesToGraphOther)
	{
		HandyArrayList<GameWrapper> combined = new HandyArrayList<>(gamesToGraph);
		combined.addAll(gamesToGraphOther);
		combined = combined.createFilteredCopy(g -> g.isFinished());
		
		if (combined.isEmpty())
		{
			graphMin = Integer.MAX_VALUE;
			graphMax = Integer.MIN_VALUE;
			return;
		}
		
		try (IntStream stream = combined.stream().mapToInt(g -> g.getFinalScore()))
		{
			graphMin = stream.min().getAsInt();
		}
		
		try (IntStream stream = combined.stream().mapToInt(g -> g.getFinalScore()))
		{
			graphMax = stream.max().getAsInt();
		}
	}
	
	/**
	 * Deal with populating the dataset used by the bar chart
	 */
	private void addValuesToDataset(HandyArrayList<GameWrapper> gamesToGraph, String legendKey, NumberField nfMedian, NumberField nfMean)
	{
		//Build up counts for each game finish value
		String suffix = " (" + legendKey + ")";
		XYSeries series = new XYSeries(graphTitle + suffix);
		HashMapCountInteger hmNoDartsToCount = new HashMapCountInteger();
		for (int i=0; i<gamesToGraph.size(); i++)
		{
			GameWrapper game = gamesToGraph.get(i);
			int score = game.getFinalScore();
			if (score > -1)
			{
				series.add((i+1), score);
				hmNoDartsToCount.incrementCount(score);
			}
		}
		
		lineChartPanel.addSeries(series, suffix, nfAverageThreshold.getNumber());
		
		appendToDataset(legendKey, hmNoDartsToCount);

		double avg = hmNoDartsToCount.calculateAverage();
		double median = hmNoDartsToCount.calculateMedian();
		nfMedian.setValue(median);
		nfMean.setValue(avg);
	}
	private void appendToDataset(String legendKey, HashMapCounter<Integer> hmNoDartsToCount)
	{
		int outlierLimit = nfOutlier.getNumber();
		int groups = nfGroups.getNumber();
		
		int groupCount = 0;
		int rangeStart = graphMin;
		for (int i=graphMin; i<=Math.min(outlierLimit, graphMax); i++)
		{
			groupCount += hmNoDartsToCount.getCount(i);
			
			//If we're a multiple of the group #...
			if (i % groups == 0
			  || (i == Math.min(outlierLimit, graphMax)))
			{
				String rangeDesc = getRangeDesc(rangeStart, i);
				dataset.addValue(groupCount, legendKey, rangeDesc);
				
				//Set up for the next block
				groupCount = 0;
				rangeStart = i+1;
			}
		}
		
		//Add outliers on the end
		int outlierCount = 0;
		for (int i=outlierLimit+1; i<=graphMax; i++)
		{
			outlierCount += hmNoDartsToCount.getCount(i);
		}
		
		dataset.addValue(outlierCount, legendKey, (outlierLimit + 1) + "+");
		
		//Also add to the Box and Whisker dataset
		ArrayList<Integer> allValues = hmNoDartsToCount.getFlattenedOrderedList(null);
		boxDataset.add(allValues, legendKey, "");
	}
	private String getRangeDesc(int start, int finish)
	{
		if (start == finish)
		{
			return "" + start;
		}
		
		return start + " - " + finish;
	}
	
	/**
	 * GameParams combo box
	 */
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		populateStats();
	}
	
	/**
	 * The number fields
	 */
	@Override
	public void propertyChange(PropertyChangeEvent arg0)
	{
		String propertyName = arg0.getPropertyName();
		if (propertyName.equals("value"))
		{
			String selectedGameParams = (String)comboBox.getSelectedItem();
			populateStatsWithoutChangingFields(selectedGameParams);
		}
	}
	
	private static class TotalScoreRenderer extends DefaultTableCellRenderer
	{
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column)
		{
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			row = table.getRowSorter().convertRowIndexToModel(row);
			
			DefaultModel model = (DefaultModel)table.getModel();
			boolean unfinished = (boolean)model.getValueAt(row, 3);
			if (unfinished)
			{
				if (isSelected)
				{
					setForeground(Color.CYAN);
				}
				else
				{
					setForeground(Color.RED);
				}
				
			}
			else
			{
				if (isSelected)
				{
					setForeground(Color.WHITE);
				}
				else
				{
					setForeground(Color.BLACK);
				}
			}
			
			return this;
		}
	}
}
