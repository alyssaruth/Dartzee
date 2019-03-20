package burlton.dartzee.code.utils;

import burlton.core.code.util.Debug;
import burlton.dartzee.code.db.DartEntity;
import burlton.dartzee.code.db.ParticipantEntity;
import burlton.dartzee.code.db.RoundEntity;
import burlton.dartzee.code.screen.ScreenCache;
import burlton.dartzee.code.screen.game.DartsGameScreen;
import burlton.desktopcore.code.util.DialogUtil;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DevUtilities
{
	public static void purgeGame()
	{
		Long[] gameIds = getAllGameIds();
		if (gameIds == null)
		{
			DialogUtil.showError("No games to delete.");
			return;
		}
		
		Object choice = JOptionPane.showInputDialog(ScreenCache.getMainScreen(), "Select Game ID", "Delete Game", -1, null, gameIds, gameIds[0]);
		if (choice == null)
		{
			return;
		}
		
		long gameId = (long)choice;
		purgeGame(gameId);
	}
	private static Long[] getAllGameIds()
	{
		int gameCount = DatabaseUtil.executeQueryAggregate("SELECT COUNT(1) FROM Game");
		if (gameCount == 0)
		{
			return null;
		}
		
		Long[] gameIds = new Long[gameCount];
		int counter = 0;
		
		String gameIdSql = "SELECT RowId FROM Game";
		try (ResultSet rs = DatabaseUtil.executeQuery(gameIdSql))
		{
			while (rs.next())
			{
				long rowId = rs.getLong(1);
				gameIds[counter] = rowId;
				counter++;
			}
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException(gameIdSql, sqle);
		}
		
		return gameIds;
	}
	
	public static void purgeGame(String gameId)
	{
		DartsGameScreen scrn = ScreenCache.getDartsGameScreen(gameId);
		if (scrn != null)
		{
			DialogUtil.showError("Cannot delete a game that's open.");
			return;
		}
		
		String countSql = "SELECT COUNT(1) FROM Game WHERE RowId = " + gameId;
		int gameCount = DatabaseUtil.executeQueryAggregate(countSql);
		if (gameCount == 0)
		{
			DialogUtil.showError("No game exists for ID " + gameId);
			return;
		}
		
		List<ParticipantEntity> participants = new ParticipantEntity().retrieveEntities("GameId = " + gameId);
		List<RoundEntity> rounds = new RoundEntity().retrieveEntitiesWithFrom(getRoundFromSql(gameId), "rnd");
		List<DartEntity> darts = new DartEntity().retrieveEntitiesWithFrom(getDartFromSql(gameId), "d");
		
		String question = "Purge all data for Game #" + gameId + "? The following rows will be deleted:"
						+ "\n\n Participant: " + participants.size() + " rows"
						+ "\n Round: " + rounds.size() + " rows"
						+ "\n Dart: " + darts.size() + " rows";
		
		int answer = DialogUtil.showQuestion(question, false);
		if (answer == JOptionPane.YES_OPTION)
		{
			for (DartEntity dart : darts)
			{
				dart.deleteFromDatabase();
			}
			
			for (RoundEntity rnd : rounds)
			{
				rnd.deleteFromDatabase();
			}
			
			for (ParticipantEntity participant : participants)
			{
				participant.deleteFromDatabase();
			}
			
			
			String gameDeleteSql = "DELETE FROM Game WHERE RowId = " + gameId;
			DatabaseUtil.executeUpdate(gameDeleteSql);
			
			DialogUtil.showInfo("Game #" + gameId + " has been purged.");
		}
	}
	
	private static String getRoundFromSql(long gameId)
	{
		return "FROM Round rnd INNER JOIN Participant p ON ("
				   + "rnd.ParticipantId = p.RowId "
				   + "AND p.GameId = " + gameId + ")";
	}
	private static String getDartFromSql(long gameId)
	{
		return "FROM Dart d INNER JOIN Round rnd ON (d.RoundId = rnd.RowId)"
			+ "INNER JOIN Participant p ON (rnd.ParticipantId = p.RowId AND p.GameId = " + gameId + ")";
	}
}
