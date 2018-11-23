package code.utils;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import code.db.AbstractEntity;
import code.db.AchievementEntity;
import code.db.DartEntity;
import code.db.DartsMatchEntity;
import code.db.GameEntity;
import code.db.ParticipantEntity;
import code.db.PlayerEntity;
import code.db.PlayerImageEntity;
import code.db.RoundEntity;
import code.db.VersionEntity;
import code.screen.PlayerMatchingDialog;
import code.screen.ScreenCache;
import code.screen.game.DartsGameScreen;
import code.screen.stats.overall.OverallStatsScreen;
import object.HandyArrayList;
import util.DateUtil;
import util.Debug;
import util.DialogUtil;
import util.FileUtil;

/**
 * Database helpers specific to Dartzee, e.g. first time initialisation
 */
public class DartsDatabaseUtil implements AchievementConstants
{
	public static final int DATABASE_VERSION = 4;
	
	private static final String DATABASE_FILE_PATH_TEMP = DatabaseUtil.DATABASE_FILE_PATH + "_copying";
	
	public static void initialiseDatabase()
	{
		DialogUtil.showLoadingDialog("Checking database status...");
		
		DatabaseUtil.doDuplicateInstanceCheck();
		
		//Ensure this exists
		new VersionEntity().createTable();
		VersionEntity version = VersionEntity.retrieveCurrentDatabaseVersion();
		
		DialogUtil.dismissLoadingDialog();
		
		initialiseDatabase(version);
	}
	private static void initialiseDatabase(VersionEntity version)
	{
		if (version == null)
		{
			initDatabaseFirstTime();
			return;
		}
		
		int versionNumber = version.getVersion();
		if (versionNumber == DATABASE_VERSION)
		{
			//nothing to do
			Debug.append("Database versions match.");
			return;
		}
		
		if (versionNumber == 2)
		{
			upgradeDatabaseToVersion3();
			version.setVersion(3);
			version.saveToDatabase();
		}
		
		if (versionNumber == 3)
		{
			upgradeDatabaseToVersion4();
			version.setVersion(4);
			version.saveToDatabase();
		}
		
		initialiseDatabase(version);
	}
	
	private static void initDatabaseFirstTime()
	{
		DialogUtil.showLoadingDialog("Initialising database, please wait...");
		Debug.appendBanner("Initting database for the first time");
		
		VersionEntity versionEntity = new VersionEntity();
		versionEntity.assignRowId();
		versionEntity.setVersion(DATABASE_VERSION);
		versionEntity.saveToDatabase();
		
		Debug.append("Saved database version of " + DATABASE_VERSION);
		
		createAllTables();
		
		Debug.appendBanner("Finished initting database");
		DialogUtil.dismissLoadingDialog();
	}
	
	private static void upgradeDatabaseToVersion4()
	{
		Debug.appendBanner("Upgrading to Version 4");
		
		//Fix default on DartsMatch
		String sql = "ALTER TABLE DartsMatch ALTER COLUMN MatchParams DROP DEFAULT";
		DatabaseUtil.executeUpdate(sql);
		
		//Create Achievement table
		new AchievementEntity().createTable();
		
		//Retroactively unlock achievements
		unlockV4Achievements();
		
		Debug.appendBanner("Finished database upgrade");
	}
	
	public static void unlockV4Achievements()
	{
		unlockBestFinishAchievement(); //ACHIEVEMENT_REF_X01_BEST_FINISH
		unlockThreeDartAchievement("drtLast.DtCreation", "drtLast.Ordinal = 3", ACHIEVEMENT_REF_X01_BEST_THREE_DART_SCORE);
	}
	
