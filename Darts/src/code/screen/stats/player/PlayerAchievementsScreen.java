package code.screen.stats.player;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import code.bean.AchievementMedal;
import code.db.AchievementEntity;
import code.db.PlayerEntity;
import code.screen.EmbeddedScreen;
import code.screen.ScreenCache;
import code.utils.AchievementConstants;

public final class PlayerAchievementsScreen extends EmbeddedScreen
											implements AchievementConstants
{
	private PlayerEntity player = null;
	
	
	public PlayerAchievementsScreen() 
	{	
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		
		tabbedPane.addTab("General", null, panelGeneral, null);
		
		
		FlowLayout flowLayout = (FlowLayout) panelX01.getLayout();
		flowLayout.setVgap(20);
		flowLayout.setHgap(20);
		flowLayout.setAlignment(FlowLayout.LEFT);
		tabbedPane.addTab("X01", null, panelX01, null);
			
		panelX01.add(lblNewLabel);
	}
	
	private final JPanel panelGeneral = new JPanel();
	private final JPanel panelX01 = new JPanel();
	private final JLabel lblNewLabel = new JLabel("");
	private AchievementMedal medal = new AchievementMedal();
	
	@Override
	public String getScreenName()
	{
		return "Achievements - " + player.getName();
	}

	@Override
	public void init()
	{
		long playerId = player.getRowId();
		
		AchievementEntity achievement = AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_BEST_FINISH, playerId);
		if (achievement != null)
		{
			lblNewLabel.setText("Best Finish: " + achievement.getAchievementCounter() + ", Game #" + achievement.getGameIdEarned());
		}
		else
		{
			lblNewLabel.setText("");
		}
		
		panelGeneral.remove(medal);
		medal = new AchievementMedal();
		panelGeneral.add(medal);
	}
	
	@Override
	public EmbeddedScreen getBackTarget()
	{
		return ScreenCache.getPlayerManagementScreen();
	}

	public void setPlayer(PlayerEntity player)
	{
		this.player = player;
	}
}
