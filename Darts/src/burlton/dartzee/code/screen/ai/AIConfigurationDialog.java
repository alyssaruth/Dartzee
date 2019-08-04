package burlton.dartzee.code.screen.ai;

import burlton.dartzee.code.ai.AbstractDartsModel;
import burlton.dartzee.code.ai.SimulationWrapper;
import burlton.dartzee.code.db.PlayerEntity;
import burlton.dartzee.code.screen.AbstractPlayerCreationDialog;
import burlton.dartzee.code.screen.Dartboard;
import burlton.dartzee.code.screen.ScreenCache;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Vector;

public class AIConfigurationDialog extends AbstractPlayerCreationDialog
{
	private PlayerEntity aiPlayer = null;
	private SimulationWrapper simulationWrapper = null;
	
	public AIConfigurationDialog()
	{
		super();
		
		setTitle("Configure AI");
		setSize(1100, 720);
		setResizable(false);
		setModal(true);
		getContentPane().add(panelSouth, BorderLayout.EAST);
		panelSouth.setBorder(null);
		panelSouth.setLayout(new BorderLayout(0, 0));
		panelCalculateStats.setBorder(new TitledBorder(null, "Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelSouth.add(panelCalculateStats, BorderLayout.NORTH);
		panelCalculateStats.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panelCalculateStats.add(panel, BorderLayout.NORTH);
		
		JLabel lblAverageScore = new JLabel("Average Score");
		panel.add(lblAverageScore);
		
		
		panel.add(textFieldAverageScore);
		textFieldAverageScore.setEditable(false);
		textFieldAverageScore.setColumns(5);
		
		JLabel lblMiss = new JLabel("Miss %");
		panel.add(lblMiss);
		textFieldMissPercent.setEditable(false);
		textFieldMissPercent.setColumns(5);
		
		panel.add(textFieldMissPercent);
		
		panel.add(lblTreble);
		textFieldTreblePercent.setEditable(false);
		textFieldTreblePercent.setColumns(5);
		
		panel.add(textFieldTreblePercent);
		
		JLabel lblDouble = new JLabel("Double %");
		panel.add(lblDouble);
		lblDouble.setBorder(new EmptyBorder(0, 10, 0, 0));
		
		
		panel.add(textFieldFinishPercent);
		textFieldFinishPercent.setEditable(false);
		textFieldFinishPercent.setColumns(5);
		
		JPanel panelCalculate = new JPanel();
		panelCalculateStats.add(panelCalculate, BorderLayout.SOUTH);
		panelCalculate.add(btnCalculate);
		
		panelCalculate.add(btnRunSimulation);
		panelSouth.add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		
		tabbedPane.setPreferredSize(new Dimension(600, 5));
		tabbedPane.setMinimumSize(new Dimension(600, 5));
		
		tabbedPane.addTab("Scatter", null, scatterTab, null);
		tabbedPane.addTab("Density", null, densityTab, null);
		
		
		getContentPane().add(panelScreen, BorderLayout.CENTER);
		panelScreen.setLayout(new BorderLayout(0, 0));
		panelScreen.add(panelNorth, BorderLayout.NORTH);
		panelNorth.setLayout(new BorderLayout(0, 0));
		panelSetup.setPreferredSize(new Dimension(10, 100));
		panelSetup.setBorder(new TitledBorder(null, "Setup", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelNorth.add(panelSetup, BorderLayout.CENTER);
		panelNorth.add(panelAIConfig, BorderLayout.SOUTH);
		panelSetup.setLayout(new MigLayout("", "[120px][150px]", "[25px][25px]"));
		
		JLabel lblModel = new JLabel("Model");
		lblModel.setPreferredSize(new Dimension(120, 25));
		panelSetup.add(lblModel, "cell 0 1,alignx left,aligny top");
		panelSetup.add(comboBox, "cell 1 1,grow");
		panelSetup.add(lblName, "cell 0 0,grow");
		panelSetup.add(textFieldName, "cell 1 0,grow");
		textFieldName.setColumns(10);
		
		JPanel panelAvatar = new JPanel();
		panelNorth.add(panelAvatar, BorderLayout.WEST);
		panelAvatar.setLayout(new BorderLayout(0, 0));
		panelAvatar.add(avatar, BorderLayout.NORTH);
		tabbedPaneGameSpecifics.setBorder(new TitledBorder(null, "Strategy", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelScreen.add(tabbedPaneGameSpecifics, BorderLayout.CENTER);
		
		tabbedPaneGameSpecifics.addTab("X01", panelX01Config);
		tabbedPaneGameSpecifics.addTab("Golf", panelGolfConfig);
		
		//Listeners
		comboBox.addActionListener(this);
		btnCalculate.addActionListener(this);
		btnRunSimulation.addActionListener(this);
	}
	
	private final JPanel panelScreen = new JPanel();
	private final JPanel panelNorth = new JPanel();
	private final JPanel panelSetup = new JPanel();
	private final JLabel lblName = new JLabel("Name");
	private final JComboBox<String> comboBox = new JComboBox<>();
	private final JTabbedPane tabbedPaneGameSpecifics = new JTabbedPane();
	private final AIConfigurationSubPanelX01 panelX01Config = new AIConfigurationSubPanelX01();
	private final AIConfigurationSubPanelGolf panelGolfConfig = new AIConfigurationSubPanelGolf();
	private final JPanel panelSouth = new JPanel();
	private final JPanel panelCalculateStats = new JPanel();
	private final JTextField textFieldAverageScore = new JTextField();
	private final JTextField textFieldFinishPercent = new JTextField();
	private final JButton btnCalculate = new JButton("Calculate");
	
	private final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
	private final VisualisationPanelScatter scatterTab = new VisualisationPanelScatter();
	private final VisualisationPanelDensity densityTab = new VisualisationPanelDensity();
	
	private AbstractAIConfigurationPanel panelAIConfig = new AIConfigurationNormalDistribution();
	private final JButton btnRunSimulation = new JButton("Run Simulation...");
	private final JTextField textFieldMissPercent = new JTextField();
	private final JLabel lblTreble = new JLabel("Treble %");
	private final JTextField textFieldTreblePercent = new JTextField();
	
	public void init(PlayerEntity aiPlayer)
	{
		this.aiPlayer = aiPlayer;
		
		populateComboBox();
		initFields();
		setFieldsEditable();
		
		tabbedPane.setEnabled(false);
		scatterTab.reset();
		densityTab.reset();
	}
	
	private void initFields()
	{
		avatar.init(aiPlayer, false);
		
		if (aiPlayer == null)
		{
			//Clear the fields
			textFieldName.setText("");
			comboBox.setSelectedItem(AbstractDartsModel.DARTS_MODEL_NORMAL_DISTRIBUTION);
			avatar.setReadOnly(false);
			setPanelBasedOnModel(AbstractDartsModel.DARTS_MODEL_NORMAL_DISTRIBUTION);
			
			panelX01Config.reset();
			panelGolfConfig.reset();
			panelAIConfig.reset();
		}
		else
		{
			avatar.setReadOnly(true);
			
			String name = aiPlayer.getName();
			textFieldName.setText(name);
			
			int strategy = aiPlayer.getStrategy();
			String strategyDesc = AbstractDartsModel.getStrategyDesc(strategy);
			comboBox.setSelectedItem(strategyDesc);
			
			String xmlStr = aiPlayer.getStrategyXml();
			AbstractDartsModel model = AbstractDartsModel.factoryForType(strategy);
			model.readXml(xmlStr);
			
			panelAIConfig.initialiseFromModel(model);
			panelX01Config.initialiseFromModel(model);
			panelGolfConfig.initialiseFromModel(model);
		}
		
		//Reset the calculation stuff too
		textFieldAverageScore.setText("");
		textFieldMissPercent.setText("");
		textFieldTreblePercent.setText("");
		textFieldFinishPercent.setText("");
		btnCalculate.setText("Calculate");
	}
	
	private void setFieldsEditable()
	{
		boolean editable = aiPlayer == null;
		
		comboBox.setEnabled(editable);
		textFieldName.setEditable(editable);
	}
	
	private void populateComboBox()
	{
		Vector<String> models = AbstractDartsModel.getModelDescriptions();
		ComboBoxModel<String> model = new DefaultComboBoxModel<>(models);
		comboBox.setModel(model);
	}
	
	private void setPanelBasedOnModel(String model)
	{
		panelNorth.remove(panelAIConfig);
		
		if (model.equals(AbstractDartsModel.DARTS_MODEL_NORMAL_DISTRIBUTION))
		{
			panelAIConfig = new AIConfigurationNormalDistribution();
		}
		
		panelNorth.add(panelAIConfig, BorderLayout.SOUTH);
		revalidate();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		Component source = (Component)arg0.getSource();
		if (source == comboBox)
		{
			String model = (String)comboBox.getSelectedItem();
			setPanelBasedOnModel(model);
		}
		else if (source == btnCalculate)
		{
			if (validModel())
			{
				calculateStats();
			}
		}
		else if (source == btnRunSimulation)
		{
			runSimulation();
		}
		else
		{
			super.actionPerformed(arg0);
		}
	}
	
	private void runSimulation()
	{
		if (validModel())
		{
			AbstractDartsModel model = getPopulatedModelFromPanels();
			
			//If the player hasn't actually been created yet, then we need to instantiate a PlayerEntity just to hold stuff like the name
			PlayerEntity playerForSimulation = aiPlayer;
			if (playerForSimulation == null)
			{
				String name = textFieldName.getText();
				if (!isValidName(name, doExistenceCheck()))
				{
					return;
				}
				
				playerForSimulation = new PlayerEntity();
				playerForSimulation.setName(name);
			}
			
			AISimulationSetup dlg = new AISimulationSetup(playerForSimulation, model, true);
			dlg.setVisible(true);
		}
	}
	
	@Override
	protected boolean valid()
	{
		if (!super.valid())
		{
			return false;
		}
		
		return validModel();
	}
	
	@Override
	protected boolean doExistenceCheck()
	{
		return aiPlayer == null;
	}
	
	private boolean validModel()
	{
		if (!panelAIConfig.valid())
		{
			return false;
		}
		
		if (!panelX01Config.valid())
		{
			return false;
		}
		  
		return true;
	}
	
	@Override
	public void savePlayer()
	{
		if (aiPlayer == null)
		{
			aiPlayer = PlayerEntity.factoryCreate();
		}
		
		String name = textFieldName.getText();
		aiPlayer.setName(name);
		
		AbstractDartsModel model = getPopulatedModelFromPanels();
		int type = model.getType();
		String xmlStr = model.writeXml();
		
		aiPlayer.setStrategy(type);
		aiPlayer.setStrategyXml(xmlStr);
		
		String avatarId = avatar.getAvatarId();
		aiPlayer.setPlayerImageId(avatarId);
		
		aiPlayer.saveToDatabase();
		
		createdPlayer = true;
		
		//Now dispose the window
		dispose();
	}
	
	private void calculateStats()
	{
		AbstractDartsModel model = getPopulatedModelFromPanels();
		
		Dartboard dartboard = scatterTab.getDartboard();
		simulationWrapper = model.runSimulation(dartboard);
		
		double averageDart = simulationWrapper.getAverageDart();
		textFieldAverageScore.setText("" + averageDart);
		
		double missPercent = simulationWrapper.getMissPercent();
		textFieldMissPercent.setText("" + missPercent);
		
		double treblePercent = simulationWrapper.getTreblePercent();
		textFieldTreblePercent.setText("" + treblePercent);
		
		double finishPercent = simulationWrapper.getFinishPercent();
		textFieldFinishPercent.setText("" + finishPercent);
		
		btnCalculate.setText("Re-calculate");
		
		visualiseSimulation();
	}
	
	private void visualiseSimulation()
	{
		AbstractDartsModel model = getPopulatedModelFromPanels();
		HashMap<Point, Integer> hmPointToCount = simulationWrapper.getHmPointToCount();
		
		tabbedPane.setEnabled(true);
		scatterTab.populate(hmPointToCount, model);
		densityTab.populate(hmPointToCount, model);
	}
	
	private AbstractDartsModel getPopulatedModelFromPanels()
	{
		AbstractDartsModel model = panelAIConfig.initialiseModel();
		panelX01Config.populateModel(model);
		panelGolfConfig.populateModel(model);
		
		return model;
	}

	public static void amendPlayer(PlayerEntity player)
	{
		AIConfigurationDialog dialog = ScreenCache.getAIConfigurationDialog();
		dialog.init(player);
		dialog.setVisible(true);
	}
}
