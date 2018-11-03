package code.screen.game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import bean.ScrollTable;
import code.db.ParticipantEntity;
import code.object.Dart;
import code.utils.DartsColour;
import code.utils.DatabaseUtil;
import object.HandyArrayList;
import object.HashMapList;
import util.Debug;
import util.MathsUtil;

/**
 * Shows statistics for each player in a particular game.
 * Runs ad-hoc SQL to get the stats, because the full detail isn't readily available in memory (and would be messy to maintain)
 */
public abstract class GameStatisticsPanel extends JPanel
{
	protected HandyArrayList<String> playerNamesOrdered = new HandyArrayList<>();
	protected HandyArrayList<ParticipantEntity> participants = null;
	protected HashMapList<String, HandyArrayList<Dart>> hmPlayerToDarts = new HashMapList<>();
	protected String gameParams = null;
	
	private DefaultTableModel tm = new DefaultTableModel();
	
	public GameStatisticsPanel() 
	{
		setLayout(new BorderLayout(0, 0));
		add(table, BorderLayout.CENTER);
	}
	
	protected final ScrollTable table = new ScrollTable();
	
	public void showStats(HandyArrayList<ParticipantEntity> participants)
	{
		this.participants = participants;
		
		hmPlayerToDarts = new HashMapList<>();
		
		for (ParticipantEntity participant : participants)
		{
			String playerName = participant.getPlayerName();
			
			StringBuilder sbSql = new StringBuilder();
			sbSql.append(" SELECT d.Score, d.Multiplier, d.StartingScore, d.SegmentType, rnd.RoundNumber");
			sbSql.append(" FROM Dart d, Round rnd");
			sbSql.append(" WHERE rnd.ParticipantId = " + participant.getRowId());
			sbSql.append(" AND d.RoundId = rnd.RowId");
			sbSql.append(" ORDER BY rnd.RoundNumber, d.Ordinal");
			
			try (ResultSet rs = DatabaseUtil.executeQuery(sbSql))
			{
				HandyArrayList<Dart> dartsForRound = new HandyArrayList<>();
				int currentRoundNumber = 1;
				
				while (rs.next())
				{
					int score = rs.getInt("Score");
					int multiplier = rs.getInt("Multiplier");
					int startingScore = rs.getInt("StartingScore");
					int segmentType = rs.getInt("SegmentType");
					
					Dart d = new Dart(score, multiplier);
					d.setStartingScore(startingScore);
					d.setSegmentType(segmentType);
					
					int roundNumber = rs.getInt("RoundNumber");
					if (roundNumber > currentRoundNumber)
					{
						hmPlayerToDarts.putInList(playerName, dartsForRound);
						dartsForRound = new HandyArrayList<>();
						currentRoundNumber = roundNumber;
					}
					
					//only needed for golf but doesn't hurt to always set it
					d.setGolfHole(roundNumber);
					d.setParticipantId(participant.getRowId());
					
					dartsForRound.add(d);
				}
				
				//Always add the last one
				hmPlayerToDarts.putInList(playerName, dartsForRound);
			}
			catch (SQLException sqle)
			{
				Debug.logSqlException("" + sbSql, sqle);
			}
		}
		
		buildTableModel();
	}
	
	protected void buildTableModel()
	{
		tm = new DefaultTableModel();
		tm.addColumn("");
		
		for (ParticipantEntity pt : participants)
		{
			String playerName = pt.getPlayerName();
			playerNamesOrdered.addUnique(playerName);
		}
		
		for (String playerName : playerNamesOrdered)
		{
			tm.addColumn(playerName);
		}
		
		table.setRowHeight(20);
		table.setModel(tm);
		table.disableSorting();
	
		addRowsToTable();
		
		//Rendering
		for (int i=0; i<getRowWidth(); i++)
		{
			table.getColumn(i).setCellRenderer(new ScorerRenderer());
		}
	}
	
	protected int getRowWidth()
	{
		return playerNamesOrdered.size() + 1;
	}
	
	protected void addRow(Object[] row)
	{
		tm.addRow(row);
	}
	
	protected HandyArrayList<Dart> getFlattenedDarts(String playerName)
	{
		ArrayList<HandyArrayList<Dart>> rounds = hmPlayerToDarts.get(playerName);
		return HandyArrayList.flattenBatches(rounds);
	}
	
	protected Object[] factoryRow(String rowName)
	{
		Object[] row = new Object[getRowWidth()];
		row[0] = rowName;
		return row;
	}
	
	protected Object[] getBestGameRow(Function<IntStream, OptionalInt> fn)
	{
		Object[] row = new Object[getRowWidth()];
		row[0] = "Best Game";
		
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			HandyArrayList<ParticipantEntity> playerPts = getFinishedParticipants(playerName);
			
			if (playerPts.isEmpty())
			{
				row[i+1] = "N/A";
			}
			else
			{
				IntStream scores = playerPts.stream().mapToInt(pt -> pt.getFinalScore());
				row[i+1] = fn.apply(scores).getAsInt();
			}
			
		}
		
