package code.screen.game;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.swing.JLabel;
import javax.swing.JPanel;

import bean.NumberField;
import code.object.Dart;
import code.utils.X01Util;
import object.HandyArrayList;
import util.MathsUtil;

/**
 * Shows running stats for X01 games - three-dart average, checkout % etc.
 */
public class GameStatisticsPanelX01 extends GameStatisticsPanel
									implements PropertyChangeListener
{
	public GameStatisticsPanelX01() 
	{
		super();
		
		
		add(panel, BorderLayout.NORTH);
		panel.add(lblSetupThreshold);
		
		panel.add(nfSetupThreshold);
		nfSetupThreshold.setColumns(10);
		
		nfSetupThreshold.setValue(100);
		nfSetupThreshold.addPropertyChangeListener(this);
	}
	
	private final JPanel panel = new JPanel();
	private final JLabel lblSetupThreshold = new JLabel("Setup Threshold");
	private final NumberField nfSetupThreshold = new NumberField();
	
	
	@Override
	protected void addRowsToTable()
	{
		nfSetupThreshold.setMinimum(62);
		nfSetupThreshold.setMaximum(Integer.parseInt(gameParams) - 1);
		
		addRow(getScoreRow(i -> i.max().getAsInt(), "Highest Score"));
		addRow(getThreeDartAvgsRow());
		addRow(getScoreRow(i -> i.min().getAsInt(), "Lowest Score"));
		addRow(getMissesRow());
		
		addRow(new Object[getRowWidth()]);
		
		addRow(getScoresBetween(180, 181, "180"));
		addRow(getScoresBetween(140, 180, "140 - 179"));
		addRow(getScoresBetween(100, 140, "100 - 139"));
		addRow(getScoresBetween(80, 100, "80 - 99"));
		addRow(getScoresBetween(60, 80, "60 - 79"));
		addRow(getScoresBetween(40, 60, "40 - 59"));
		addRow(getScoresBetween(20, 40, "20 - 39"));
		addRow(getScoresBetween(0, 20, "0 - 19"));
		
		addRow(new Object[getRowWidth()]);
		
		addRow(getCheckoutPercentRow());
		
		table.setColumnWidths("120");
	}
	
	private Object[] getThreeDartAvgsRow()
	{
		Object[] threeDartAvgs = new Object[getRowWidth()];
		threeDartAvgs[0] = "3-dart avg";
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			ArrayList<HandyArrayList<Dart>> rounds = hmPlayerToDarts.get(playerName);
			HandyArrayList<Dart> darts = HandyArrayList.flattenBatches(rounds);
			
			double avg = X01Util.calculateThreeDartAverage(darts, nfSetupThreshold.getNumber());
			int p1 = (int)(100 * avg);
			avg = (double)p1/100;
			
			threeDartAvgs[i+1] = avg;
			
		}
		
		return threeDartAvgs;
	}
	
	private Object[] getCheckoutPercentRow()
	{
		Object[] row = new Object[getRowWidth()];
		row[0] = "Checkout %";
		
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			HandyArrayList<Dart> darts = getFlattenedDarts(playerName);
			
			HandyArrayList<Dart> potentialFinishers = darts.createFilteredCopy(d -> X01Util.isCheckoutDart(d));
			HandyArrayList<Dart> actualFinishes = potentialFinishers.createFilteredCopy(d -> d.isDouble() && (d.getTotal() == d.getStartingScore()));
			
			if (actualFinishes.isEmpty())
			{
				row[i+1] = "N/A";
			}
			else
			{
				int p1 = 10000 * actualFinishes.size() / potentialFinishers.size();
				double percent = (double)p1/100;
				
				row[i+1] = percent;
			}
			
			
		}
		
		return row;
	}
	
	private Object[] getScoresBetween(int min, int max, String desc)
	{
		Object[] row = new Object[getRowWidth()];
		row[0] = desc;
		
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			HandyArrayList<HandyArrayList<Dart>> rounds = getScoringRounds(playerName);
			
			HandyArrayList<HandyArrayList<Dart>> bigRounds = rounds.createFilteredCopy(r -> X01Util.sumScore(r) >= min && X01Util.sumScore(r) < max);
			
			row[i+1] = bigRounds.size();
		}
		
		return row;
	}
	
	private Object[] getScoreRow(Function<IntStream, Integer> f, String desc)
	{
		Object[] row = new Object[getRowWidth()];
		row[0] = desc;
		
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			ArrayList<HandyArrayList<Dart>> rounds = getScoringRounds(playerName);
			
			if (!rounds.isEmpty())
			{
				IntStream roundsAsTotal = rounds.stream().mapToInt(rnd -> X01Util.sumScore(rnd));
				row[i+1] = f.apply(roundsAsTotal);
			}
			else
			{
				row[i+1] = "N/A";
			}
		}
		
		return row;
	}
	
	private Object[] getMissesRow()
	{
		Object[] row = new Object[getRowWidth()];
		row[0] = "Miss %";
		
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			HandyArrayList<Dart> scoringDarts = getScoringDarts(playerName);
			HandyArrayList<Dart> misses = scoringDarts.createFilteredCopy(d -> d.getMultiplier() == 0);
			
			double percent = 100 * (double)misses.size() / scoringDarts.size();
			percent = MathsUtil.round(percent, 2);
			
			row[i+1] = percent;
		}
		
		return row;
	}
	
	private HandyArrayList<HandyArrayList<Dart>> getScoringRounds(String playerName)
	{
		HandyArrayList<HandyArrayList<Dart>> rounds = hmPlayerToDarts.get(playerName);
		return rounds.createFilteredCopy(r -> r.lastElement().getStartingScore() > nfSetupThreshold.getNumber());
	}
	
	private HandyArrayList<Dart> getScoringDarts(String playerName)
	{
		HandyArrayList<Dart> darts = getFlattenedDarts(playerName);
		return X01Util.getScoringDarts(darts, nfSetupThreshold.getNumber());
	}
	
	@Override
	protected ArrayList<Integer> getRankedRowsHighestWins()
	{
		return HandyArrayList.factoryAdd(0, 1, 2, 14, 15);
	}
	
	@Override
	protected ArrayList<Integer> getRankedRowsLowestWins()
	{
		return HandyArrayList.factoryAdd(3, 17, 18);
	}
	
	@Override
	protected ArrayList<Integer> getHistogramRows()
	{
		return HandyArrayList.factoryAdd(5, 6, 7, 8, 9, 10, 11, 12);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent arg0)
	{
		String propertyName = arg0.getPropertyName();
		if (propertyName.equals("value"))
		{
			buildTableModel();
			repaint();
		}
	}
}
