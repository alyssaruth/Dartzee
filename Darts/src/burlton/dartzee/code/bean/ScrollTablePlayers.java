package burlton.dartzee.code.bean;

import burlton.core.code.obj.HandyArrayList;
import burlton.dartzee.code.db.PlayerEntity;
import burlton.desktopcore.code.bean.ScrollTable;
import burlton.desktopcore.code.util.TableUtil;

import javax.swing.*;
import java.util.ArrayList;

public class ScrollTablePlayers extends ScrollTable
{
	public ScrollTablePlayers()
	{
		super();
		
		initTableModel();
		
		setRowName("player");	
	}
	
	
	public PlayerEntity getSelectedPlayer()
	{
		int row = table.getSelectedRow();
		if (row == -1)
		{
			return null;
		}
		
		return getPlayerEntityForRow(row);
	}
	public HandyArrayList<PlayerEntity> getAllPlayers()
	{
		HandyArrayList<PlayerEntity> ret = new HandyArrayList<>();
		
		for (int i=0; i<table.getRowCount(); i++)
		{
			PlayerEntity player = getPlayerEntityForRow(i);
			ret.add(player);
		}
		
		return ret;
	}
	public HandyArrayList<PlayerEntity> getSelectedPlayers()
	{
		HandyArrayList<PlayerEntity> ret = new HandyArrayList<>();
		
		int[] viewRows = table.getSelectedRows();
		for (int i=0; i<viewRows.length; i++)
		{
			PlayerEntity player = getPlayerEntityForRow(viewRows[i]);
			ret.add(player);
		}
		
		return ret;
	}
	public PlayerEntity getPlayerEntityForRow(int row)
	{
		//Apparently we don't need to do this conversion
		//int internalRow = table.convertRowIndexToModel(row);
		return (PlayerEntity)table.getValueAt(row, 1);
	}
	
	private void initTableModel()
	{
		initTableModel(new ArrayList<>());
	}
	public void initTableModel(java.util.List<PlayerEntity> players)
	{
		TableUtil.DefaultModel model = new TableUtil.DefaultModel();
		model.addColumn("");
		model.addColumn("Player");
		
		setModel(model);
		
		setRowHeight(23);
		setColumnWidths("25");
		
		for (PlayerEntity player : players)
		{
			addPlayer(player);
		}
		
		sortBy(1, false);
	}
	public void addPlayer(PlayerEntity player)
	{
		ImageIcon flag = player.getFlag();
		Object[] row = {flag, player};
		
		addRow(row);
	}
	
	public void addPlayers(ArrayList<PlayerEntity> players)
	{
		for (PlayerEntity player : players)
		{
			addPlayer(player);
		}
	}
}
