package burlton.dartzee.code.screen.reporting;
import burlton.dartzee.code.bean.ScrollTableDartsGame;
import burlton.dartzee.code.reporting.ReportParameters;
import burlton.dartzee.code.reporting.ReportResultWrapper;
import burlton.dartzee.code.reporting.ReportingSqlUtil;
import burlton.dartzee.code.screen.EmbeddedScreen;
import burlton.dartzee.code.screen.ScreenCache;
import burlton.desktopcore.code.util.TableUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;
import java.util.ArrayList;

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
