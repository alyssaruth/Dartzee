package burlton.dartzee.code.screen;

import burlton.core.code.obj.HandyArrayList;
import burlton.dartzee.code.bean.PlayerTypeFilterPanel;
import burlton.dartzee.code.bean.ScrollTablePlayers;
import burlton.dartzee.code.db.PlayerEntity;
import burlton.desktopcore.code.screen.SimpleDialog;
import burlton.desktopcore.code.util.DialogUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class PlayerSelectDialog extends SimpleDialog
{
	private List<PlayerEntity> players = new ArrayList<>();
	private HandyArrayList<PlayerEntity> selectedPlayers = new HandyArrayList<>();
	private ArrayList<PlayerEntity> playersToExclude = new ArrayList<>();
	
	public PlayerSelectDialog(int selectionMode) 
	{
		super();
		setTitle("Select Player(s)");
		setSize(300, 300);
		setModal(true);
		
		getContentPane().add(panelNorth, BorderLayout.NORTH);
		getContentPane().add(tablePlayers, BorderLayout.CENTER);
		tablePlayers.setSelectionMode(selectionMode);
		
		panelNorth.addActionListener(this);
	}
	
	private final PlayerTypeFilterPanel panelNorth = new PlayerTypeFilterPanel();
	private final ScrollTablePlayers tablePlayers = new ScrollTablePlayers();
	
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		if (panelNorth.isEventSource(arg0))
		{
			init();
		}
		else
		{
			super.actionPerformed(arg0);
		}
	}
	
	private void init()
	{
		retrievePlayers();
		populateTable();
	}
	
	private void retrievePlayers()
	{
		String whereSql = panelNorth.getWhereSql();
		players = PlayerEntity.retrievePlayers(whereSql, false);
		
		for (int i=players.size()-1; i >= 0; i--)
		{
			PlayerEntity player = players.get(i);
			if (playersToExclude.contains(player))
			{
				players.remove(i);
			}
		}
	}
	
	private void populateTable()
	{
		tablePlayers.initTableModel(players);
	}
	
	/**
	 * Gets / sets
	 */
	public void setPlayersToExclude(ArrayList<PlayerEntity> playersToExclude)
	{
		this.playersToExclude = playersToExclude;
	}
	public HandyArrayList<PlayerEntity> getSelectedPlayers()
	{
		return selectedPlayers;
	}
	
	/**
	 * Statics
	 */
	public static PlayerEntity selectPlayer()
	{
		HandyArrayList<PlayerEntity> players = selectPlayers(new ArrayList<>(), ListSelectionModel.SINGLE_SELECTION);
		if (players.isEmpty())
		{
			return null;
		}
		
		return players.firstElement();
	}
	public static HandyArrayList<PlayerEntity> selectPlayers(ArrayList<PlayerEntity> playersToExclude)
	{
		return selectPlayers(playersToExclude, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}
	private static HandyArrayList<PlayerEntity> selectPlayers(ArrayList<PlayerEntity> playersToExclude, int selectionMode)
	{
		PlayerSelectDialog dialog = new PlayerSelectDialog(selectionMode);
		dialog.setPlayersToExclude(playersToExclude);
		dialog.init();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
		
		return dialog.getSelectedPlayers();
	}

	@Override
	public void okPressed()
	{
		selectedPlayers = tablePlayers.getSelectedPlayers();
		if (selectedPlayers.isEmpty())
		{
			DialogUtil.showError("You must select at least one player.");
			return;
		}
		
		dispose();
	}
}
