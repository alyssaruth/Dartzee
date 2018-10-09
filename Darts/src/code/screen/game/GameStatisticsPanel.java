package code.screen.game;

import java.awt.BorderLayout;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JPanel;

import bean.ScrollTable;
import code.db.ParticipantEntity;
import code.object.Dart;
import code.utils.DatabaseUtil;
import object.HashMapList;
import object.SuperHashMap;
import util.Debug;

/**
 * Shows statistics for each player in a particular game.
 * Runs ad-hoc SQL to get the stats, because the full detail isn't readily available in memory (and would be messy to maintain)
 */
public abstract class GameStatisticsPanel extends JPanel
{
	protected SuperHashMap<Integer, ParticipantEntity> hmPlayerNumberToParticipant = null;
	protected HashMapList<String, Dart> hmPlayerToDarts = new HashMapList<>();
	
	public GameStatisticsPanel() 
	{
		setLayout(new BorderLayout(0, 0));
		add(table, BorderLayout.CENTER);
	}
	
	protected final ScrollTable table = new ScrollTable();
	
	
	public void showStats(long gameId, SuperHashMap<Integer, ParticipantEntity> hmPlayerNumberToParticipant)
	{
		this.hmPlayerNumberToParticipant = hmPlayerNumberToParticipant;
		
		hmPlayerToDarts = new HashMapList<>();
		
		StringBuilder sbSql = new StringBuilder();
		sbSql.append(" SELECT d.Score, d.Multiplier, d.StartingScore, rnd.RoundNumber, p.Name");
		sbSql.append(" FROM Dart d, Participant pt, Player p, Round rnd");
		sbSql.append(" WHERE pt.GameId = " + gameId);
		sbSql.append(" AND rnd.ParticipantId = pt.RowId");
		sbSql.append(" AND pt.PlayerId = p.RowId");
		sbSql.append(" AND d.RoundId = rnd.RowId");
		sbSql.append(" ORDER BY pt.Ordinal, rnd.RoundNumber, d.Ordinal");
		
		try (ResultSet rs = DatabaseUtil.executeQuery(sbSql))
		{
			while (rs.next())
			{
				int score = rs.getInt("Score");
				int multiplier = rs.getInt("Multiplier");
				int startingScore = rs.getInt("StartingScore");
				
				Dart d = new Dart(score, multiplier);
				d.setStartingScore(startingScore);
				
				String playerName = rs.getString("Name");
				
				hmPlayerToDarts.putInList(playerName, d);
			}
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException("" + sbSql, sqle);
		}
		
		buildTableModel();
	}
	
	protected abstract void buildTableModel();
}
