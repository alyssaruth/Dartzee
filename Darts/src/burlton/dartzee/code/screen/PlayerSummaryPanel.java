package burlton.dartzee.code.screen;

import burlton.dartzee.code.db.GameEntity;
import burlton.dartzee.code.db.PlayerEntity;
import burlton.dartzee.code.screen.stats.player.PlayerStatisticsScreen;
import burlton.dartzee.code.stats.PlayerSummaryStats;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PlayerSummaryPanel extends JPanel
								implements ActionListener
{
	private PlayerEntity player = null;
	private int gameType = -1;
	
	public PlayerSummaryPanel(int gameType)
	{
		this.gameType = gameType;
		
		setBorder(new TitledBorder(null, GameEntity.getTypeDesc(gameType), TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.PLAIN, 20)));
		setLayout(new MigLayout("", "[][][][][][][][][grow][]", "[][][]"));
		
		add(lblP, "cell 0 0");
		
		add(lblW, "cell 2 0,alignx leading");
		
		add(lblHighScore, "cell 4 0");
		nfGamesPlayed.setEditable(false);
		add(nfGamesPlayed, "cell 0 1,growx");
		nfGamesPlayed.setColumns(10);
		Component horizontalStrut = Box.createHorizontalStrut(20);
		add(horizontalStrut, "cell 1 1");
		nfGamesWon.setEditable(false);
		add(nfGamesWon, "cell 2 1,growx");
		nfGamesWon.setColumns(10);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		add(horizontalStrut_1, "cell 3 1");
		nfBestGame.setEditable(false);
		add(nfBestGame, "cell 4 1,growx");
		nfBestGame.setColumns(10);
		
		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		add(horizontalStrut_2, "flowx,cell 8 1");
		btnViewStats.setFont(new Font("Tahoma", Font.PLAIN, 16));
		add(btnViewStats, "cell 8 1,alignx center");
		
		btnViewStats.addActionListener(this);
	}
	
	private final JTextField nfGamesPlayed = new JTextField();
	private final JTextField nfGamesWon = new JTextField();
	private final JTextField nfBestGame = new JTextField();
	private final JButton btnViewStats = new JButton("View Stats");
	private final JLabel lblP = new JLabel("Played");
	private final JLabel lblW = new JLabel("Won");
	private final JLabel lblHighScore = new JLabel("High score");
	
	public void init(PlayerEntity player)
	{
		setVisible(true);
		
		this.player = player;
		
		PlayerSummaryStats stats = PlayerSummaryStats.getSummaryStats(player, gameType);
		
		int gamesPlayed = stats.getGamesPlayed();
		int gamesWon = stats.getGamesWon();
		int bestScore = stats.getBestScore();
		
		nfGamesPlayed.setText("" + gamesPlayed);
		nfGamesWon.setText("" + gamesWon);
		
		if (bestScore > 0)
		{
			nfBestGame.setText("" + bestScore);
		}
		else
		{
			nfBestGame.setText("");
		}
		
		btnViewStats.setEnabled(gamesPlayed > 0);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (arg0.getSource() == btnViewStats)
		{
			PlayerStatisticsScreen statsScrn = ScreenCache.getScreen(PlayerStatisticsScreen.class);
			statsScrn.setVariables(gameType, player);
			
			ScreenCache.switchScreen(statsScrn);
		}
	}
}
