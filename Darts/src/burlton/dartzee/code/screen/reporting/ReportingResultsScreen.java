package burlton.dartzee.code.screen.reporting;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

import burlton.dartzee.code.bean.ScrollTableDartsGame;
import burlton.dartzee.code.reporting.ReportParameters;
import burlton.dartzee.code.reporting.ReportResultWrapper;
import burlton.dartzee.code.reporting.ReportingSqlUtil;
import burlton.dartzee.code.screen.EmbeddedScreen;
import burlton.dartzee.code.screen.ScreenCache;
import burlton.desktopcore.code.util.TableUtil;

public class ReportingResultsScreen extends EmbeddedScreen
{
	private ReportParameters rp = null;
	private ArrayList<Object[]> cachedRows = null;
	
	public ReportingResultsScreen() 
	{
		add(tableResults, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		
		
		panel.add(btnConfigureColumns);
		btnConfigureColumns.addActionListener(this);
	}
	
	private final JButton btnConfigureColumns = new JButton("Configure Columns...");
	private final ScrollTableDartsGame tableResults = new ScrollTableDartsGame();

	@Override
	public String getScreenName()
	{
		return "Game Report";
	}

	@Override
	public void initialise()
	{
		buildTable(true);
	}

	private void buildTable(boolean runSql)
	{
		TableUtil.DefaultModel model = new TableUtil.DefaultModel();
		model.addColumn("Game");
		model.addColumn("Type");
		model.addColumn("Players");
		model.addColumn("Start Date");
		model.addColumn("Finish Date");
		model.addColumn("Match");
		
		if (runSql)
		{
			ArrayList<ReportResultWrapper> wrappers = ReportingSqlUtil.runReport(rp);
			cachedRows = ReportResultWrapper.getTableRowsFromWrappers(wrappers);
		}
		
		for (Object[] row : cachedRows)
		{
			model.addRow(row);
		}
		
		tableResults.setRowName("game");
		tableResults.setModel(model);
		tableResults.setColumnWidths("60;160;-1;DT;DT;100");
		tableResults.sortBy(0, false);
		
		setRenderersAndComparators();
		stripOutRemovedColumns();
	}
	private void setRenderersAndComparators()
	{
		tableResults.setRenderer(3, TableUtil.TIMESTAMP_RENDERER);
		tableResults.setRenderer(4, TableUtil.TIMESTAMP_RENDERER);
		
		tableResults.setComparator(3, (Timestamp t1, Timestamp t2) -> t1.compareTo(t2));
		tableResults.setComparator(3, (Timestamp t1, Timestamp t2) -> t1.compareTo(t2));
	}
	private void stripOutRemovedColumns()
	{
		ConfigureReportColumnsDialog dlg = ScreenCache.getConfigureReportColumnsDialog();
		
		int columns = tableResults.getColumnCount();
		for (int i=columns-1; i>=0; i--)
		{
			String columnName = tableResults.getColumnName(i);
			if (!dlg.includeColumn(columnName))
			{
				tableResults.removeColumn(i);
			}
		}
	}
	
	/**
	 * Re-written, use wrapper class
	 */
	/*private ArrayList<Object[]> retrieveGames(String sql)
	{
		SuperHashMap<Long, Object[]> hm = new SuperHashMap<>();
		try (ResultSet rs = DatabaseUtil.executeQuery(sql))
		{
			while (rs.next())
			{
				Long gameId = rs.getLong(1);
				int gameType = rs.getInt(2);
				String gameParams = rs.getString(3);
				Timestamp dtStart = rs.getTimestamp(4);
				Timestamp dtFinish = rs.getTimestamp(5);
				String playerName = rs.getString(6);
				int finishPos = rs.getInt(7);
				long matchId = rs.getLong(8);
				int matchOrdinal = rs.getInt(9);
				
				String gameTypeDesc = GameEntity.getTypeDesc(gameType, gameParams);
				String playerDesc = playerName + " (" + transformFinishPos(finishPos) + ")";
				
				String matchDesc = "";
				if (matchId > -1)
				{
					matchDesc = "#" + matchId + " (Game " + (matchOrdinal+1) + ")";
				}
				
				Object[] existingRow = hm.get(gameId);
				if (existingRow == null)
				{
					Object[] row = {gameId, gameTypeDesc, playerDesc, dtStart, dtFinish, matchDesc};
					hm.put(gameId, row);
				}
				else
				{
					existingRow[2] = existingRow[2] + ", " + playerDesc;
				}
			}
		}
		catch (SQLException sqle)
		{
			Debug.logSqlException(sql, sqle);
			DialogUtil.showError("SQL failed for report parameters.");
			
			//return an empty hashmap so we show an empty table
			hm.clear();
		}
		
		return hm.getValuesAsVector();
	}*/
	
	public void setReportParameters(ReportParameters rp)
	{
		this.rp = rp;
	}
	
	@Override
	public EmbeddedScreen getBackTarget()
	{
		return ScreenCache.getScreen(ReportingSetupScreen.class);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (arg0.getSource() == btnConfigureColumns)
		{
			ConfigureReportColumnsDialog dlg = ScreenCache.getConfigureReportColumnsDialog();
			dlg.setLocationRelativeTo(this);
			dlg.setVisible(true);
			
			buildTable(false);
		}
		else
		{
			super.actionPerformed(arg0);
		}
	}	
}
