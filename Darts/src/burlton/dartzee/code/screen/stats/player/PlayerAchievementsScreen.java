package burlton.dartzee.code.screen.stats.player;

import burlton.core.code.obj.HandyArrayList;
import burlton.dartzee.code.achievements.*;
import burlton.dartzee.code.bean.AchievementMedal;
import burlton.dartzee.code.db.AchievementEntity;
import burlton.dartzee.code.db.PlayerEntity;
import burlton.dartzee.code.screen.EmbeddedScreen;
import burlton.dartzee.code.screen.ScreenCache;
import burlton.desktopcore.code.bean.WrapLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public final class PlayerAchievementsScreen extends EmbeddedScreen
{
	private PlayerEntity player = null;
	
	
	public PlayerAchievementsScreen() 
	{
		JPanel centerPanel = new JPanel();
		add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		centerPanel.add(tabbedPane, BorderLayout.CENTER);

		WrapLayout fl = new WrapLayout();
		fl.setVgap(25);
		fl.setHgap(20);
		fl.setAlignment(FlowLayout.LEFT);
		panelGeneral.setLayout(fl);


		centerPanel.add(panelAchievementDesc, BorderLayout.SOUTH);
		panelAchievementDesc.setPreferredSize(new Dimension(200, 100));
		panelAchievementDesc.setBorder(new BevelBorder(5));

		JScrollPane sp = new JScrollPane();
		sp.setViewportView(panelGeneral);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("X01", null, sp, null);
	}
	
	private final JPanel panelGeneral = new JPanel();
	private final JPanel panelAchievementDesc = new JPanel();
	
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
		addAchievement(new AchievementX01HighestBust(), achievementRows);
	}
	private void addAchievement(AbstractAchievement aa, HandyArrayList<AchievementEntity> achievementRows)
	{
		int ref = aa.getAchievementRef();
		achievementRows = achievementRows.createFilteredCopy(a -> a.getAchievementRef() == ref);

		aa.initialiseFromDb(achievementRows);

		AchievementMedal medal = new AchievementMedal(aa);
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
