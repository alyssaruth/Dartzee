package burlton.dartzee.code.screen.stats.overall;
import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.util.Debug;
import burlton.dartzee.code.bean.PlayerTypeFilterPanel;
import burlton.dartzee.code.bean.ScrollTableDartsGame;
import burlton.dartzee.code.db.GameEntity;
import burlton.dartzee.code.db.PlayerEntity;
import burlton.dartzee.code.screen.EmbeddedScreen;
import burlton.dartzee.code.utils.DartsRegistry;
import burlton.dartzee.code.utils.DatabaseUtil;
import burlton.dartzee.code.utils.PreferenceUtil;
import burlton.desktopcore.code.util.DateUtil;
import burlton.desktopcore.code.util.DialogUtil;
import burlton.desktopcore.code.util.TableUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class OverallStatsScreen extends EmbeddedScreen
{
	public static final String TOTAL_ROUND_SCORE_SQL_STR = "(drtFirst.StartingScore - drtLast.StartingScore) + (drtLast.score * drtLast.multiplier)";
	
	public OverallStatsScreen() 
	{	
		add(tabbedPane, BorderLayout.CENTER);
		
		addTotalScoreTabs();
		
		tabbedPane.addTab("X01 Finishes", null, panelX01Finishes, null);
		panelX01Finishes.setLayout(new BorderLayout(0, 0));
		
		panelX01Finishes.add(tableTopFinishes);
		tableTopFinishes.setRowHeight(23);
		panelX01Finishes.add(panelTopFinishesFilters, BorderLayout.NORTH);
		panelTopFinishesFilters.add(playerFilterPanelTopFinishes);
		playerFilterPanelTopFinishes.addActionListener(this);
	}

	private final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
	private final JPanel panelX01Finishes = new JPanel();
	private final ScrollTableDartsGame tableTopFinishes = new ScrollTableDartsGame();
	private final PlayerTypeFilterPanel playerFilterPanelTopFinishes = new PlayerTypeFilterPanel();
	private final JPanel panelTopFinishesFilters = new JPanel();

	/**
	 * The total score tabs are added dynamically, so that adding a new game type will automatically update the leaderboard
	 */
	private void addTotalScoreTabs()
	{
		HandyArrayList<Integer> gameTypes = GameEntity.getAllGameTypes();
		for (int gameType : gameTypes)
		{
			String tabTitle = GameEntity.getTypeDesc(gameType);
			tabbedPane.addTab(tabTitle, null, new OverallStatsTabTotalScore(gameType), null);
		}
	}
	
	@Override
	public String getScreenName()
	{
		return "Game Statistics";
	}

	@Override
	public void initialise()
	{
		int tabCount = tabbedPane.getTabCount();
		for (int i=0; i<tabCount; i++)
		{
			Component tab = tabbedPane.getComponentAt(i);
			if (tab instanceof OverallStatsTabTotalScore)
			{
				OverallStatsTabTotalScore totalScoreTab = (OverallStatsTabTotalScore)tab;
				totalScoreTab.buildTable();
			}
		}
		
		buildTopFinishesTable();
	}
	
	/**
	 * Build a standard leaderboard table, which contains the flag, name, Game ID and a custom 'score' column.
	 */
	public static void buildStandardLeaderboard(ScrollTableDartsGame table, String sql, String scoreColumnName, boolean desc)
	{
		TableUtil.DefaultModel model = new TableUtil.DefaultModel();
		model.addColumn("");
		model.addColumn("Player");
		model.addColumn("Game");
		model.addColumn(scoreColumnName);
		
		ArrayList<Object[]> rows = retrieveDatabaseRowsForLeaderboard(sql);
		for (Object[] row : rows)
		{
			model.addRow(row);
		}
		
		table.setModel(model);
		table.setColumnWidths("25");
		table.sortBy(3, desc);
	}
	private static ArrayList<Object[]> retrieveDatabaseRowsForLeaderboard(String sqlStr)
	{
		ArrayList<Object[]> rows = new ArrayList<>();
		
		try (ResultSet rs = DatabaseUtil.executeQuery(sqlStr))
		{		
			while (rs.next())
			{
				int strategy = rs.getInt(1);
				String playerName = rs.getString(2);
				long gameId = rs.getLong(3);
				int score = rs.getInt(4);
				
				ImageIcon playerFlag = PlayerEntity.getPlayerFlag(strategy == -1);
				
				Object[] row = {playerFlag, playerName, gameId, score};
				rows.add(row);
			}
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException(sqlStr, sqle);
			DialogUtil.showError("Failed to build finishes leaderboard.");
		}
		
		return rows;
	}
	
	
	private void buildTopFinishesTable()
	{
		String sql = getTopX01FinishSql();
		
		buildStandardLeaderboard(tableTopFinishes, sql, "Finish", true);
		tableTopFinishes.setRowName("finish", "finishes");
	}
	
	
	/**
	 * N.B. It is *wrong* to specify that drtLast.Ordinal = 3, as the finish might have been accomplished in two darts.
	 * 
	 * This clause isn't actually needed, because we enforce that drtLast is a double and that it's score subtracted from the 
	 * starting score is 0, so it must be a finish dart (and therefore the last).
	 */
	private String getTopX01FinishSql()
	{
		int leaderboardSize = PreferenceUtil.getIntValue(DartsRegistry.PREFERENCES_INT_LEADERBOARD_SIZE);
		String extraWhereSql = playerFilterPanelTopFinishes.getWhereSql();
		
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT p.Strategy, p.Name, pt.GameId, ");
		sb.append(TOTAL_ROUND_SCORE_SQL_STR);
		sb.append(" FROM Dart drtFirst, Dart drtLast, Round rnd, Participant pt, Player p");
		sb.append(" WHERE drtFirst.RoundId = rnd.RowId");
		sb.append(" AND drtLast.RoundId = rnd.RowId");
		sb.append(" AND drtFirst.Ordinal = 1");
		sb.append(" AND rnd.ParticipantId = pt.RowId");
		sb.append(" AND pt.PlayerId = p.RowId");
		sb.append(" AND pt.DtFinished < "); 
		sb.append(DateUtil.getEndOfTimeSqlString());
		sb.append("	AND drtLast.StartingScore - (drtLast.Multiplier * drtLast.Score) = 0");
		sb.append(" AND drtLast.Multiplier = 2");
		
		if (!extraWhereSql.isEmpty())
		{
			sb.append(" AND p.");
			sb.append(extraWhereSql);
		}
		
		sb.append(" ORDER BY ");
		sb.append(TOTAL_ROUND_SCORE_SQL_STR);
		sb.append(" DESC");
		sb.append(" FETCH FIRST ");
		sb.append(leaderboardSize);
		sb.append(" ROWS ONLY");
		
		return sb.toString();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (playerFilterPanelTopFinishes.isEventSource(arg0))
		{
			buildTopFinishesTable();
		}
		else
		{
			super.actionPerformed(arg0);
		}
	}
	
}
