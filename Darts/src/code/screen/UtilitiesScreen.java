package code.screen;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;

import code.db.sanity.DatabaseSanityCheck;
import code.utils.DartsDatabaseUtil;
import code.utils.DevUtilities;
import net.miginfocom.swing.MigLayout;
import screen.DebugConsoleAdv;
import util.AbstractClient;
import util.ComponentUtil;
import util.DialogUtil;
import util.ThreadUtil;

public class UtilitiesScreen extends EmbeddedScreen
{
	public UtilitiesScreen() 
	{
		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new MigLayout("", "[grow]", "[][][][][][][][][][][]"));
		btnDeleteGame.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel.add(btnDeleteGame, "cell 0 0,alignx center,aligny center");
		btnCreateBackup.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel.add(btnCreateBackup, "cell 0 2,alignx center");
		btnRestoreFromBackup.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel.add(btnRestoreFromBackup, "cell 0 4,alignx center");
		btnMerge.setFont(new Font("Tahoma", Font.PLAIN, 18));
		
		panel.add(btnMerge, "cell 0 5,alignx center");
		btnPerformDatabaseCheck.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel.add(btnPerformDatabaseCheck, "cell 0 6,alignx center,aligny center");
		btnThreadStacks.setFont(new Font("Tahoma", Font.PLAIN, 18));
		
		panel.add(btnThreadStacks, "cell 0 7,alignx center");
		btnCheckForUpdates.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel.add(btnCheckForUpdates, "cell 0 8,alignx center");
		btnViewLogs.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel.add(btnViewLogs, "cell 0 10,alignx center");
		
		ArrayList<AbstractButton> buttons = ComponentUtil.getAllChildComponentsForType(panel, AbstractButton.class);
		for (AbstractButton button : buttons)
		{
			button.addActionListener(this);
		}
	}
	
	private final JButton btnDeleteGame = new JButton("Delete Game");
	private final JButton btnCreateBackup = new JButton("Create backup");
	private final JButton btnRestoreFromBackup = new JButton("Restore from backup");
	private final JButton btnPerformDatabaseCheck = new JButton("Perform Database Check");
	private final JButton btnCheckForUpdates = new JButton("Check for Updates");
	private final JButton btnViewLogs = new JButton("View Logs");
	private final JButton btnThreadStacks = new JButton("Thread Stacks");
	private final JButton btnMerge = new JButton("Merge databases");

	@Override
	public String getScreenName()
	{
		return "Utilities";
	}

	@Override
	public void init()
	{
		//Nothing to do, it's just a placeholder for some buttons
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		Object src = arg0.getSource();
		
		if (src == btnDeleteGame)
		{
			DevUtilities.purgeGame();
		}
		else if (src == btnCreateBackup)
		{
			DartsDatabaseUtil.backupCurrentDatabase();
		}
		else if (src == btnRestoreFromBackup)
		{
			DartsDatabaseUtil.restoreDatabase();
		}
		else if (src == btnPerformDatabaseCheck)
		{
			DatabaseSanityCheck.runSanityCheck();
		}
		else if (src == btnCheckForUpdates)
		{
			AbstractClient.getInstance().checkForUpdates();
		}
		else if (src == btnViewLogs)
		{
			DebugConsoleAdv loggingDialog = ScreenCache.getDebugConsole();
			loggingDialog.setVisible(true);
			loggingDialog.toFront();
		}
		else if (src == btnThreadStacks)
		{
			ThreadUtil.dumpStacks();
		}
		else if (src == btnMerge)
		{
			DialogUtil.showError("This isn't finished.");
			return;
			
			//DartsDatabaseUtil.startMerge();
		}
		else
		{
			super.actionPerformed(arg0);
		}
	}

}
