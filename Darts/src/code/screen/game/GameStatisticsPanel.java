package code.screen.game;

import java.awt.BorderLayout;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import bean.ScrollTable;
import code.db.ParticipantEntity;
import code.object.Dart;
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
			sbSql.append(" SELECT d.Score, d.Multiplier, d.StartingScore, rnd.RoundNumber");
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
					
					Dart d = new Dart(score, multiplier);
					d.setStartingScore(startingScore);
					
					int roundNumber = rs.getInt("RoundNumber");
					if (roundNumber > currentRoundNumber)
					{
						hmPlayerToDarts.putInList(playerName, dartsForRound);
						dartsForRound = new HandyArrayList<>();
						currentRoundNumber = roundNumber;
					}
					
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
	
	private void buildTableModel()
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
	
	protected abstract void addRowsToTable();
}
