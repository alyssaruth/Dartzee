package burlton.dartzee.code.screen.stats.player;

import burlton.core.code.obj.HandyArrayList;
import burlton.core.code.util.StringUtil;
import burlton.dartzee.code.bean.ScrollTableDartsGame;
import burlton.dartzee.code.object.Dart;
import burlton.dartzee.code.stats.GameWrapper;
import burlton.desktopcore.code.util.TableUtil;
import burlton.desktopcore.code.util.TableUtil.SimpleRenderer;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StatisticsTabTopFinishes extends AbstractStatisticsTab
{
	private static final int MAX_FINISHES_TO_SHOW = 25;
	
	public StatisticsTabTopFinishes()
	{
		super();
		
		setLayout(new GridLayout(0, 2, 0, 0));
		
		add(tableTopFinishesMine);
		add(tableTopFinishesOther);
		
		tableTopFinishesOther.setTableForeground(Color.RED);
	}
	
	private final ScrollTableDartsGame tableTopFinishesMine = new ScrollTableDartsGame();
	private final ScrollTableDartsGame tableTopFinishesOther = new ScrollTableDartsGame();
	
	@Override
	public void populateStats()
	{
		setOtherComponentVisibility(this, tableTopFinishesOther);
		
		buildTopFinishesTable(filteredGames, tableTopFinishesMine);
		if (includeOtherComparison())
		{
			buildTopFinishesTable(filteredGamesOther, tableTopFinishesOther);
		}
	}
	
	private void buildTopFinishesTable(HandyArrayList<GameWrapper> games, ScrollTableDartsGame table)
	{
		TableUtil.DefaultModel model = new TableUtil.DefaultModel();
		model.addColumn("Finish");
		model.addColumn("Darts");
		model.addColumn("Game");
		
		//Sort by checkout total. 
		games.sort((GameWrapper g1, GameWrapper g2) 
							-> Integer.compare(g2.getCheckoutTotal(), g1.getCheckoutTotal()));
		
		int listSize = Math.min(MAX_FINISHES_TO_SHOW, games.size());
		for (int i=0; i<listSize; i++)
		{
			GameWrapper game = games.get(i);
			if (!game.isFinished())
			{
				continue;
			}
			
			long gameId = game.getGameId();
			int total = game.getCheckoutTotal();
			
			List<Dart> darts = game.getDartsForFinalRound();
			String dartStr = StringUtil.toDelims(darts, ", ");
			
			Object[] row = {total, dartStr, gameId};
			model.addRow(row);
		}
		
		table.setModel(model);
		table.setRenderer(0, new SimpleRenderer(SwingConstants.LEFT, null));
		table.sortBy(0, true);
	}
}
