package code.screen.game;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import code.bean.ScrollTableDartsGame;
import code.db.DartsMatchEntity;
import code.db.GameEntity;
import code.db.ParticipantEntity;
import code.db.PlayerEntity;
import object.HandyArrayList;
import object.SuperHashMap;
import util.Debug;
import util.TableUtil.DefaultModel;

public class MatchSummaryPanel extends JPanel
{
	private static final int COLUMN_NO_ORDINAL = 0;
	private static final int COLUMN_NO_GAME_ID = 1;
	
	private SuperHashMap<String, Integer> hmPlayerNameToColumnNo = new SuperHashMap<>();
	private HandyArrayList<PlayerEntity> players = null;
	private DartsMatchEntity match = null;
	
	public MatchSummaryPanel() 
	{
		setLayout(new BorderLayout(0, 0));
		add(tableResults, BorderLayout.CENTER);
	}
	
	private final ScrollTableDartsGame tableResults = new ScrollTableDartsGame();
	
	public void init(DartsMatchEntity match)
	{
		this.players = new HandyArrayList<>(match.getPlayers());
		this.match = match;
		
		DefaultModel model = new DefaultModel();
		model.addColumn("#");
		model.addColumn("Game");
		
		int playerCount = players.size();
		for (int i=0; i<playerCount; i++)
		{
			PlayerEntity player = players.get(i);
			String playerName = player.getName();
			model.addColumn(playerName);
			
			int columnIx = model.getColumnCount() - 1;
			hmPlayerNameToColumnNo.put(playerName, columnIx);
		}
		
		tableResults.setModel(model);
		
		for (int i=COLUMN_NO_GAME_ID + 1; i<model.getColumnCount(); i++)
		{
			tableResults.getColumn(i).setCellRenderer(new ParticipantRenderer());
		}
	}
	public void addGame(GameEntity game)
	{
		DefaultModel model = (DefaultModel)tableResults.getModel();
		
		Object[] row = new Object[players.size() + 2];
		row[COLUMN_NO_ORDINAL] = game.getMatchOrdinal();
		row[COLUMN_NO_GAME_ID] = game.getRowId();
		
		model.addRow(row);
	}
	
	public void addParticipant(long gameId, ParticipantEntity participant)
	{
		DefaultModel model = (DefaultModel)tableResults.getModel();
		for (int row=0; row<model.getRowCount(); row++)
		{
			long rowGameId = (long)model.getValueAt(row, COLUMN_NO_GAME_ID);
			if (gameId != rowGameId)
			{
				continue;
			}
			
			String playerName = participant.getPlayerName();
			int col = hmPlayerNameToColumnNo.get(playerName);
			model.setValueAt(participant, row, col);
		}
	}
	
	private final class ParticipantRenderer extends DefaultTableCellRenderer
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Object newValue = getReplacementValue(value, row);
			super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column);
			
			setHorizontalAlignment(SwingConstants.CENTER);
    		setFont(new Font("Trebuchet MS", Font.BOLD, 15));
    		
    		return this;
		}
		
		public Object getReplacementValue(Object obj, int row)
		{
			if (obj == null)
			{
				return "???";
			}
			
			if (!(obj instanceof ParticipantEntity))
			{
				Debug.stackTrace("Unexpected element in table at row [" + row + "]. Object is [" + obj + "]");
				return null;
			}
			
			ParticipantEntity pt = (ParticipantEntity)obj;
			int finishPos = pt.getFinishingPosition();
			if (finishPos == -1)
			{
				return "-";
			}
			
			int score = match.getScoreForFinishingPosition(finishPos);
			return score + " (" + finishPos + ")";
		}
	}
}
