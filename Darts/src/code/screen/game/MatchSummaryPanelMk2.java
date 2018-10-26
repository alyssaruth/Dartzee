package code.screen.game;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

import code.db.DartsMatchEntity;
import code.db.GameEntity;
import code.db.ParticipantEntity;
import code.db.PlayerEntity;
import object.HandyArrayList;
import object.SuperHashMap;

/**
 * The first tab displayed for any match. Provides a summary of the players' overall scores with (hopefully) nice graphs and stuff
 */
public class MatchSummaryPanelMk2 extends PanelWithScorers<MatchScorer>
								  implements ActionListener
{
	public MatchSummaryPanelMk2() {
	}
	private SuperHashMap<Long, MatchScorer> hmPlayerIdToScorer = new SuperHashMap<>();
	private HandyArrayList<ParticipantEntity> participants = new HandyArrayList<>();
	private DartsMatchEntity match = null;
	
	private GameStatisticsPanel statsPanel;
	
	public void init(DartsMatchEntity match)
	{
		this.match = match;
		
		statsPanel = factoryStatsPanel();
		if (statsPanel != null)
		{
			panelCenter.add(statsPanel, BorderLayout.CENTER);
			panelCenter.add(refreshPanel, BorderLayout.SOUTH);
			
			refreshPanel.add(btnRefresh);
			btnRefresh.addActionListener(this);
		}
		
		HandyArrayList<PlayerEntity> players = match.getPlayers();
		
		int totalPlayers = players.size();
		initScorers(totalPlayers);
		
		for (PlayerEntity player : players)
		{
			long playerId = player.getRowId();
			MatchScorer scorer = assignScorer(player, hmPlayerIdToScorer, playerId, "");
			scorer.setMatch(match);
		}
	}
	
	private final JPanel refreshPanel = new JPanel();
	private final JButton btnRefresh = new JButton("Refresh Stats");
	
	public void addParticipant(long gameId, ParticipantEntity participant)
	{
		long playerId = participant.getPlayerId();
		MatchScorer scorer = hmPlayerIdToScorer.get(playerId);
		
		Object[] row = {gameId, participant, participant, participant};
		scorer.addRow(row);
		
		participants.add(participant);
	}
	public void updateTotalScores()
	{
		ArrayList<MatchScorer> scorers = hmPlayerIdToScorer.getValuesAsVector();
		for (MatchScorer scorer : scorers)
		{
			scorer.updateResult();
		}
		
		updateStats();
	}
	public void updateStats()
	{
		statsPanel.showStats(participants);
	}

	@Override
	public MatchScorer factoryScorer()
	{
		return new MatchScorer();
	}
	
	private GameStatisticsPanel factoryStatsPanel()
	{
		int type = match.getGameType();
		if (type == GameEntity.GAME_TYPE_X01)
		{
			return new MatchStatisticsPanelX01();
		}
		
		return null;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		updateStats();
	}
}
