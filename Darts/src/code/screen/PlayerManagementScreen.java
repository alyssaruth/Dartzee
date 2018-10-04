package code.screen;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import code.bean.ScrollTablePlayers;
import code.db.PlayerEntity;

import javax.swing.ImageIcon;

public class PlayerManagementScreen extends EmbeddedScreen
									implements ListSelectionListener
{
	public PlayerManagementScreen() 
	{
		super();
		
		JPanel sideBar = new JPanel();
		sideBar.setLayout(new BorderLayout(0, 0));
		
		JPanel panelPlayers = new JPanel();
		panelPlayers.setPreferredSize(new Dimension(200, 300));
		panelPlayers.setBorder(new TitledBorder(null, "Players", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		sideBar.add(panelPlayers, BorderLayout.CENTER);
		
		add(sideBar, BorderLayout.WEST);
		panelPlayers.setLayout(new BorderLayout(0, 0));
		
		panelPlayers.add(tablePlayers, BorderLayout.CENTER);
		panel_1.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		sideBar.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		btnNewPlayer.setIcon(new ImageIcon(PlayerManagementScreen.class.getResource("/buttons/addHuman.png")));
		btnNewPlayer.setPreferredSize(new Dimension(30, 30));
		panel_1.add(btnNewPlayer);
		btnNewPlayer.setBorder(new EmptyBorder(5, 0, 5, 0));
		btnNewAi.setIcon(new ImageIcon(PlayerManagementScreen.class.getResource("/buttons/addAi.png")));
		btnNewAi.setPreferredSize(new Dimension(30, 30));
		
		panel_1.add(btnNewAi);
		add(panel, BorderLayout.CENTER);
		
		ListSelectionModel model = tablePlayers.getSelectionModel();
		model.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		model.addListSelectionListener(this);
		
		//Listeners
		btnNewPlayer.addActionListener(this);
		btnNewAi.addActionListener(this);
	}
	
	private final ScrollTablePlayers tablePlayers = new ScrollTablePlayers();
	private final PlayerManagementPanel panel = new PlayerManagementPanel();
	private final JButton btnNewPlayer = new JButton("");
	private final JPanel panel_1 = new JPanel();
	private final JButton btnNewAi = new JButton("");
	
	@Override
	public void init()
	{
		buildTable();
		showNoSelectionPanel();
	}
	
	private void buildTable()
	{
		ArrayList<PlayerEntity> players = PlayerEntity.retrievePlayers("", false);
		tablePlayers.initTableModel(players);
	}
	
	private void showNoSelectionPanel()
	{
		panel.clear();
	}
	
	@Override
	public String getScreenName()
	{
		return "Player Management";
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0)
	{
		PlayerEntity player = tablePlayers.getSelectedPlayer();
		if (player == null)
		{
			showNoSelectionPanel();
			return;
		}
		
		panel.init(player);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		AbstractButton src = (AbstractButton)arg0.getSource();
		if (src == btnNewPlayer)
		{
			PlayerEntity.createNewPlayer(true);
		}
		else if (src == btnNewAi)
		{
			PlayerEntity.createNewPlayer(false);
		}
		else
		{
			super.actionPerformed(arg0);
		}
	}
}
