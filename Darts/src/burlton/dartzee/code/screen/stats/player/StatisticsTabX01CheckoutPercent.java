package burlton.dartzee.code.screen.stats.player;

import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.obj.HashMapList;
import burlton.core.code.util.MathsUtil;
import burlton.dartzee.code.object.Dart;
import burlton.dartzee.code.stats.GameWrapper;
import burlton.dartzee.code.utils.X01UtilKt;
import burlton.desktopcore.code.bean.ScrollTable;
import burlton.desktopcore.code.util.TableUtil.DefaultModel;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * Checkout percentages for X01
 */
public final class StatisticsTabX01CheckoutPercent extends AbstractStatisticsTab
{

	public StatisticsTabX01CheckoutPercent()
	{
		setLayout(new GridLayout(0, 2, 0, 0));
		
		
		add(panelMine);
		panelMine.setLayout(new BorderLayout(0, 0));
		panelMine.add(tableMine, BorderLayout.CENTER);
		add(panelOther);
		panelOther.setLayout(new BorderLayout(0, 0));
		panelOther.add(tableOther, BorderLayout.CENTER);
		tableOther.setTableForeground(Color.RED);
	}
	
	private final JPanel panelMine = new JPanel();
	private final ScrollTable tableMine = new ScrollTable();
	private final JPanel panelOther = new JPanel();
	private final ScrollTable tableOther = new ScrollTable();
	

	@Override
	public void populateStats()
	{
		setOtherComponentVisibility(this, panelOther);
		
		populateTable(tableMine, filteredGames);
		if (includeOtherComparison())
		{
			populateTable(tableOther, filteredGamesOther);
		}
		
	}
	private void populateTable(ScrollTable table, HandyArrayList<GameWrapper> games)
	{
		HashMapList<Integer, Dart> hmDoubleToDartsThrown = new HashMapList<>();
		for (GameWrapper g : games)
		{
			addDartsToHashMap(g, hmDoubleToDartsThrown);
		}
		
		DefaultModel model = new DefaultModel();
		model.addColumn("Double");
		model.addColumn("Opportunities");
		model.addColumn("Hits");
		model.addColumn("Checkout %");
		
		int totalOpportunities = 0;
		int totalHits = 0;
		for (int i=2; i<=50; i+=2)
		{
			if (i > 40
			  && i < 50)
			{
				continue;
			}
			
			Collection<Dart> darts = hmDoubleToDartsThrown.get(i);
			if (darts == null)
			{
				Object[] row = {i/2, 0, 0, 0};
				model.addRow(row);
				continue;
			}
			
			int opportunities = darts.size();
			int hits = 0;
			for (Dart drt : darts)
			{
				if (drt.isDouble()
				  && drt.getTotal() == i)
				{
					hits++;
				}
			}
			
			Object[] row = {i/2, opportunities, hits, MathsUtil.getPercentage(hits, opportunities)};
			model.addRow(row);
			
			totalOpportunities += opportunities;
			totalHits += hits;
		}
		
		table.setModel(model);
		
		Object[] totalsRow = {"", totalOpportunities, totalHits, MathsUtil.getPercentage(totalHits, totalOpportunities)};
		table.addFooterRow(totalsRow);
	}
	private void addDartsToHashMap(GameWrapper g, HashMapList<Integer, Dart> hmDoubleToDartsThrown)
	{
		Collection<Dart> darts = g.getAllDarts();
		for (Dart drt : darts)
		{
			if (X01UtilKt.isCheckoutDart(drt))
			{	
				int startingScore = drt.getStartingScore();
				hmDoubleToDartsThrown.putInList(startingScore, drt);
			}
		}
	}

}
