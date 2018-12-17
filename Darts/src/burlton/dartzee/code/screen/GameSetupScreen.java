package burlton.dartzee.code.screen;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import burlton.dartzee.code.screen.game.DartsGameScreen;
import burlton.dartzee.code.bean.ComboBoxGameType;
import burlton.dartzee.code.bean.GameParamFilterPanel;
import burlton.dartzee.code.bean.GameParamFilterPanelX01;
import burlton.dartzee.code.bean.PlayerSelector;
import burlton.dartzee.code.db.GameEntity;
import burlton.dartzee.code.db.PlayerEntity;

public class GameSetupScreen extends EmbeddedScreen
{
	public GameSetupScreen() 
	{
		super();
		
		panelGameType.setBorder(new TitledBorder(null, "Game Type", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panelGameType, BorderLayout.NORTH);
		panelGameType.setLayout(new GridLayout(0, 1, 0, 0));
		
		panelGameType.add(panel);
		panel.add(gameTypeComboBox);
		
		panelGameType.add(gameParamFilterPanel);
		
		panelPlayers.setBorder(new TitledBorder(null, "Players", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panelPlayers, BorderLayout.CENTER);
		panelPlayers.setLayout(new BorderLayout(0, 0));
		panelPlayers.add(launchPanel, BorderLayout.SOUTH);
		launchPanel.add(btnLaunch);
		panelPlayers.add(playerSelector, BorderLayout.CENTER);
		
		gameTypeComboBox.addActionListener(this);
		
		btnLaunch.addActionListener(this);
	}
	
	private final JPanel panelGameType = new JPanel();
	private final JPanel panelPlayers = new JPanel();
	private final JPanel launchPanel = new JPanel();
	private final JButton btnLaunch = new JButton("Launch Game");
	private final PlayerSelector playerSelector = new PlayerSelector();
	private final ComboBoxGameType gameTypeComboBox = new ComboBoxGameType();
	private final JPanel panel = new JPanel();
	private GameParamFilterPanel gameParamFilterPanel = new GameParamFilterPanelX01();
	
	@Override
	public void initialise()
	{
		playerSelector.init();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		if (arg0.getSource() == btnLaunch)
		{
			launchGame();
		}
		else if (arg0.getSource() == gameTypeComboBox)
		{
			panelGameType.remove(gameParamFilterPanel);
			gameParamFilterPanel = GameEntity.getFilterPanel(gameTypeComboBox.getGameType());
			panelGameType.add(gameParamFilterPanel);
			panelGameType.revalidate();
		}
		else
		{
			super.actionPerformed(arg0);
		}
	}
	
	public void launchGame()
	{
		if (playerSelector.valid())
		{
			ArrayList<PlayerEntity> selectedPlayers = playerSelector.getSelectedPlayers();
			DartsGameScreen.launchNewGame(selectedPlayers, gameTypeComboBox.getGameType(), getGameParams());
		}
	}
	
	private String getGameParams()
	{
		return gameParamFilterPanel.getGameParams();
	}
	
	@Override
	public void nextPressed()
	{
		MatchSetupScreen scrn = ScreenCache.getScreen(MatchSetupScreen.class);
		scrn.init(playerSelector.getSelectedPlayers(), gameTypeComboBox.getGameType(), getGameParams());
		ScreenCache.switchScreen(scrn);
	}

	@Override
	public String getScreenName()
	{
		return "Game Setup";
	}
	@Override
	public boolean showNextButton()
	{
		return true;
	}
	@Override
	public String getNextText()
	{
		return "Match Setup";
	}
}
