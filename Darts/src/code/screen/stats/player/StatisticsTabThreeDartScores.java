package code.screen.stats.player;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import bean.NumberField;
import bean.RowSelectionListener;
import bean.ScrollTable;
import code.bean.ScrollTableDartsGame;
import code.bean.X01ScoreRenderer;
import code.stats.GameWrapper;
import code.stats.ThreeDartScoreWrapper;
import object.HandyArrayList;
import object.SuperHashMap;
import util.TableUtil.DefaultModel;
import util.TableUtil.SimpleRenderer;

public class StatisticsTabThreeDartScores extends AbstractStatisticsTab
										  implements RowSelectionListener
{
	public StatisticsTabThreeDartScores() 
	{
		nfScoreThreshold.setColumns(10);
		setLayout(new BorderLayout(0, 0));
		
		
		add(panelTables, BorderLayout.CENTER);
		panelTables.setLayout(new GridLayout(2, 2, 0, 0));
		panelTables.add(tableScoresMine);
		
		tableScoresOther.setTableForeground(Color.RED);
		
		panelTables.add(tableScoresOther);
		panelTables.add(tableBreakdownMine);
		panelTables.add(tableBreakdownOther);
		
		tableBreakdownOther.setTableForeground(Color.RED);
		
		add(panelConfig, BorderLayout.NORTH);
		
		panelConfig.add(lblScoreThreshold);
		
		panelConfig.add(nfScoreThreshold);
		nfScoreThreshold.setValue(140);
		tableScoresMine.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableScoresOther.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		nfScoreThreshold.addPropertyChangeListener(this);
		
		tableScoresMine.addRowSelectionListener(this);
		tableScoresOther.addRowSelectionListener(this);
	}
	
	private final JPanel panelTables = new JPanel();
	private final ScrollTableDartsGame tableBreakdownMine = new ScrollTableDartsGame("Example Game");
	private final ScrollTableDartsGame tableBreakdownOther = new ScrollTableDartsGame("Example Game");
	private final JPanel panelConfig = new JPanel();
	private final JLabel lblScoreThreshold = new JLabel("Score Threshold");
	private final NumberField nfScoreThreshold = new NumberField(62, 300);
	private final ScrollTable tableScoresMine = new ScrollTable();
	private final ScrollTable tableScoresOther = new ScrollTable();

	@Override
	public void populateStats()
	{
		setComponentVisibility();
		
		populateTable(tableScoresMine, filteredGames);
		if (includeOtherComparison())
		{
			populateTable(tableScoresOther, filteredGamesOther);
		}
		
	}
	private void setComponentVisibility()
	{
		panelTables.removeAll();
		
		if (includeOtherComparison())
		{
			panelTables.setLayout(new GridLayout(2, 2, 0, 0));
			panelTables.add(tableScoresMine);
			panelTables.add(tableScoresOther);
			panelTables.add(tableBreakdownMine);
			panelTables.add(tableBreakdownOther);
		}
		else
		{
			panelTables.setLayout(new GridLayout(2, 1, 0, 0));
			panelTables.add(tableScoresMine);
			panelTables.add(tableBreakdownMine);
		}
	}
	private void populateTable(ScrollTable table, HandyArrayList<GameWrapper> filteredGames)
	{
		//Sort by start date
		filteredGames.sort((GameWrapper g1, GameWrapper g2) -> g1.compareStartDate(g2));
		
		//Build up two maps, one of score to count (e.g. 20, 5, 1 -> 10) and the other of score to example game
		SuperHashMap<Integer, ThreeDartScoreWrapper> hmScoreToThreeDartBreakdown = new SuperHashMap<>();
		for (GameWrapper game : filteredGames)
		{
			game.populateThreeDartScoreMap(hmScoreToThreeDartBreakdown, nfScoreThreshold.getNumber());
		}
		
		DefaultModel model = new DefaultModel();
		model.addColumn("Score");
		model.addColumn("Count");
		model.addColumn("!Wrapper");
		
		ArrayList<Integer> scores = hmScoreToThreeDartBreakdown.getKeysAsVector();
		for (int i=0; i<scores.size(); i++)
		{
			int score = scores.get(i);
			ThreeDartScoreWrapper wrapper = hmScoreToThreeDartBreakdown.get(score);
			int totalCount = wrapper.getTotalCount();
			
			Object[] row = {score, totalCount, wrapper};
			model.addRow(row);
		}
		
		table.setModel(model);
		
		table.setRenderer(0, new X01ScoreRenderer());
		table.setRenderer(1, new SimpleRenderer(SwingConstants.LEFT, null));
		
		table.selectFirstRow();
		
		table.removeColumn(2);
		table.sortBy(0, false);
	}
	
	
	private void populateBreakdownTable(ScrollTableDartsGame table, ThreeDartScoreWrapper wrapper)
	{
		DefaultModel model = new DefaultModel();
		model.addColumn("Method");
		model.addColumn("Count");
		model.addColumn("Example Game");
		
		ArrayList<Object[]> rows = wrapper.getRows();
		for (Object[] row : rows)
		{
			model.addRow(row);
		}
		
		table.setModel(model);
		
		table.setRenderer(1, new SimpleRenderer(SwingConstants.LEFT, null));
		
		
		Object[] footerRow = {"Total", wrapper.getTotalCount(), "-"};
		table.addFooterRow(footerRow);
		
		table.sortBy(1, true);
	}
	
	@Override
	public void selectionChanged(ScrollTable src)
	{
		int selectedRow = src.getSelectedModelRow();
		if (selectedRow == -1)
		{
			return;
		}
		
		ThreeDartScoreWrapper wrapper = (ThreeDartScoreWrapper)src.getValueAt(selectedRow, 2);
		
		if (src == tableScoresMine)
		{
			populateBreakdownTable(tableBreakdownMine, wrapper);
		}
		else if (src == tableScoresOther)
		{
			populateBreakdownTable(tableBreakdownOther, wrapper);
		}
	}
}
