package burlton.dartzee.code.screen;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;

import burlton.dartzee.code.screen.preference.PreferencesDialog;
import burlton.dartzee.code.screen.reporting.ReportingSetupScreen;
import burlton.dartzee.code.screen.stats.overall.OverallStatsScreen;
import burlton.desktopcore.code.screen.AbstractAboutDialog;
import burlton.desktopcore.code.screen.BugReportDialog;
import burlton.desktopcore.code.util.ComponentUtil;
import java.awt.Font;

public class MenuScreen extends EmbeddedScreen
{
	public MenuScreen()
	{
		super();
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		btnNewGame.setFont(new Font("Tahoma", Font.PLAIN, 18));
		
		btnNewGame.setBounds(145, 40, 150, 50);
		panel.add(btnNewGame);
		btnManagePlayers.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnManagePlayers.setBounds(60, 140, 150, 50);
		panel.add(btnManagePlayers);
		btnGameStats.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnGameStats.setBounds(35, 240, 150, 50);
		panel.add(btnGameStats);
		btnPreferences.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnPreferences.setBounds(505, 40, 150, 50);
		panel.add(btnPreferences);
		btnAbout.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnAbout.setBounds(590, 140, 150, 50);
		panel.add(btnAbout);
		btnBugReport.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnBugReport.setBounds(615, 240, 150, 50);
		panel.add(btnBugReport);
		btnExit.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnExit.setBounds(325, 465, 150, 50);
		panel.add(btnExit);
		btnGameReport.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnGameReport.setBounds(60, 340, 150, 50);
		panel.add(btnGameReport);
		btnUtilities.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnUtilities.setBounds(590, 340, 150, 50);
		panel.add(btnUtilities);
		
		menuDartboard.setBounds(200, 65, 400, 400);
		menuDartboard.paintDartboard(null, false);
		panel.add(menuDartboard);
		
		//Add ActionListeners
		ArrayList<AbstractButton> buttons = ComponentUtil.getAllChildComponentsForType(this, AbstractButton.class);
		for (AbstractButton button : buttons)
		{
			button.addActionListener(this);
		}
	}
	
	private final Dartboard menuDartboard = new Dartboard(400, 400);
	
	private final JButton btnNewGame = new JButton("New Game");
	private final JButton btnManagePlayers = new JButton("Manage Players");
	private final JButton btnGameStats = new JButton("Game Stats");
	private final JButton btnPreferences = new JButton("Preferences");
	private final JButton btnAbout = new JButton("About...");
	private final JButton btnBugReport = new JButton("Bug Report...");
	private final JButton btnUtilities = new JButton("Utilities");
	private final JButton btnExit = new JButton("Exit");
	private final JButton btnGameReport = new JButton("Game Report");
	
	@Override
	public String getScreenName()
	{
		return "Menu";
	}

	@Override
	public void initialise()
	{
		//Do nothing
	}
	
	@Override
	public boolean showBackButton()
	{
		//This is the root screen!
		return false;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		AbstractButton src = (AbstractButton)arg0.getSource();
		if (src == btnAbout)
		{
			AbstractAboutDialog dialog = ScreenCache.getAboutDialog();
			dialog.setLocationRelativeTo(this);
			dialog.setModal(true);
			dialog.setVisible(true);
		}
		else if (src == btnBugReport)
		{
			BugReportDialog dialog = ScreenCache.getBugReportDialog();
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
		}
		else if (src == btnPreferences)
		{
			PreferencesDialog dialog = ScreenCache.getPreferencesDialog();
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
		}
		else if (src == btnExit)
		{
			ScreenCache.exitApplication();
		}
		else if (src == btnNewGame)
		{
			GameSetupScreen scrn = ScreenCache.getScreen(GameSetupScreen.class);
			ScreenCache.switchScreen(scrn);
		}
		else if (src == btnManagePlayers)
		{
			PlayerManagementScreen playerManagement = ScreenCache.getScreen(PlayerManagementScreen.class);
			ScreenCache.switchScreen(playerManagement);
		}
		else if (src == btnGameReport)
		{
			ReportingSetupScreen rss = ScreenCache.getScreen(ReportingSetupScreen.class);
			ScreenCache.switchScreen(rss);
		}
		else if (src == btnGameStats)
		{
			OverallStatsScreen scrn = ScreenCache.getScreen(OverallStatsScreen.class);
			ScreenCache.switchScreen(scrn);
		}
		else if (src == btnUtilities)
		{
			UtilitiesScreen scrn = ScreenCache.getScreen(UtilitiesScreen.class);
			ScreenCache.switchScreen(scrn);
		}
		else
		{
			super.actionPerformed(arg0);
		}
	}
}