	private static void unlockBestFinishAchievement()
	{
		String whereSql = "drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) = 0"
						+ " AND drtLast.Multiplier = 2";
		
		unlockThreeDartAchievement("pt.DtFinished", whereSql, ACHIEVEMENT_REF_X01_BEST_FINISH);
	}
	private static void unlockThreeDartAchievement(String dtColumn, String lastDartWhereSql, int achievementRef)
	{
		String tempTable = DatabaseUtil.createTempTable("PlayerFinishes", "PlayerId INT, GameId INT, DtAchieved TIMESTAMP, Score INT");
		if (tempTable == null)
		{
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO " + tempTable);
		sb.append(" SELECT p.RowId, pt.GameId, " + dtColumn + ", ");
		sb.append(OverallStatsScreen.TOTAL_ROUND_SCORE_SQL_STR);
		sb.append(" FROM Dart drtFirst, Dart drtLast, Round rnd, Participant pt, Player p, Game g");
		sb.append(" WHERE drtFirst.RoundId = rnd.RowId");
		sb.append(" AND drtLast.RoundId = rnd.RowId");
		sb.append(" AND drtFirst.Ordinal = 1");
		sb.append(" AND rnd.ParticipantId = pt.RowId");
		sb.append(" AND pt.PlayerId = p.RowId");
		sb.append(" AND pt.DtFinished < "); 
		sb.append(DateUtil.getEndOfTimeSqlString());
		sb.append("	AND " + lastDartWhereSql);
		sb.append(" AND pt.GameId = g.RowId");
		sb.append(" AND g.GameType = " + GameEntity.GAME_TYPE_X01);
		
		if (!DatabaseUtil.executeUpdate("" + sb))
		{
			DatabaseUtil.dropTable(tempTable);
			return;
		}
		
		sb = new StringBuilder();
		sb.append(" SELECT PlayerId, GameId, DtAchieved, Score");
		sb.append(" FROM " + tempTable + " zz1");
		sb.append(" WHERE NOT EXISTS (");
		sb.append(" 	SELECT 1");
		sb.append(" 	FROM " + tempTable + " zz2");
		sb.append(" 	WHERE zz2.PlayerId = zz1.PlayerId");
		sb.append(" 	AND (zz2.Score > zz1.Score OR (zz2.Score = zz1.Score AND zz2.GameId < zz1.GameId))");
		sb.append(" )");
		sb.append(" ORDER BY PlayerId");
		
		try (ResultSet rs = DatabaseUtil.executeQuery(sb))
		{
			while (rs.next())
			{
				long playerId = rs.getLong("PlayerId");
				long gameId = rs.getLong("GameId");
				Timestamp dtAchieved = rs.getTimestamp("DtAchieved");
				int score = rs.getInt("Score");
				
				AchievementEntity.factoryAndSave(achievementRef, playerId, gameId, score, dtAchieved);
			}
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException(sb.toString(), sqle);
		}
		finally
		{
			DatabaseUtil.dropTable(tempTable);
		}
	}
	
	private static void upgradeDatabaseToVersion3()
	{
		Debug.appendBanner("Upgrading to Version 3");
		
		//Added these columns for creating matches
		new GameEntity().addIntColumn("DartsMatchId");
		new GameEntity().addIntColumn("MatchOrdinal");
		
		//Added the match entity
		new DartsMatchEntity().createTable();
		
		//Columns shouldn't allow NULLs, so fix this
		alterColumnsToNotAllowNulls();
		
		Debug.appendBanner("Finished database upgrade");
	}
	
	private static void createAllTables()
	{
		ArrayList<AbstractEntity<?>> entities = getAllEntities();
		for (int i=0; i<entities.size(); i++)
		{
			AbstractEntity<?> entity = entities.get(i);
			entity.createTable();
		}
	}
	
	private static void alterColumnsToNotAllowNulls()
	{
		ArrayList<AbstractEntity<?>> entities = DartsDatabaseUtil.getAllEntities();
		for (AbstractEntity<?> entity : entities)
		{
			alterColumnsToNotAllowNulls(entity);
		}
	}
	private static void alterColumnsToNotAllowNulls(AbstractEntity<? extends AbstractEntity<?>> entity)
	{
		String tableName = entity.getTableName();
		ArrayList<String> columns = entity.getColumns();
		for (String column : columns)
		{
			String sql = "ALTER TABLE " + tableName + " ALTER COLUMN " + column + " NOT NULL";
			DatabaseUtil.executeUpdate(sql);
		}
	}
	
	public static ArrayList<AbstractEntity<?>> getAllEntities()
	{
		ArrayList<AbstractEntity<?>> ret = new ArrayList<>();
		ret.add(new PlayerEntity());
		ret.add(new DartEntity());
		ret.add(new GameEntity());
		ret.add(new ParticipantEntity());
		ret.add(new RoundEntity());
		ret.add(new PlayerImageEntity());
		ret.add(new DartsMatchEntity());
		ret.add(new AchievementEntity());
		
		return ret;
	}
	public static ArrayList<AbstractEntity<?>> getAllEntitiesIncludingVersion()
	{
		ArrayList<AbstractEntity<?>> ret = getAllEntities();
		ret.add(new VersionEntity());
		return ret;
	}
	
	
	/**
	 * Backup / Restore / Merge
	 */
	public static void backupCurrentDatabase()
	{
		File dbFolder = new File(DatabaseUtil.DATABASE_FILE_PATH);
		
		Debug.append("About to start DB backup");
		
		File file = FileUtil.chooseDirectory(ScreenCache.getMainScreen());
		if (file == null)
		{
			//Cancelled
			return;
		}
		
		String destinationPath = file.getAbsolutePath() + "\\Databases";
		boolean success = FileUtil.copyDirectoryRecursively(dbFolder, destinationPath);
		if (!success)
		{
			DialogUtil.showError("There was a problem creating the backup.");
		}
		
		DialogUtil.showInfo("Database successfully backed up to " + destinationPath);
	}
	
	public static void restoreDatabase()
	{
		Debug.append("About to start DB restore");
		
		if (!checkAllGamesAreClosed())
		{
			return;
		}
		
		File directoryFrom = selectAndValidateNewDatabase("restore from.");
		if (directoryFrom == null)
		{
			//Cancelled, or invalid
			return;
		}
		
		//Confirm at this point
		String confirmationQ = "Successfully conected to target database. "
		  + "\n\nAre you sure you want to restore this database? All current data will be lost.";
		int option = DialogUtil.showQuestion(confirmationQ, false);
		if (option == JOptionPane.NO_OPTION)
		{
			Debug.append("Restore cancelled.");
			return;
		}
		
		//Copy the files to a temporary file path in the application directory - Databases_copying.
		FileUtil.deleteDirectoryIfExists(new File(DATABASE_FILE_PATH_TEMP));
		boolean success = FileUtil.copyDirectoryRecursively(directoryFrom, DATABASE_FILE_PATH_TEMP);
		if (!success)
		{
			DialogUtil.showError("Restore failed - failed to copy the new database files.");
			return;
		}
		
		//Issue a shutdown command to derby so we no longer have a handle on the old files
		boolean shutdown = DatabaseUtil.shutdownDerby();
		if (!shutdown)
		{
			DialogUtil.showError("Failed to shut down current database connection, unable to restore new database.");
			return;
		}
		
		//Now switch it in
		String error = FileUtil.swapInFile(DatabaseUtil.DATABASE_FILE_PATH, DATABASE_FILE_PATH_TEMP);
		if (error != null)
		{
			Debug.stackTraceSilently("Failed to swap in new database for restore: " + error);
			DialogUtil.showError("Failed to restore database. Error: " + error);
			return;
		}
	
		DialogUtil.showInfo("Database successfully restored. Application will now exit.");
		System.exit(0);
	}
	
	private static File selectAndValidateNewDatabase(String messageSuffix)
	{
		DialogUtil.showInfo("Select the 'Databases' folder you want to " + messageSuffix);
		File directoryFrom = FileUtil.chooseDirectory(ScreenCache.getMainScreen());
		if (directoryFrom == null)
		{
			//Cancelled
			return null;
		}
		
		//Check it's named right
		String name = directoryFrom.getName();
		if (!name.equals("Databases"))
		{
			Debug.append("Aborting - selected folder invalid: " + directoryFrom);
			DialogUtil.showError("Selected path is not valid - you must select a folder named 'Databases'");
			return null;
		}
		
		//Test we can connect
		String filePath = directoryFrom.getAbsolutePath();
		boolean testSuccess = DatabaseUtil.testConnection(filePath);
		if (!testSuccess)
		{
			DialogUtil.showError("Testing conection failed for the selected database. Cannot restore from this location.");
			return null;
		}
		
		return directoryFrom;
	}
	
	private static boolean checkAllGamesAreClosed()
	{
		ArrayList<DartsGameScreen> openScreens = ScreenCache.getDartsGameScreens();
		if (!openScreens.isEmpty())
		{
			Debug.append("Aborting - there are games still open.");
			DialogUtil.showError("You must close all open games before continuing.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Merge
	 */
	public static void startMerge()
	{
		if (!checkAllGamesAreClosed())
		{
			return;
		}
		
		if (!confirmMerge())
		{
			return;
		}
		
		File directoryFrom = selectAndValidateNewDatabase("import data from.");
		String dbPath = directoryFrom.getAbsolutePath();
		
		//Phase One: Synchronise the players
		DatabaseUtil.shutdownDerby();
		HandyArrayList<PlayerEntity> importedPlayers = new PlayerEntity().retrieveEntitiesAlternateDb(dbPath);
		DatabaseUtil.shutdownDerby();
		
		PlayerMatchingDialog.matchPlayers(importedPlayers);
		
	}
	private static boolean confirmMerge()
	{
		String question = "This tool will take a database from a different instance of Dartzee and merge it into the current one.";
		question += "\n\nIt is recommended that you back up your current database before you continue. Do you want to proceed?";
		int option = DialogUtil.showQuestion(question, false);
		return option == JOptionPane.YES_OPTION;
	}
}
