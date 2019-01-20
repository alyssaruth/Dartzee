package burlton.dartzee.code.screen.reporting;

import burlton.core.code.obj.SuperHashMap;
import burlton.core.code.util.Debug;
import burlton.dartzee.code.bean.ComboBoxGameType;
import burlton.dartzee.code.bean.GameParamFilterPanel;
import burlton.dartzee.code.bean.GameParamFilterPanelX01;
import burlton.dartzee.code.bean.ScrollTablePlayers;
import burlton.dartzee.code.db.GameEntityKt;
import burlton.dartzee.code.db.PlayerEntity;
import burlton.dartzee.code.reporting.IncludedPlayerParameters;
import burlton.dartzee.code.reporting.ReportParameters;
import burlton.dartzee.code.screen.EmbeddedScreen;
import burlton.dartzee.code.screen.PlayerSelectDialog;
import burlton.dartzee.code.screen.ScreenCache;
import burlton.desktopcore.code.bean.DateFilterPanel;
import burlton.desktopcore.code.bean.RadioButtonPanel;
import burlton.desktopcore.code.util.ComponentUtil;
import burlton.desktopcore.code.util.DialogUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;
import java.util.ArrayList;

public class ReportingSetupScreen extends EmbeddedScreen
								  implements ChangeListener,
								  ListSelectionListener
{
	private SuperHashMap<PlayerEntity, PlayerParametersPanel> hmIncludedPlayerToPanel = new SuperHashMap<>();
	private ArrayList<PlayerEntity> excludedPlayers = new ArrayList<>();
	
	public ReportingSetupScreen() 
	{
		add(tabbedPane, BorderLayout.CENTER);
		
		tabbedPane.addTab("Game", null, panelGame, null);
		panelGame.setLayout(new MigLayout("hidemode 3", "[][][grow][]", "[][][][][][][][][][][][]"));
		checkBoxGameType.setEnabled(false);
		
		panelGame.add(checkBoxGameType, "flowx,cell 0 0");
		
		panelGame.add(lblGameType, "cell 0 0");
		
		panelGame.add(horizontalStrut, "cell 1 0");
		
		panelGame.add(cbType, "cell 0 1");
		
		panelGame.add(panelGameParams, "cell 2 1");
		
		panelGame.add(verticalStrut, "cell 0 2");
		
		panelGame.add(cbStartDate, "cell 0 3,aligny center");
		FlowLayout fl_panelDtStart = (FlowLayout) dateFilterPanelStart.getLayout();
		fl_panelDtStart.setAlignment(FlowLayout.LEFT);
		panelGame.add(dateFilterPanelStart, "cell 2 3,alignx left,aligny center");
		panelGame.add(cbFinishDate, "cell 0 4");
		panelGame.add(rdbtnUnfinished, "flowx,cell 2 5");
		panelGame.add(rdbtnDtFinish, "flowx,cell 2 4");
		panelGame.add(dateFilterPanelFinish, "cell 2 4");
		panelGame.add(panelUnfinishedLabel, "cell 2 5");
		panelUnfinishedLabel.add(lblUnfinished);
		
		panelGame.add(cbPartOfMatch, "cell 0 6");
		
		panelGame.add(rdbtnYes, "flowx,cell 2 6");
		
		panelGame.add(panelGameType, "cell 2 0");
		
		panelGameType.add(comboBox);
		
		panelGame.add(rdbtnNo, "cell 2 6");
		tabbedPane.addTab("Players", null, panelIncludedPlayers, null);
		panelIncludedPlayers.setLayout(new BorderLayout(0, 0));
		FlowLayout flowLayout = (FlowLayout) panel_3.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panelIncludedPlayers.add(panel_3, BorderLayout.NORTH);
		btnAddIncluded.setPreferredSize(new Dimension(30, 30));
		btnAddIncluded.setIcon(new ImageIcon(ReportingSetupScreen.class.getResource("/buttons/addPlayer.png")));
		panel_3.add(btnAddIncluded);
		btnRemoveIncluded.setIcon(new ImageIcon(ReportingSetupScreen.class.getResource("/buttons/removePlayer.png")));
		btnRemoveIncluded.setPreferredSize(new Dimension(30, 30));
		panel_3.add(btnRemoveIncluded);
		panelIncludedPlayers.add(scrollTableIncluded, BorderLayout.CENTER);
		panelIncludedPlayers.add(includedPlayerPanel, BorderLayout.SOUTH);
		tabbedPane.addTab("Excluded players", null, panelExcludedPlayers, null);
		panelExcludedPlayers.setLayout(new BorderLayout(0, 0));
		FlowLayout flowLayout_1 = (FlowLayout) panel_2.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		panelExcludedPlayers.add(panel_2, BorderLayout.NORTH);
		btnAddExcluded.setIcon(new ImageIcon(ReportingSetupScreen.class.getResource("/buttons/addPlayer.png")));
		btnAddExcluded.setPreferredSize(new Dimension(30, 30));
		panel_2.add(btnAddExcluded);
		btnRemoveExcluded.setIcon(new ImageIcon(ReportingSetupScreen.class.getResource("/buttons/removePlayer.png")));
		btnRemoveExcluded.setPreferredSize(new Dimension(30, 30));
		panel_2.add(btnRemoveExcluded);
		defaultIncludedPlayerPanel.disableAll();
		panelExcludedPlayers.add(scrollTableExcluded, BorderLayout.CENTER);
		
		createButtonGroupsAndSelectDefaults();
		addListeners();
	}
	
	private final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
	
	//Game tab
	private final JPanel panelGame = new JPanel();
	private GameParamFilterPanel panelGameParams = new GameParamFilterPanelX01();
	private final ComboBoxGameType comboBox = new ComboBoxGameType();
	private final JCheckBox cbStartDate = new JCheckBox("Start Date");
	private final DateFilterPanel dateFilterPanelStart = new DateFilterPanel();
	private final JCheckBox cbFinishDate = new JCheckBox("Finish Date");
	private final JRadioButton rdbtnUnfinished = new JRadioButton("");
	private final JRadioButton rdbtnDtFinish = new JRadioButton("");
	private final DateFilterPanel dateFilterPanelFinish = new DateFilterPanel();
	private final JLabel lblUnfinished = new JLabel("Unfinished");
	private final JPanel panelUnfinishedLabel = new JPanel();
	private final JCheckBox cbPartOfMatch = new JCheckBox("Part of Match");
	private final JRadioButton rdbtnYes = new JRadioButton("Yes");
	private final JRadioButton rdbtnNo = new JRadioButton("No");
	
	//Included/Excluded Players
	private final JPanel panelIncludedPlayers = new JPanel();
	private final JPanel panelExcludedPlayers = new JPanel();
	private final JPanel panel_2 = new JPanel();
	private final ScrollTablePlayers scrollTableExcluded = new ScrollTablePlayers();
	private final JPanel panel_3 = new JPanel();
	private final ScrollTablePlayers scrollTableIncluded = new ScrollTablePlayers();
	private final JButton btnAddIncluded = new JButton("");
	private final JButton btnRemoveIncluded = new JButton("");
	private final JButton btnAddExcluded = new JButton("");
	private final JButton btnRemoveExcluded = new JButton("");
	private final PlayerParametersPanel defaultIncludedPlayerPanel = new PlayerParametersPanel();
	
	//Will have one of these per included player, we'll swap them out/in as different players are selected
	private PlayerParametersPanel includedPlayerPanel = defaultIncludedPlayerPanel;
	private final JLabel lblGameType = new JLabel("Game");
	private final JCheckBox checkBoxGameType = new JCheckBox("");
	private final Component verticalStrut = Box.createVerticalStrut(20);
	private final Component horizontalStrut = Box.createHorizontalStrut(20);
	private final RadioButtonPanel panelGameType = new RadioButtonPanel();
	private final JCheckBox cbType = new JCheckBox(" Type");
	
	
	@Override
	public String getScreenName()
	{
		return "Report Setup";
	}

	@Override
	public void initialise()
	{
		//Nothing to do
	}
	
	private void createButtonGroupsAndSelectDefaults()
	{
		ComponentUtil.createButtonGroup(rdbtnDtFinish, rdbtnUnfinished);
		ComponentUtil.createButtonGroup(rdbtnYes, rdbtnNo);
	}
	private void addListeners()
	{
		comboBox.addActionListener(this);
		
		addChangeListener(cbType);
		addChangeListener(cbStartDate);
		addChangeListener(rdbtnDtFinish);
		addChangeListener(rdbtnUnfinished);
		addChangeListener(cbFinishDate);
		addChangeListener(cbPartOfMatch);
		
		btnAddIncluded.addActionListener(this);
		btnRemoveIncluded.addActionListener(this);
		btnAddExcluded.addActionListener(this);
		btnRemoveExcluded.addActionListener(this);
		
		ListSelectionModel model = scrollTableIncluded.getSelectionModel();
		model.addListSelectionListener(this);
	}
	
	private boolean valid()
	{
		if (!dateFilterPanelStart.valid())
		{
			return false;
		}
		
		if (!dateFilterPanelFinish.valid())
		{
			return false;
		}
		
		ArrayList<PlayerEntity> players = hmIncludedPlayerToPanel.getKeysAsVector();
		for (PlayerEntity player : players)
		{
			PlayerParametersPanel panel = hmIncludedPlayerToPanel.get(player);
			if (!panel.valid(player))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Add the listener to the component and fire a state-changed to get its dependents in the correct state.
	 */
	private void addChangeListener(AbstractButton btn)
	{
		btn.addChangeListener(this);
		stateChanged(new ChangeEvent(btn));
	}
	
	@Override
	public boolean showNextButton()
	{
		return true;
	}
	@Override
	public void nextPressed()
	{
		if (!valid())
		{
			return;
		}
		
		ReportingResultsScreen scrn = ScreenCache.getScreen(ReportingResultsScreen.class);
		
		ReportParameters rp = generateReportParams();
		scrn.setReportParameters(rp);
		
		ScreenCache.switchScreen(scrn);
	}
	
	private ReportParameters generateReportParams()
	{
		ReportParameters rp = new ReportParameters();
		
		int gameType = comboBox.getGameType();
		rp.setGameType(gameType);
		
		if (cbType.isSelected())
		{
			String gameParams = panelGameParams.getGameParams();
			rp.setGameParams(gameParams);
		}
		
		if (cbPartOfMatch.isSelected())
		{
			rp.setEnforceMatch(rdbtnYes.isSelected());
		}
		
		if (cbStartDate.isSelected())
		{
			Timestamp dtStartFrom = dateFilterPanelStart.getSqlDtFrom();
			Timestamp dtStartTo = dateFilterPanelStart.getSqlDtTo();
			
			rp.setDtStartFrom(dtStartFrom);
			rp.setDtStartTo(dtStartTo);
		}
		
		if (cbFinishDate.isSelected())
		{
			if (rdbtnUnfinished.isSelected())
			{
				rp.setUnfinishedOnly(true);
			}
			else
			{
				Timestamp dtFinishFrom = dateFilterPanelFinish.getSqlDtFrom();
				Timestamp dtFinishTo = dateFilterPanelFinish.getSqlDtTo();
				
				rp.setDtFinishFrom(dtFinishFrom);
				rp.setDtFinishTo(dtFinishTo);
			}
		}
		
		SuperHashMap<PlayerEntity, IncludedPlayerParameters> includedPlayers = generateIncludedPlayerParams();
		rp.setIncludedPlayers(includedPlayers);
		rp.setExcludedPlayers(excludedPlayers);
		
		return rp;
	}
	
	private SuperHashMap<PlayerEntity, IncludedPlayerParameters> generateIncludedPlayerParams()
	{
		SuperHashMap<PlayerEntity, IncludedPlayerParameters> ret = new SuperHashMap<>();
		
		ArrayList<PlayerEntity> includedPlayers = hmIncludedPlayerToPanel.getKeysAsVector();
		for (PlayerEntity player : includedPlayers)
		{
			PlayerParametersPanel panel = hmIncludedPlayerToPanel.get(player);
			IncludedPlayerParameters parms = panel.generateParameters();
			
			ret.put(player, parms);
		}
		
		return ret;
	}

	@Override
	public void stateChanged(ChangeEvent arg0)
	{
		AbstractButton src = (AbstractButton)arg0.getSource();
		boolean enabled = src.isSelected();
		
		if (src == cbType)
		{
			panelGameParams.setEnabled(cbType.isSelected());
		}
		else if (src == cbStartDate)
		{
			dateFilterPanelStart.enableComponents(enabled);
		}
		else if (src == cbFinishDate)
		{
			toggleDtFinishFilters(enabled);
		}
		else if (src == rdbtnUnfinished)
		{
			lblUnfinished.setEnabled(enabled);
		}
		else if (src == rdbtnDtFinish)
		{
			dateFilterPanelFinish.enableComponents(enabled);
		}
		else if (src == cbPartOfMatch)
		{
			rdbtnYes.setEnabled(enabled);
			rdbtnNo.setEnabled(enabled);
		}
		else
		{
			Debug.stackTrace("Unexpected stateChanged [" + src.getText() + "]");
		}
	}
	
	private void toggleDtFinishFilters(boolean enabled)
	{
		rdbtnDtFinish.setEnabled(enabled);
		rdbtnUnfinished.setEnabled(enabled);
		
		if (!enabled)
		{
			//CheckBox not enabled, so disable everything
			lblUnfinished.setEnabled(enabled);
			dateFilterPanelFinish.enableComponents(enabled);
		}
		else
		{
			lblUnfinished.setEnabled(rdbtnUnfinished.isSelected());
			dateFilterPanelFinish.enableComponents(rdbtnDtFinish.isSelected());
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		Object src = arg0.getSource();
		if (src == comboBox)
		{
			panelGame.remove(panelGameParams);
			panelGameParams = GameEntityKt.getFilterPanel(comboBox.getGameType());
			panelGameParams.setEnabled(cbType.isSelected());
			panelGame.add(panelGameParams, "cell 2 1");
			panelGame.revalidate();
		}
		else if (src == btnAddIncluded)
		{
			addPlayers(scrollTableIncluded, hmIncludedPlayerToPanel.getKeysAsVector());
		}
		else if (src == btnAddExcluded)
		{
			addPlayers(scrollTableExcluded, excludedPlayers);
		}
		else if (src == btnRemoveIncluded)
		{
			removePlayers(scrollTableIncluded, hmIncludedPlayerToPanel.getKeysAsVector());
		}
		else if (src == btnRemoveExcluded)
		{
			removePlayers(scrollTableExcluded, excludedPlayers);
		}
		else
		{
			super.actionPerformed(arg0);
		}
	}
	
	private void addPlayers(ScrollTablePlayers table, ArrayList<PlayerEntity> tableList)
	{
		ArrayList<PlayerEntity> allSelected = new ArrayList<>(hmIncludedPlayerToPanel.getKeysAsVector());
		allSelected.addAll(excludedPlayers);
		
		ArrayList<PlayerEntity> players = PlayerSelectDialog.selectPlayers(allSelected);
		if (table == scrollTableIncluded)
		{
			for (PlayerEntity player : players)
			{
				hmIncludedPlayerToPanel.put(player, new PlayerParametersPanel());
			}
		}
		
		tableList.addAll(players);
		table.initTableModel(tableList);
		table.selectFirstRow();
	}
	
	private void removePlayers(ScrollTablePlayers table, ArrayList<PlayerEntity> tableList)
	{
		ArrayList<PlayerEntity> playersToRemove = table.getSelectedPlayers();
		if (playersToRemove.isEmpty())
		{
			DialogUtil.showError("You must select player(s) to remove.");
			return;
		}
		
		tableList.removeAll(playersToRemove);
		table.initTableModel(tableList);
		
		//Bleh
		if (table == scrollTableIncluded)
		{
			for (PlayerEntity player : playersToRemove)
			{
				hmIncludedPlayerToPanel.remove(player);
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0)
	{
		panelIncludedPlayers.remove(includedPlayerPanel);
		
		PlayerEntity player = scrollTableIncluded.getSelectedPlayer();
		if (player == null)
		{
			includedPlayerPanel = defaultIncludedPlayerPanel;
		}
		else
		{
			includedPlayerPanel = hmIncludedPlayerToPanel.get(player);
		}
		
		panelIncludedPlayers.add(includedPlayerPanel, BorderLayout.SOUTH);
		
		ScreenCache.getMainScreen().pack();
		repaint();
	}
}
