package burlton.dartzee.code.screen.stats.player;

import burlton.core.code.obj.HandyArrayList;
import burlton.dartzee.code.achievements.AbstractAchievement;
import burlton.dartzee.code.achievements.AchievementX01BestFinish;
import burlton.dartzee.code.achievements.AchievementX01BestThreeDarts;
import burlton.dartzee.code.achievements.AchievementX01CheckoutCompleteness;
import burlton.dartzee.code.bean.AchievementMedal;
import burlton.dartzee.code.db.AchievementEntity;
import burlton.dartzee.code.db.PlayerEntity;
import burlton.dartzee.code.screen.EmbeddedScreen;
import burlton.dartzee.code.screen.ScreenCache;

import javax.swing.*;
import java.awt.*;

public final class PlayerAchievementsScreen extends EmbeddedScreen
{
	private PlayerEntity player = null;
	
	
	public PlayerAchievementsScreen() 
	{	
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		
		tabbedPane.addTab("X01", null, panelGeneral, null);

		FlowLayout fl = new FlowLayout();
		fl.setVgap(20);
		fl.setHgap(20);
		fl.setAlignment(FlowLayout.LEFT);
		panelGeneral.setLayout(fl);
	}
	
	private final JPanel panelGeneral = new JPanel();
	
	@Override
	public String getScreenName()
	{
		return "Achievements - " + player.getName();
	}

	@Override
	public void initialise()
	{
		long playerId = player.getRowId();
		
		HandyArrayList<AchievementEntity> achievementRows = new AchievementEntity().retrieveEntities("PlayerId = " + playerId);

		addAchievement(new AchievementX01BestFinish(), achievementRows);
		addAchievement(new AchievementX01BestThreeDarts(), achievementRows);
		addAchievement(new AchievementX01CheckoutCompleteness(), achievementRows);
	}
	private void addAchievement(AbstractAchievement aa, HandyArrayList<AchievementEntity> achievementRows)
	{
		int ref = aa.getAchievementRef();
		achievementRows = achievementRows.createFilteredCopy(a -> a.getAchievementRef() == ref);

		aa.initialiseFromDb(achievementRows);

		AchievementMedal medal = new AchievementMedal(aa);
		medal.setPreferredSize(new Dimension(200, 200));
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
