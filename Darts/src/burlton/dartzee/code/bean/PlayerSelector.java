package burlton.dartzee.code.bean;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import burlton.desktopcore.code.bean.DoubleClickListener;
import burlton.desktopcore.code.bean.ScrollTable;
import burlton.dartzee.code.db.PlayerEntity;
import net.miginfocom.swing.MigLayout;
import burlton.core.code.obj.HandyArrayList;
import burlton.desktopcore.code.util.DialogUtil;

public class PlayerSelector extends JPanel
							implements ActionListener, DoubleClickListener
{
	public PlayerSelector() 
	{
		setLayout(new MigLayout("", "[452px][100px][452px]", "[407px]"));
		
		add(tablePlayersToSelectFrom, "cell 0 0,alignx left,growy");
		panelMovementOptions.setMinimumSize(new Dimension(50, 10));
		add(panelMovementOptions, "cell 1 0,grow");
		panelMovementOptions.setLayout(new MigLayout("al center center, wrap, gapy 20"));
		btnSelect.setIcon(new ImageIcon(PlayerSelector.class.getResource("/buttons/rightArrow.png")));
		btnSelect.setPreferredSize(new Dimension(40, 40));
		
		panelMovementOptions.add(btnSelect, "cell 0 0,alignx left,aligny top");
		btnUnselect.setIcon(new ImageIcon(PlayerSelector.class.getResource("/buttons/leftArrow.png")));
		btnUnselect.setPreferredSize(new Dimension(40, 40));
		
		panelMovementOptions.add(btnUnselect, "cell 0 1,alignx left,aligny top");
		add(tablePlayersSelected, "cell 2 0,alignx left,growy");
		
		tablePlayersSelected.enableManualReordering();
		
		
		tablePlayersSelected.addDoubleClickListener(this);
		tablePlayersToSelectFrom.addDoubleClickListener(this);
		
		btnSelect.addActionListener(this);
		btnUnselect.addActionListener(this);
		
		addKeyListener(tablePlayersSelected);
		addKeyListener(tablePlayersToSelectFrom);
	}
	
	private final ScrollTablePlayers tablePlayersToSelectFrom = new ScrollTablePlayers();
	private final JPanel panelMovementOptions = new JPanel();
	private final ScrollTablePlayers tablePlayersSelected = new ScrollTablePlayers();
	private final JButton btnSelect = new JButton("");
	private final JButton btnUnselect = new JButton("");
	
	public void init()
	{
		ArrayList<PlayerEntity> allPlayers = PlayerEntity.retrievePlayers("", false);
		tablePlayersToSelectFrom.initTableModel(allPlayers);
		tablePlayersSelected.removeAllRows();
	}
	
	public void init(HandyArrayList<PlayerEntity> selectedPlayers)
	{
		init();
		moveRows(tablePlayersToSelectFrom, tablePlayersSelected, selectedPlayers);
	}
	
	private void addKeyListener(ScrollTable table)
	{
		table.addKeyAction(KeyEvent.VK_ENTER, "Enter", new AbstractAction() 
		{
			@Override
			public void actionPerformed(ActionEvent ae) 
			{
				moveRows(table);
			}
		});
	}
	
	private void moveRows(ScrollTablePlayers source, ScrollTablePlayers destination)
	{
		HandyArrayList<PlayerEntity> selectedPlayers = source.getSelectedPlayers();
		if (selectedPlayers.isEmpty())
		{
			//Nothing to do
			return;
		}
		
		moveRows(source, destination, selectedPlayers);
	}
	private void moveRows(ScrollTablePlayers source, ScrollTablePlayers destination, HandyArrayList<PlayerEntity> selectedPlayers)
	{
		destination.addPlayers(selectedPlayers);
		
		int rowToSelect = source.getSelectedViewRow();
		
		HandyArrayList<PlayerEntity> allPlayers = source.getAllPlayers();
		allPlayers.removeAll(selectedPlayers);
		source.initTableModel(allPlayers);
		
		if (rowToSelect > allPlayers.size() - 1)
		{
			rowToSelect = 0;
		}
		
		if (!allPlayers.isEmpty())
		{
			source.selectRow(rowToSelect);
		}
	}
	
	public HandyArrayList<PlayerEntity> getSelectedPlayers()
	{
		return tablePlayersSelected.getAllPlayers();
	}
	
	/**
	 * Is this selection valid for a game/match?
	 */
	public boolean valid()
	{
		ArrayList<PlayerEntity> selectedPlayers = getSelectedPlayers();
		int rowCount = selectedPlayers.size();
		if (rowCount < 2)
		{
			DialogUtil.showError("You must select at least 2 players.");
			return false;
		}
		
		if (rowCount > 4)
		{
			DialogUtil.showError("You cannot have more than 4 players.");
			return false;
		}
		
		return true;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == btnSelect)
		{
			moveRows(tablePlayersToSelectFrom, tablePlayersSelected);
		}
		else if (e.getSource() == btnUnselect)
		{
			moveRows(tablePlayersSelected, tablePlayersToSelectFrom);
		}
	}
	
	private void moveRows(Object source)
	{
		if (source == tablePlayersToSelectFrom)
		{
			moveRows(tablePlayersToSelectFrom, tablePlayersSelected);
		}
		else
		{
			moveRows(tablePlayersSelected, tablePlayersToSelectFrom);
		}
	}

	@Override
	public void doubleClicked(Component source)
	{
		moveRows(source);
	}
}
