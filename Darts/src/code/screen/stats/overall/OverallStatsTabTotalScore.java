package code.screen.stats.overall;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JPanel;

import bean.RadioButtonPanel;
import code.bean.GameParamFilterPanel;
import code.bean.PlayerTypeFilterPanel;
import code.bean.ScrollTableDartsGame;
import code.db.GameEntity;
import code.utils.DartsRegistry;
import code.utils.PreferenceUtil;
import javax.swing.JRadioButton;

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
		
		panelFilters.add(horizontalStrut_1);
		
		panelFilters.add(panelBestOrWorst);
		
		panelBestOrWorst.add(rdbtnBest);
		
		panelBestOrWorst.add(rdbtnWorst);
		add(scrollPane, BorderLayout.CENTER);
		
		panelBestOrWorst.addActionListener(this);
	}
	
	private final JPanel panelFilters = new JPanel();
	private final ScrollTableDartsGame scrollPane = new ScrollTableDartsGame();
	private final PlayerTypeFilterPanel panelPlayerFilters = new PlayerTypeFilterPanel();
	private final Component horizontalStrut = Box.createHorizontalStrut(20);
	private final Component horizontalStrut_1 = Box.createHorizontalStrut(20);
	private final RadioButtonPanel panelBestOrWorst = new RadioButtonPanel();
	private final JRadioButton rdbtnBest = new JRadioButton("Best");
	private final JRadioButton rdbtnWorst = new JRadioButton("Worst");

	public void buildTable()
	{
		String sql = getTotalScoreSql();
		OverallStatsScreen.buildStandardLeaderboard(scrollPane, sql, "Score", rdbtnWorst.isSelected());
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
		
		String orderStr = rdbtnBest.isSelected()?"ASC":"DESC";
		sb.append(" ORDER BY pt.FinalScore ");
		sb.append(orderStr);
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
