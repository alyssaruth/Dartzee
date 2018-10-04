package code.screen.stats.player;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.jfree.data.xy.XYSeries;

import bean.NumberField;
import code.bean.ScrollTableDartsGame;
import code.stats.GameWrapper;
import net.miginfocom.swing.MigLayout;
import object.HandyArrayList;
import util.TableUtil;
import util.TableUtil.DefaultModel;

public class StatisticsTabThreeDartAverage extends AbstractStatisticsTab
{
	public StatisticsTabThreeDartAverage() 
	{
		nfAverageThreshold.setPreferredSize(new Dimension(40, 20));
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.EAST);
		panel.setLayout(new MigLayout("", "[][grow]", "[][][][][][][]"));
		
		JLabel lblSetupThreshold = new JLabel("Setup Threshold");
		panel.add(lblSetupThreshold, "cell 0 0,alignx leading");
		
		
		nfScoringThreshold.setPreferredSize(new Dimension(40, 20));
		nfThreeDartAverage.setPreferredSize(new Dimension(40, 20));
		
		panel.add(nfScoringThreshold, "cell 1 0");
		
		nfScoringThreshold.setValue(140);
		
		nfAverageThreshold.setValue(5);
		
		panel.add(lblMovingAverageInterval, "cell 0 1,alignx leading");
		
		panel.add(nfAverageThreshold, "cell 1 1");
		
		panel.add(lblDartAverage, "cell 0 3,alignx leading");
		nfThreeDartAverage.setEditable(false);
		
		panel.add(nfThreeDartAverage, "flowx,cell 1 3");
		
		panel.add(lblMiss, "cell 0 4,alignx trailing");
		nfMissPercent.setPreferredSize(new Dimension(40, 20));
		nfMissPercent.setEditable(false);
		
		panel.add(nfMissPercent, "flowx,cell 1 4");
		panel_1.setBorder(new TitledBorder(null, "Raw Data", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		panel.add(panel_1, "cell 0 6 2 1");
		panel_1.setLayout(new BorderLayout(0, 0));
		
		panel_1.add(tableBestAverages);
		
		tableBestAverages.setPreferredScrollableViewportSize(new Dimension(300, 800));
		nfOtherThreeDartAvg.setPreferredSize(new Dimension(40, 20));
		nfOtherThreeDartAvg.setEditable(false);
		nfOtherThreeDartAvg.setForeground(Color.RED);
		
		panel.add(nfOtherThreeDartAvg, "cell 1 3");
		nfOtherMissPercent.setPreferredSize(new Dimension(40, 20));
		nfOtherMissPercent.setForeground(Color.RED);
		nfOtherMissPercent.setEditable(false);
		
		panel.add(nfOtherMissPercent, "cell 1 4");
		
		nfScoringThreshold.addPropertyChangeListener(this);
		nfAverageThreshold.addPropertyChangeListener(this);
		
		add(panelCenter, BorderLayout.CENTER);
		panelCenter.setLayout(new BorderLayout(0, 0));
		
		panelCenter.add(chartPanel, BorderLayout.CENTER);
	}
	private final NumberField nfScoringThreshold = new NumberField(62, 501);
	private final JLabel lblMovingAverageInterval = new JLabel("Moving Average Interval");
	private final NumberField nfAverageThreshold = new NumberField(1, 200);
	private final JLabel lblDartAverage = new JLabel("3 Dart Average");
	private final JTextField nfThreeDartAverage = new JTextField();
	private final JPanel panel_1 = new JPanel();
	private final ScrollTableDartsGame tableBestAverages = new ScrollTableDartsGame();
	private final JTextField nfOtherThreeDartAvg = new JTextField();
	private final JPanel panelCenter = new JPanel();
	private final MovingAverageChartPanel chartPanel = new MovingAverageChartPanel(this);
	private final JLabel lblMiss = new JLabel("Miss %");
	private final JTextField nfMissPercent = new JTextField();
	private final JTextField nfOtherMissPercent = new JTextField();

	@Override
	public void populateStats()
	{
		chartPanel.init("3 Dart Average", "Average");
		
		nfOtherThreeDartAvg.setVisible(includeOtherComparison());
		nfOtherMissPercent.setVisible(includeOtherComparison());
		
		//Construct the table model
		TableUtil.DefaultModel model = new TableUtil.DefaultModel();
		model.addColumn("Ordinal");
		model.addColumn("Average");
		model.addColumn("Start Value");
		model.addColumn("Game");
		
		populateStats(filteredGames, model, nfThreeDartAverage, nfMissPercent, "");
		if (includeOtherComparison())
		{
			populateStats(filteredGamesOther, null, nfOtherThreeDartAvg, nfOtherMissPercent, " (Other)");
		}
		
		chartPanel.finalise();
		
		//Finish off the table model
		tableBestAverages.setModel(model);
		tableBestAverages.sortBy(0, false);
	}
	
	private void populateStats(HandyArrayList<GameWrapper> filteredGames, DefaultModel model, JTextField nfThreeDartAverage,
	  JTextField nfMissPercent, String graphSuffix)
	{
		//Filter out unfinished games, then sort by start date
		HandyArrayList<GameWrapper> filteredGamesFinished = filteredGames.createFilteredCopy(g -> g.isFinished());
		filteredGamesFinished.sort((GameWrapper g1, GameWrapper g2) -> g1.getDtStart().compareTo(g2.getDtStart()));
		
		double misses = 0;
		double dartsTotal = 0;
		double avgTotal = 0;
		int scoreThreshold = nfScoringThreshold.getNumber();
		XYSeries rawAverages = new XYSeries("Avg" + graphSuffix);
		for (int i=0; i<filteredGamesFinished.size(); i++)
		{
			//Get the relevant fields off the game
			GameWrapper game = filteredGamesFinished.get(i);
			int ordinal = (i+1);
			double avg = game.getThreeDartAverage(scoreThreshold);
			int startValue = game.getGameStartValueX01();
			long gameId = game.getGameId();
			
			dartsTotal += game.getScoringDarts(scoreThreshold).size();
			misses += game.getMissedDartsX01(scoreThreshold);
			
			//Table row - only show the raw data for the actual player, not the comparison
			if (model != null)
			{
				Object[] row = {ordinal, avg, startValue, gameId};
				model.addRow(row);
			}
			
			//Graph point
			rawAverages.add(ordinal, avg);
			
			//Increment the total average
			avgTotal += avg;
		}
		
		chartPanel.addSeries(rawAverages, graphSuffix, nfAverageThreshold.getNumber());
		
		//Overall avg, to 1 d.p
		double totalAverage = (double)Math.round(10 * avgTotal / filteredGamesFinished.size()) / 10;
		nfThreeDartAverage.setText("" + totalAverage);
		
		//Miss percent, to 1 d.p
		double missPercentage = (double)Math.round(1000 * misses / dartsTotal) / 10;
		nfMissPercent.setText("" + missPercentage);
	}
}
