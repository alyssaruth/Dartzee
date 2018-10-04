package code.screen;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;

import bean.ScrollTableButton;
import code.db.PlayerEntity;
import object.HandyArrayList;
import screen.SimpleDialog;
import util.TableUtil.DefaultModel;

/**
 * Dialog to match up players from a remote DB to the local one during a merge
 */
public final class PlayerMatchingDialog extends SimpleDialog
{
	private HandyArrayList<PlayerEntity> playersToImport = null;
	
	
	public PlayerMatchingDialog()
	{
		setSize(500, 500);
		
		
	}
	
	private ScrollTableButton scrollTable = null;

	public void init()
	{
		DefaultModel model = new DefaultModel();
		model.addColumn("");
		model.addColumn("Player");
		model.addColumn("Status");
		model.addColumn("");
		
		ArrayList<PlayerEntity> currentPlayers = PlayerEntity.retrievePlayers("", false);
		
		for (PlayerEntity player : playersToImport)
		{
			attemptAutoMatch(player, currentPlayers);
			
			String btnString = "Match Player > ";
			if (player.getMatchedPlayer() != null)
			{
				btnString = "Unmatch > ";
			}
			
			Object[] row = {player.getFlag(), player, player.getMatchDesc(), btnString};
			model.addRow(row);
		}
		
		AbstractAction btnAction = new AbstractAction() 
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int modelRow = Integer.valueOf(e.getActionCommand());
				
				
			}
		};
		
		scrollTable = new ScrollTableButton(3, model, btnAction);
		
		getContentPane().add(scrollTable, BorderLayout.CENTER);
	}
	private static void attemptAutoMatch(PlayerEntity player, ArrayList<PlayerEntity> potentialMatches)
	{
		String playerName = player.getName();
		for (PlayerEntity potentialMatch : potentialMatches)
		{
			String otherName = potentialMatch.getName();
			if (otherName.equals(playerName))
			{
				player.setMatchedPlayer(potentialMatch);
				player.setAutoMatched(true);
			}
		}
	}
	
	@Override
	public void okPressed()
	{
		// TODO Auto-generated method stub

	}
	
	public static void matchPlayers(HandyArrayList<PlayerEntity> playersToImport)
	{
		PlayerMatchingDialog dialog = new PlayerMatchingDialog();
		dialog.playersToImport = playersToImport;
		dialog.init();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}
}