		return row;
	}
	
	protected Object[] getAverageGameRow()
	{
		Object[] row = new Object[getRowWidth()];
		row[0] = "Avg Game";
		
		for (int i=0; i<playerNamesOrdered.size(); i++)
		{
			String playerName = playerNamesOrdered.get(i);
			
			HandyArrayList<ParticipantEntity> playerPts = getFinishedParticipants(playerName);
			if (playerPts.isEmpty())
			{
				row[i+1] = "N/A";
			}
			else
			{
				IntStream scores = playerPts.stream().mapToInt(pt -> pt.getFinalScore());
				double avg = scores.average().getAsDouble();
			
				row[i+1] = MathsUtil.round(avg, 2);
			}
		}
		
		return row;
	}
	
	private HandyArrayList<ParticipantEntity> getFinishedParticipants(String playerName)
	{
		return participants.createFilteredCopy(pt -> pt.getPlayerName().equals(playerName)
				 								  && pt.getFinalScore() > -1);
	}
	
	public void setGameParams(String gameParams)
	{
		this.gameParams = gameParams;
	}
	
	protected abstract void addRowsToTable();
	protected abstract ArrayList<Integer> getRankedRowsHighestWins();
	protected abstract ArrayList<Integer> getRankedRowsLowestWins();
	protected abstract ArrayList<Integer> getHistogramRows();
	
	private class ScorerRenderer extends DefaultTableCellRenderer
	{
        @Override
        public Component getTableCellRendererComponent(JTable table, Object
            value, boolean isSelected, boolean hasFocus, int row, int column) 
        {
    		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    		setHorizontalAlignment(SwingConstants.CENTER);
    		
    		if (column == 0)
    		{
    			setFont(new Font("Trebuchet MS", Font.BOLD, 15));
    		}
    		else
    		{
    			setFont(new Font("Trebuchet MS", Font.PLAIN, 15));
    		}
    		
    		setColours(table, row, column);
    		return this;
        }
        
        private void setColours(JTable table, int row, int column)
        {
        	if (column == 0)
        	{
        		//Do nothing
        		setForeground(null);
        		setBackground(Color.WHITE);
        		return;
        	}
        	
        	TableModel tm = table.getModel();
        	
        	if (getRankedRowsHighestWins().contains(row))
        	{
        		int pos = getPositionForColour(tm, row, column, true);
        		DartsColour.setFgAndBgColoursForPosition(this, pos);
        	}
        	else if (getRankedRowsLowestWins().contains(row))
        	{
        		int pos = getPositionForColour(tm, row, column, false);
        		DartsColour.setFgAndBgColoursForPosition(this, pos);
        	}
        	else if (getHistogramRows().contains(row))
        	{
        		long sum = getHistogramSum(tm, column);
        		
        		double thisValue = getDoubleAt(tm, row, column);
        		float percent = (sum == 0)? 0 : (float)thisValue / sum;
        		
        		Color bg = Color.getHSBColor((float)0.5, percent, 1);
        		
        		setForeground(null);
        		setBackground(bg);
        	}
        	else
        	{
        		setForeground(null);
        		setBackground(Color.WHITE);
        	}
        }
        
        private double getDoubleAt(TableModel tm, int row, int col)
        {
        	Number thisValue = (Number)tm.getValueAt(row, col);
        	
        	if (thisValue == null)
        	{
        		Debug.append("ROW: " + row + ", COL: " + col);
        		return -1;
        	}
        	
    		return thisValue.doubleValue();
        }
        
        private int getPositionForColour(TableModel tm, int row, int col, boolean highestWins)
        {
        	if (tm.getValueAt(row, col) instanceof String)
        	{
        		return -1;
        	}
        	
        	double myScore = getDoubleAt(tm, row, col);
        	
        	int myPosition = 1;
        	for (int i=1; i<tm.getColumnCount(); i++)
        	{
        		if (i == col
        		  || tm.getValueAt(row, i) instanceof String)
        		{
        			continue;
        		}
        		
        		double theirScore = getDoubleAt(tm, row, i);
        		
        		//Compare positivity to the boolean
        		int result = Double.compare(theirScore, myScore);
        		if ((result > 0) == highestWins
        		  && result != 0)
        		{
        			myPosition++;
        		}
        	}
        	
        	return myPosition;
        }
        
        private long getHistogramSum(TableModel tm, int col)
        {
        	return getHistogramRows().stream()
        							 .mapToLong(row -> ((Number)tm.getValueAt(row, col)).longValue())
        							 .sum();
        }
    }
}
