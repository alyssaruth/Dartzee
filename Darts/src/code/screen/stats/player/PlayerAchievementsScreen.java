package code.screen.stats.player;

import java.awt.BorderLayout;

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
import javafx.scene.paint.Color;

public final class PlayerAchievementsScreen extends EmbeddedScreen
											implements AchievementConstants
{
	private PlayerEntity player = null;
	
	
	public PlayerAchievementsScreen() 
	{	
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		
		tabbedPane.addTab("General", null, panelGeneral, null);
		
		panelGeneral.add(gray);
		panelGeneral.add(red);
		panelGeneral.add(orange);
		panelGeneral.add(yellow);
		panelGeneral.add(green);
		panelGeneral.add(cyan);
		panelGeneral.add(hotpink);
		
		
		//FlowLayout flowLayout = (FlowLayout) panelX01.getLayout();
		//flowLayout.setVgap(20);
		//flowLayout.setHgap(20);
		//flowLayout.setAlignment(FlowLayout.LEFT);
		//tabbedPane.addTab("X01", null, panelX01, null);
			
		//panelX01.add(lblNewLabel);
	}
	
	private final JPanel panelGeneral = new JPanel();
	private final AchievementMedal gray = new AchievementMedal(15, Color.GRAY);
	private final AchievementMedal red = new AchievementMedal(45, Color.RED);
	private final AchievementMedal orange = new AchievementMedal(85, Color.ORANGE);
	private final AchievementMedal yellow = new AchievementMedal(180, Color.YELLOW);
	private final AchievementMedal green = new AchievementMedal(210, Color.LIGHTGREEN);
	private final AchievementMedal cyan = new AchievementMedal(266, Color.CYAN);
	private final AchievementMedal hotpink = new AchievementMedal(360, Color.DEEPPINK);
	private final JLabel lblNewLabel = new JLabel("");
	
	@Override
	public String getScreenName()
	{
		return "Achievements - " + player.getName();
	}

	@Override
	public void init()
	{
		long playerId = player.getRowId();
		
		AchievementEntity achievement = AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_X01_BEST_FINISH, playerId);
		if (achievement != null)
		{
			lblNewLabel.setText("Best Finish: " + achievement.getAchievementCounter() + ", Game #" + achievement.getGameIdEarned());
		}
		else
		{
			lblNewLabel.setText("");
		}
	}
	
	/*@Override
	public void postInit()
	{
		HandyArrayList<AchievementMedal> medals = ComponentUtil.getAllChildComponentsForType(this, AchievementMedal.class);
		for (AchievementMedal medal : medals)
		{
			medal.animateProgressBar();
		}
	}*/
	
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
