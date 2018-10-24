package code.screen.game;

import java.awt.BorderLayout;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JPanel;

import bean.ScrollTable;
import code.db.ParticipantEntity;
import code.object.Dart;
import code.utils.DatabaseUtil;
import object.HandyArrayList;
import object.HashMapList;
import util.Debug;

/**
 * Shows statistics for each player in a particular game.
 * Runs ad-hoc SQL to get the stats, because the full detail isn't readily available in memory (and would be messy to maintain)
 */
public abstract class GameStatisticsPanel extends JPanel
{
	protected ArrayList<ParticipantEntity> participants = null;
	protected HashMapList<String, HandyArrayList<Dart>> hmPlayerToDarts = new HashMapList<>();
	
	public GameStatisticsPanel() 
	{
		setLayout(new BorderLayout(0, 0));
		add(table, BorderLayout.CENTER);
	}
	
	protected final ScrollTable table = new ScrollTable();
	
	
	public void showStats(ArrayList<ParticipantEntity> participants)
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
	
	protected abstract void buildTableModel();
}
