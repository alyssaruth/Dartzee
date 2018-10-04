package code.screen.ai;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import bean.NumberField;
import bean.RadioButtonPanel;
import code.ai.AbstractDartsModel;
import code.ai.AbstractDartsSimulation;
import code.ai.DartsSimulationGolf;
import code.ai.DartsSimulationX01;
import code.db.PlayerEntity;
import code.screen.Dartboard;
import code.screen.ScreenCache;
import code.screen.stats.player.PlayerStatisticsScreen;
import code.stats.GameWrapper;
import net.miginfocom.swing.MigLayout;
import object.SuperHashMap;
import screen.ProgressDialog;
import screen.SimpleDialog;
import util.Debug;
import util.DialogUtil;

public final class AISimulationSetup extends SimpleDialog
{
	private PlayerEntity player = null;
	private boolean modal = false; //If we're from AIConfig, want the results to be modal
	private AbstractDartsModel model = null;
	
	public AISimulationSetup(PlayerEntity player) 
	{
		this(player, null, false);
	}
	public AISimulationSetup(PlayerEntity player, AbstractDartsModel model, boolean dialog)
	{
		this.player = player;
		this.model = model;
		this.modal = dialog;
		
		setTitle("Simulation Options");
		setSize(400, 160);
		setLocationRelativeTo(ScreenCache.getAIConfigurationDialog());
		setModal(true);
		
		nfNumberOfGames.setColumns(10);
		getContentPane().add(panelCenter, BorderLayout.CENTER);
		panelCenter.setLayout(new MigLayout("", "[][]", "[][]"));
		panelCenter.add(lblGameMode, "cell 0 0");
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		
		
		panelCenter.add(panel_1, "cell 1 0,grow");
		
		panel_1.add(rdbtn501);
		
		panel_1.add(rdbtnGolfHoles);
		
		panelCenter.add(lblNumberOfGames, "cell 0 1,alignx trailing");
		
		panelCenter.add(nfNumberOfGames, "cell 1 1,growx");
		nfNumberOfGames.setValue(1000);
	}
	
	private final JPanel panelCenter = new JPanel();
	private final JLabel lblGameMode = new JLabel("Game Mode");
	private final RadioButtonPanel panel_1 = new RadioButtonPanel();
	private final JRadioButton rdbtn501 = new JRadioButton("501");
	private final JRadioButton rdbtnGolfHoles = new JRadioButton("Golf (18 Holes)");
	private final JLabel lblNumberOfGames = new JLabel("Number of games");
	private final NumberField nfNumberOfGames = new NumberField(100, 100000);
	
	@Override
	public void okPressed()
	{
		//Do the simulation...
		if (model == null)
		{
			model = player.getModel();
		}
		
		Dartboard dartboard = new Dartboard(500, 500);
		dartboard.setSimulation(true); //Don't do animations etc
		dartboard.paintDartboard();
		
		AbstractDartsSimulation sim = factorySimulationForSelection(dartboard);
		runSimulationInSeparateThread(sim);
	}
	private AbstractDartsSimulation factorySimulationForSelection(Dartboard dartboard)
	{
		JRadioButton rdbtn = panel_1.getSelection();
		if (rdbtn == rdbtn501)
		{
			return new DartsSimulationX01(dartboard, model);
		}
		
		return new DartsSimulationGolf(dartboard, model);
	}
	
	private void runSimulationInSeparateThread(AbstractDartsSimulation sim)
	{
		final SuperHashMap<Long, GameWrapper> hmGameIdToWrapper = new SuperHashMap<>();
		final int numberOfGames = nfNumberOfGames.getNumber();
		Runnable simulationRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				runSimulation(sim, hmGameIdToWrapper, numberOfGames);
			}
		};
		
		Thread t = new Thread(simulationRunnable, "Simulation Thread");
		t.start();
	}
	private void runSimulation(AbstractDartsSimulation sim, SuperHashMap<Long, GameWrapper> hmGameIdToWrapper, int numberOfGames)
	{
		double startTime = System.currentTimeMillis();
		ProgressDialog dialog = ProgressDialog.factory("Simulating games...", "games remaining", numberOfGames);
		dialog.showCancel(true);
		dialog.setVisibleLater();
		
		Debug.appendBanner("Starting simulation for " + numberOfGames + " games");
		
		for (int i=1; i<=numberOfGames; i++)
		{
			try
			{
				GameWrapper wrapper = sim.simulateGame(-i);
				hmGameIdToWrapper.put(Long.valueOf(-i), wrapper);
				dialog.incrementProgressLater();
				
				Debug.logProgress(i, numberOfGames, 10);
				
				if (dialog.cancelPressed())
				{
					Debug.append("Simulation Cancelled");
					hmGameIdToWrapper.clear();
					dialog.disposeLater();
					return;
				}
			}
			catch (Throwable t)
			{
				hmGameIdToWrapper.clear();
				dialog.disposeLater();
				Debug.stackTrace(t);
				DialogUtil.showErrorLater("A serious problem has occurred with the simulation.");
			}
		}
		
		double finishTime = System.currentTimeMillis();
		Debug.appendBanner("Simulation complete - Took " + (finishTime - startTime) + " millis");
		dialog.disposeLater();
		
		if (!hmGameIdToWrapper.isEmpty())
		{
			//The simulation finished successfully, so show it
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					simulationFinished(hmGameIdToWrapper, sim.getGameType());
				}
			});
		}
	}
	private void simulationFinished(SuperHashMap<Long, GameWrapper> hmGameIdToWrapper, int gameType)
	{
		String title = "Simulation Results - " + player.getName() + " (" + hmGameIdToWrapper.size() + " games)";
		Window parentWindow = getParentWindowForResults(title);
		parentWindow.setSize(1200, 800);
		parentWindow.setLayout(new BorderLayout(0, 0));
		
		PlayerStatisticsScreen scrn = new PlayerStatisticsScreen();
		scrn.setVariables(gameType, player);
		scrn.initFake(hmGameIdToWrapper);
		parentWindow.add(scrn);
		parentWindow.setVisible(true);
		
		dispose();
	}
	private Window getParentWindowForResults(String title)
	{
		if (modal)
		{
			JDialog dlg = new JDialog();
			dlg.setModal(true);
			dlg.setTitle(title);
			return dlg;
		}
		
		return new JFrame(title);
	}
}
