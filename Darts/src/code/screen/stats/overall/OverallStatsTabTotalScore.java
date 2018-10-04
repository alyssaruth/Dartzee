package code.screen.stats.overall;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import code.bean.GameParamFilterPanel;
import code.bean.PlayerTypeFilterPanel;
import code.bean.ScrollTableDartsGame;
import code.db.GameEntity;
import code.utils.DartsRegistry;
import code.utils.PreferenceUtil;

import java.awt.Component;
import javax.swing.Box;

public final class OverallStatsTabTotalScore extends JPanel
											 implements ActionListener
{
	private int gameType = -1;
	private GameParamFilterPanel panelGameParams = null;
	
	public OverallStatsTabTotalScore(int gameType)
	{
		super();
		
		this.gameType = gameType;
		setLayout(new BorderLayout(0, 0));
		
		panelGameParams = GameEntity.getFilterPanel(gameType);
		panelGameParams.addActionListener(this);
		panelPlayerFilters.addActionListener(this);
		
		scrollPane.setRowHeight(23);
		
		add(panelFilters, BorderLayout.NORTH);
		panelFilters.add(panelGameParams);
		
		panelFilters.add(horizontalStrut);
		panelFilters.add(panelPlayerFilters);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	private final JPanel panelFilters = new JPanel();
	private final ScrollTableDartsGame scrollPane = new ScrollTableDartsGame();
	private final PlayerTypeFilterPanel panelPlayerFilters = new PlayerTypeFilterPanel();
	private final Component horizontalStrut = Box.createHorizontalStrut(20);

	public void buildTable()
	{
		String sql = getTotalScoreSql();
		OverallStatsScreen.buildStandardLeaderboard(scrollPane, sql, "Score", false);
	}
	private String getTotalScoreSql()
	{
		int leaderboardSize = PreferenceUtil.getIntValue(DartsRegistry.PREFERENCES_INT_LEADERBOARD_SIZE);
		String gameParams = panelGameParams.getGameParams();
		String playerWhereSql = panelPlayerFilters.getWhereSql();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT p.Strategy, p.Name, pt.GameId, pt.FinalScore");
		sb.append(" FROM Participant pt, Game g, Player p");
		sb.append(" WHERE pt.GameId = g.RowId");
		sb.append(" AND pt.PlayerId = p.RowId");
		sb.append(" AND g.GameType = ");
		sb.append(gameType);
		sb.append(" AND g.GameParams = '");
		sb.append(gameParams);
		sb.append("' AND pt.FinalScore > -1");
		
		if (!playerWhereSql.isEmpty())
		{
			sb.append(" AND p.");
			sb.append(playerWhereSql);
		}
		
		sb.append(" ORDER BY pt.FinalScore ASC");
		sb.append(" FETCH FIRST ");
		sb.append(leaderboardSize);
		sb.append(" ROWS ONLY");
		
		return sb.toString();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		buildTable();
	}
}
