package code.screen.stats.overall;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import code.bean.PlayerTypeFilterPanel;
import code.bean.ScrollTableDartsGame;
import code.db.GameEntity;
import code.db.PlayerEntity;
import code.screen.EmbeddedScreen;
import code.utils.DartsRegistry;
import code.utils.DatabaseUtil;
import code.utils.PreferenceUtil;
import net.miginfocom.swing.MigLayout;
import object.HandyArrayList;
import util.DateUtil;
import util.Debug;
import util.DialogUtil;
import util.TableUtil;

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
		txt100.setEditable(false);
		txt100.setColumns(10);
		txt140.setEditable(false);
		txt140.setColumns(10);
		txt180.setEditable(false);
		txt180.setColumns(10);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("Misc", null, panel, null);
		panel.setLayout(new MigLayout("", "[][]", "[][][][][][]"));
		panel.add(lblGamesPlayed, "cell 0 0");
		txtGamesPlayed.setEditable(false);
		panel.add(txtGamesPlayed, "cell 1 0,growx");
		txtGamesPlayed.setColumns(10);
		panel.add(lblDartsThrown, "cell 0 1");
		txtDartsThrown.setEditable(false);
		panel.add(txtDartsThrown, "cell 1 1,growx");
		txtDartsThrown.setColumns(10);
		panel.add(lbls, "cell 0 3");
		panel.add(txt180, "cell 1 3,growx");
		panel.add(lbls_1, "cell 0 4");
		panel.add(txt140, "cell 1 4,growx,aligny top");
		panel.add(label, "cell 0 5");
		panel.add(txt100, "cell 1 5,growx,aligny top");
		tableTopFinishes.setRowHeight(23);
		panelX01Finishes.add(panelTopFinishesFilters, BorderLayout.NORTH);
		panelTopFinishesFilters.add(playerFilterPanelTopFinishes);
		playerFilterPanelTopFinishes.addActionListener(this);
	}

	private final JLabel lblDartsThrown = new JLabel("Darts Thrown");
	private final JTextField txtDartsThrown = new JTextField();
	private final JLabel lblGamesPlayed = new JLabel("Games Played");
	private final JTextField txtGamesPlayed = new JTextField();
	private final JLabel lbls = new JLabel("180s");
	private final JLabel lbls_1 = new JLabel("140+");
	private final JLabel label = new JLabel("100+");
	private final JTextField txt180 = new JTextField();
	private final JTextField txt140 = new JTextField();
	private final JTextField txt100 = new JTextField();
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
	public void init()
	{
		//Run SQL to get the relevant stats
		int numberOfGames = DatabaseUtil.executeQueryAggregate("SELECT COUNT(1) FROM Game");
		txtGamesPlayed.setText("" + numberOfGames);
		
		int numberOfDarts = DatabaseUtil.executeQueryAggregate("SELECT COUNT(1) FROM Dart");
		txtDartsThrown.setText("" + numberOfDarts);
		
		int oneEighties = getCountForScoringRounds(180, -1);
		txt180.setText("" + oneEighties);
		
		int oneFourties = getCountForScoringRounds(140, 180);
		txt140.setText("" + oneFourties);
		
		int oneHundreds = getCountForScoringRounds(100, 140);
		txt100.setText("" + oneHundreds);
		
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
	
	private int getCountForScoringRounds(int min, int max)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(1)");
		sb.append(" FROM Dart drtFirst, Dart drtLast, Round rnd");
		appendFirstAndLastDartSql(sb);
		sb.append(" AND ");
		sb.append(TOTAL_ROUND_SCORE_SQL_STR);
		sb.append(" >= ");
		sb.append(min);
		
		if (max > -1)
		{
			sb.append(" AND ");
			sb.append(TOTAL_ROUND_SCORE_SQL_STR);
			sb.append(" < ");
			sb.append(max);
		}
		
		return DatabaseUtil.executeQueryAggregate(sb);
	}
	
	/**
	 * Specifying Ordinal = 3 technically won't get us everything: if you scored > 100 in your first two darts but it 
	 * caused you to bust, then there won't be an ordinal 3 dart and it won't be counted. But such a minority case, I don't care.
	 * 
	 * The alternative "AND NOT EXISTS" sql took 90 seconds to run with ~300 games of data!
	 */
	private void appendFirstAndLastDartSql(StringBuilder sb)
	{
		sb.append(" WHERE drtFirst.RoundId = rnd.RowId");
		sb.append(" AND drtLast.RoundId = rnd.RowId");
		sb.append(" AND drtFirst.Ordinal = 1"); //First dart
		
		/*sb.append(" AND NOT EXISTS");
		sb.append(" (");
		sb.append(" 	SELECT 1");
		sb.append(" 	FROM Dart drtOther");
		sb.append(" 	WHERE drtOther.RoundId = rnd.RowId");
		sb.append(" 	AND drtOther.Ordinal > drtLast.Ordinal");
		sb.append(" )");*/
		sb.append(" AND drtLast.Ordinal = 3");
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
