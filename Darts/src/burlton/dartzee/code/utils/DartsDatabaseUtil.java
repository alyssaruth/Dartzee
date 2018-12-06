package burlton.dartzee.code.utils;

import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.util.Debug;
import burlton.core.code.util.FileUtil;
import burlton.dartzee.code.achievements.AchievementX01BestFinish;
import burlton.dartzee.code.achievements.AchievementX01BestThreeDarts;
import burlton.dartzee.code.achievements.AchievementX01CheckoutCompleteness;
import burlton.dartzee.code.achievements.AchievementX01HighestBust;
import burlton.dartzee.code.db.*;
import burlton.dartzee.code.screen.PlayerMatchingDialog;
import burlton.dartzee.code.screen.ScreenCache;
import burlton.dartzee.code.screen.game.DartsGameScreen;
import burlton.desktopcore.code.util.DialogUtil;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Database helpers specific to Dartzee, e.g. first time initialisation
 */
public class DartsDatabaseUtil
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
		new AchievementX01BestFinish().runConversion(new ArrayList<>());
		new AchievementX01BestThreeDarts().runConversion(new ArrayList<>());
		new AchievementX01CheckoutCompleteness().runConversion(new ArrayList<>());
		new AchievementX01HighestBust().runConversion(new ArrayList<>());
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
