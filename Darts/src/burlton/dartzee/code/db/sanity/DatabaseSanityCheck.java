package burlton.dartzee.code.db.sanity;

import burlton.core.code.obj.SuperHashMap;
import burlton.core.code.util.Debug;
import burlton.core.code.util.StringUtil;
import burlton.dartzee.code.db.*;
import burlton.dartzee.code.object.DartboardSegment;
import burlton.dartzee.code.screen.ScreenCache;
import burlton.dartzee.code.utils.DartsDatabaseUtil;
import burlton.dartzee.code.utils.DatabaseUtil;
import burlton.desktopcore.code.bean.ScrollTableButton;
import burlton.desktopcore.code.screen.TableModelDialog;
import burlton.desktopcore.code.util.DateUtil;
import burlton.desktopcore.code.util.DialogUtil;
import burlton.desktopcore.code.util.TableUtil.DefaultModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseSanityCheck
{
	private static ArrayList<AbstractSanityCheckResult> sanityErrors = new ArrayList<>();
	
	public static void runSanityCheck()
	{
		Debug.appendBanner("RUNNING SANITY CHECK");
		
		sanityErrors.clear();
		
		checkForHangingOrUnsetIds();
		checkParticipantTable();
		checkForDuplicateDarts();
		checkForColumnsWithDefaults();
		checkForUnfinishedGames();
		checkFinalScore();
		checkForDuplicateMatchOrdinals();
		
		//Do this last in case any sanity checks have left rogue tables lying around
		checkForUnexpectedTables();
		
		DefaultTableModel tm = buildResultsModel();
		if (tm.getRowCount() > 0)
		{
			Action showResults = new AbstractAction()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					int modelRow = Integer.valueOf(e.getActionCommand());
					
					AbstractSanityCheckResult result = sanityErrors.get(modelRow);
					showResultsBreakdown(result);
				}
			};
			
			Action autoFix = new AbstractAction()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					int modelRow = Integer.valueOf(e.getActionCommand());
					
					AbstractSanityCheckResult result = sanityErrors.get(modelRow);
					result.autoFix();
				}
			};
			
			SuperHashMap<Integer, Action> hmColumnToAction = new SuperHashMap<>();
			hmColumnToAction.put(2, showResults);
			hmColumnToAction.put(3, autoFix);
			
			TableModelDialog dlg = new TableModelDialog("Sanity Results", new ScrollTableButton(hmColumnToAction, tm));
			dlg.setColumnWidths("-1;50;150;150");
			dlg.setLocationRelativeTo(ScreenCache.getMainScreen());
			dlg.setVisible(true);
		}
		else
		{
			DialogUtil.showInfo("Sanity check completed and found no issues");
		}
	}
	private static DefaultTableModel buildResultsModel()
	{
		DefaultModel model = new DefaultModel();
		model.addColumn("Description");
		model.addColumn("Count");
		model.addColumn("");
		model.addColumn("");
		
		for (AbstractSanityCheckResult result : sanityErrors)
		{
			Object[] row = {result.getDescription(), result.getCount(), "View Results >", "Auto-fix"};
			model.addRow(row);
		}
		
		return model;
	}
	
	private static void showResultsBreakdown(AbstractSanityCheckResult result)
	{
		TableModelDialog dlg = result.getResultsDialog();
		dlg.setSize(800, 600);
		dlg.setLocationRelativeTo(ScreenCache.getMainScreen());
		dlg.setVisible(true);
	}
	
	private static void checkForHangingOrUnsetIds()
	{
		ArrayList<AbstractEntity<?>> entities = DartsDatabaseUtil.getAllEntities();
		for (AbstractEntity<?> entity : entities)
		{
			checkForHangingOrUnsetIds(entity);
		}
	}
	private static void checkForHangingOrUnsetIds(AbstractEntity<?> entity)
	{
		ArrayList<String> columns = entity.getColumns();
		for (String column : columns)
		{
			if (column.equals("RowId")
			  || !column.endsWith("Id"))
			{
				continue;
			}
			
			//Always look for hanging values. Only perform the 'unset' check for columns that should always be set.
			checkForHangingValues(entity, column);
			if (!entity.columnCanBeUnset(column))
			{
				checkForUnsetValues(entity, column);
			}
		}
	}
	
	private static void checkForHangingValues(AbstractEntity<?> entity, String idColumn)
	{
		if (idColumn.equals("DartzeeRuleId"))
		{
			//Temporary measure until I've created this table!
			return;
		}
		
		String referencedTable = idColumn.substring(0, idColumn.length() - 2);
		
		StringBuilder sb = new StringBuilder();
		sb.append(idColumn);
		sb.append(" > -1 ");
		sb.append("AND NOT EXISTS (");
		sb.append("SELECT 1 FROM ");
		sb.append(referencedTable);
		sb.append(" ref WHERE e.");
		sb.append(idColumn);
		sb.append(" = ref.RowId)");
		
		String whereSql = sb.toString();
		ArrayList<? extends AbstractEntity<?>> entities = entity.retrieveEntities(whereSql, "e");
		
		int count = entities.size();
		if (count > 0)
		{
			sanityErrors.add(new SanityCheckResultHangingEntities(idColumn, entities));
		}
	}
	private static void checkForUnsetValues(AbstractEntity<?> entity, String idColumn)
	{
		String whereSql = idColumn + " = -1";
		ArrayList<? extends AbstractEntity<?>> entities = entity.retrieveEntities(whereSql);
		
		int count = entities.size();
		if (count > 0)
		{
			sanityErrors.add(new SanityCheckResultUnsetColumns(idColumn, entities));
		}
	}
	
	private static void checkParticipantTable()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(" DtFinished < ");
		sb.append(DateUtil.getEndOfTimeSqlString());
		sb.append(" AND FinalScore = -1");
		
		String whereSql = sb.toString();
		ArrayList<ParticipantEntity> participants = new ParticipantEntity().retrieveEntities(whereSql);
		
		int count = participants.size();
		if (count > 0)
		{
			sanityErrors.add(new SanityCheckResultEntitiesSimple(participants, "Participants marked as finished but with no final score"));
		}
	}
	
	private static void checkForDuplicateDarts()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(" EXISTS (");
		sb.append(" SELECT 1");
		sb.append(" FROM Dart drt2");
		sb.append(" WHERE drt.Ordinal = drt2.Ordinal");
		sb.append(" AND drt.RoundId = drt2.RoundId");
		sb.append(" AND drt.RowId > drt2.RowId");
		sb.append(")");
		
		String whereSql = sb.toString();
		ArrayList<DartEntity> darts = new DartEntity().retrieveEntities(whereSql, "drt");
		int count = darts.size();
		if (count > 0)
		{
			sanityErrors.add(new SanityCheckResultEntitiesSimple(darts, "Duplicate darts"));
		}
	}
	
	private static void checkForColumnsWithDefaults()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT t.TableName, c.ColumnName ");
		sb.append("FROM sys.systables t, sys.syscolumns c ");
		sb.append("WHERE c.ReferenceId = t.TableId ");
		sb.append("AND t.TableType = 'T' ");
		sb.append("AND c.ColumnDefault IS NOT NULL");
		
		DefaultModel model = new DefaultModel();
		model.addColumn("TableName");
		model.addColumn("ColumnName");
		try (ResultSet rs = DatabaseUtil.executeQuery(sb))
		{
			while (rs.next())
			{
				String tableName = rs.getString("TableName");
				String columnName = rs.getString("ColumnName");
				
				String[] row = {tableName, columnName};
				model.addRow(row);
			}
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException(sb.toString(), sqle);
		}
		
		if (model.getRowCount() > 0)
		{
			sanityErrors.add(new SanityCheckResultSimpleTableModel(model, "Columns that allow defaults"));
		}
	}
	
	/**
	 * Primarily to spot temp tables that haven't been tidied up
	 */
	private static void checkForUnexpectedTables()
	{
		ArrayList<AbstractEntity<?>> entities = DartsDatabaseUtil.getAllEntitiesIncludingVersion();
		ArrayList<String> tableNames = AbstractEntity.makeFromEntityFields(entities, "TableNameUpperCase");
		String tableInSql = StringUtil.toSqlInStatement(tableNames, false);
		
		DefaultModel tm = new DefaultModel();
		tm.addColumn("Schema");
		tm.addColumn("TableName");
		tm.addColumn("TableId");
		
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT s.SchemaName, t.TableName, t.TableId");
		sb.append(" FROM sys.systables t, sys.sysschemas s");
		sb.append(" WHERE t.SchemaId = s.SchemaId");
		sb.append(" AND t.TableType = 'T'");
		sb.append(" AND t.TableName ");
		sb.append(tableInSql);

		try (ResultSet rs = DatabaseUtil.executeQuery(sb))
		{
			while (rs.next())
			{
				String schema = rs.getString("SchemaName");
				String tableName = rs.getString("TableName");
				String tableId = rs.getString("TableId");
				
				if (!tableNames.contains(tableName))
				{
					String[] row = {schema, tableName, tableId};
					tm.addRow(row);
				}
			}
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException("" + sb, sqle);
		}
		
		if (tm.getRowCount() > 0)
		{
			sanityErrors.add(new SanityCheckResultUnexpectedTables(tm));
		}
	}
	
	/**
	 * Look for games where DtFinished = EOT, but all the participants have finished.
	 */
	private static void checkForUnfinishedGames()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("DtFinish = ");
		sb.append(DateUtil.getEndOfTimeSqlString());
		sb.append(" AND NOT EXISTS ");
		sb.append(" (");
		sb.append(" 	SELECT 1");
		sb.append(" 	FROM Participant pt");
		sb.append(" 	WHERE pt.GameId = g.RowId");
		sb.append(" 	AND pt.DtFinished = ");
		sb.append(DateUtil.getEndOfTimeSqlString());
		sb.append(" )");
		
		String whereSql = sb.toString();
		ArrayList<GameEntity> games = new GameEntity().retrieveEntities(whereSql, "g");
		if (games.size() > 0)
		{
			sanityErrors.add(new SanityCheckResultEntitiesSimple(games, "Unfinished games without active players"));
		}
	}
	
	/**
	 * Actually got the potential for this. When loading an unfinished match, the ordinal got reset to 0. Oops...
	 */
	private static void checkForDuplicateMatchOrdinals()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(" g.MatchOrdinal > -1");
		sb.append(" AND EXISTS (");
		sb.append(" SELECT 1");
		sb.append(" FROM Game g2");
		sb.append(" WHERE g2.DartsMatchId = g.DartsMatchId");
		sb.append(" AND g2.MatchOrdinal = g.MatchOrdinal");
		sb.append(" AND g2.RowId > g.RowId");
		sb.append(")");
		
		String whereSql = sb.toString();
		ArrayList<GameEntity> games = new GameEntity().retrieveEntities(whereSql, "g");
		int count = games.size();
		if (count > 0)
		{
			sanityErrors.add(new SanityCheckResultDuplicateMatchOrdinals(games, "Games with duplicate MatchOrdinals"));
		}
	}
	
	/**
	 * The FinalScore column on Participant is basically denormalised data. Sense-check it against the raw data here.
	 */
	private static void checkFinalScore()
	{
		checkFinalScoreX01();
		checkFinalScoreGolf();
		checkFinalScoreRoundTheClock();
	}
	
	/**
	 * Should be (totalRounds - 1) * 3 + (# darts in final round)
	 */
	private static void checkFinalScoreX01()
	{
		String tempTable1 = DatabaseUtil.createTempTable("ParticipantToRoundCount", "ParticipantId INT, RoundCount INT, FinalRoundNumber INT");
		if (tempTable1 == null)
		{
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(tempTable1);
		sb.append(" SELECT pt.RowId, COUNT(1), MAX(rnd.RoundNumber)");
		sb.append(" FROM Round rnd, Participant pt, Game g");
		sb.append(" WHERE rnd.ParticipantId = pt.RowId");
		sb.append(" AND pt.GameId = g.RowId");
		sb.append(" AND g.GameType = ");
		sb.append(GameEntityKt.GAME_TYPE_X01);
		sb.append(" AND pt.FinalScore > -1");
		sb.append(" GROUP BY pt.RowId");
		
		String sql = sb.toString();
		boolean success = DatabaseUtil.executeUpdate(sql);
		if (!success)
		{
			return;
		}
		
		String tempTable2 = DatabaseUtil.createTempTable("ParticipantToX01Score", "ParticipantId INT, FinalScoreCalculated INT");
		if (tempTable2 == null)
		{
			return;
		}
		
		sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(tempTable2);
		sb.append(" SELECT zz.ParticipantId, 3*(zz.RoundCount - 1) + COUNT(1)");
		sb.append(" FROM Round rnd, Dart drt, ");
		sb.append(tempTable1);
		sb.append(" zz");
		sb.append(" WHERE zz.ParticipantId = rnd.ParticipantId");
		sb.append(" AND zz.FinalRoundNumber = rnd.RoundNumber");
		sb.append(" AND drt.RoundId = rnd.RowId");
		sb.append(" GROUP BY zz.ParticipantId, zz.RoundCount");
	
		sql = sb.toString();
		
		try
		{
			DatabaseUtil.executeUpdate(sql);
			createFinalScoreSanityCheck(tempTable2, GameEntityKt.GAME_TYPE_X01);
		}
		finally
		{
			DatabaseUtil.dropTable(tempTable1);
		}
	}
	
	/**
	 * 
	 */
	private static void checkFinalScoreGolf()
	{
		String tempTable = DatabaseUtil.createTempTable("ParticipantToGolfScore", "ParticipantId INT, FinalScoreCalculated INT");
		if (tempTable == null)
		{
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(" INSERT INTO " + tempTable);
		sb.append(" SELECT pt.RowId, SUM(");
		sb.append(" 	CASE");
		sb.append(" 		WHEN drt.Score <> rnd.RoundNumber THEN 5");
		sb.append(" 		WHEN drt.SegmentType = " + DartboardSegment.TYPE_DOUBLE + " THEN 1");
		sb.append(" 		WHEN drt.SegmentType = " + DartboardSegment.TYPE_TREBLE + " THEN 2");
		sb.append(" 		WHEN drt.SegmentType = " + DartboardSegment.TYPE_INNER_SINGLE + " THEN 3");
		sb.append(" 		WHEN drt.SegmentType = " + DartboardSegment.TYPE_OUTER_SINGLE + " THEN 4");
		sb.append(" 		ELSE 5");
		sb.append(" 	END");
		sb.append(" )");
		sb.append(" FROM Dart drt, Round rnd, Participant pt, Game g");
		sb.append(" WHERE drt.RoundId = rnd.RowId");
		sb.append(" AND rnd.ParticipantId = pt.RowId");
		sb.append(" AND pt.GameId = g.RowId");
		sb.append(" AND g.GameType = " + GameEntityKt.GAME_TYPE_GOLF);
		sb.append(" AND pt.FinalScore > -1");
		sb.append(" AND NOT EXISTS");
		sb.append(" (");
		sb.append(" 	SELECT 1");
		sb.append(" 	FROM Dart drt2");
		sb.append(" 	WHERE drt.RoundId = drt2.RoundId");
		sb.append(" 	AND drt2.Ordinal > drt.Ordinal");
		sb.append(" )");
		sb.append(" GROUP BY pt.RowID, pt.FinalScore");
		
		String sql = sb.toString();
		
		
		boolean success	= DatabaseUtil.executeUpdate(sql);
		if (!success)
		{
			DatabaseUtil.dropTable(tempTable);
			return;
			
		}
		
		createFinalScoreSanityCheck(tempTable, GameEntityKt.GAME_TYPE_GOLF);
	}
	
	/**
	 * Should just equal the number of darts
	 */
	private static void checkFinalScoreRoundTheClock()
	{
		String tempTable = DatabaseUtil.createTempTable("ParticipantToRoundTheClockScore", "ParticipantId INT, FinalScoreCalculated INT");
		if (tempTable == null)
		{
			//Something went wrong
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(tempTable);
		sb.append(" SELECT pt.RowId, COUNT(1)");
		sb.append(" FROM Game g, Participant pt, Round rnd, Dart drt");
		sb.append(" WHERE drt.RoundId = rnd.RowId");
		sb.append(" AND rnd.ParticipantId = pt.RowId");
		sb.append(" AND pt.GameId = g.RowId");
		sb.append(" AND g.GameType = ");
		sb.append(GameEntityKt.GAME_TYPE_ROUND_THE_CLOCK);
		sb.append(" AND pt.FinalScore > -1");
		sb.append(" GROUP BY pt.RowId");
		
		String sql = sb.toString();
		boolean success = DatabaseUtil.executeUpdate(sql);
		if (!success)
		{
			return;
		}
		
		createFinalScoreSanityCheck(tempTable, GameEntityKt.GAME_TYPE_ROUND_THE_CLOCK);
	}
	private static void createFinalScoreSanityCheck(String tempTable, int gameType)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT pt.*, zz.FinalScoreCalculated");
		sb.append(" FROM Participant pt, ");
		sb.append(tempTable);
		sb.append(" zz WHERE pt.RowId = zz.ParticipantId");
		sb.append(" AND pt.FinalScore > -1");
		sb.append(" AND pt.FinalScore <> zz.FinalScoreCalculated");
		
		SuperHashMap<ParticipantEntity, Integer> hmParticipantToActualCount = new SuperHashMap<>();
		try (ResultSet rs = DatabaseUtil.executeQuery(sb))
		{
			while (rs.next())
			{
				ParticipantEntity pt = new ParticipantEntity().factoryFromResultSet(rs);
				int dartCount = rs.getInt("FinalScoreCalculated");
				
				hmParticipantToActualCount.put(pt, dartCount);
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
		
		//Add the sanity error
		if (hmParticipantToActualCount.size() > 0)
		{
			sanityErrors.add(new SanityCheckResultFinalScoreMismatch(gameType, hmParticipantToActualCount));
		}
	}
}
