package burlton.dartzee.code.screen.stats.player;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.sql.Timestamp;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import burlton.desktopcore.code.bean.DateFilterPanel;
import burlton.dartzee.code.bean.GameParamFilterPanel;
import burlton.dartzee.code.db.GameEntity;
import burlton.dartzee.code.screen.ScreenCache;
import burlton.dartzee.code.stats.GameWrapper;
import burlton.desktopcore.code.screen.SimpleDialog;


public class PlayerStatisticsFilterDialog extends SimpleDialog
										  implements ChangeListener
{
	private String gameParams = "";
	private boolean filterByDate = false;
	
	private GameParamFilterPanel filterPanel = null;
	
	public PlayerStatisticsFilterDialog(int gameType)
	{
		filterPanel = GameEntity.getFilterPanel(gameType);
		
		setTitle("Filters");
		setSize(473, 200);
		setModal(true);
		
		JPanel panelFilters = new JPanel();
		getContentPane().add(panelFilters, BorderLayout.CENTER);
		panelFilters.setLayout(new GridLayout(0, 1, 0, 0));
		
		panelFilters.add(panelStartingScore);
		panelStartingScore.add(cbType);
		panelStartingScore.add(filterPanel);
		
		panelFilters.add(panelDate);
		
		panelDate.add(chckbxDatePlayed);
		
		panelDate.add(dateFilter);
		
		cbType.addChangeListener(this);
		chckbxDatePlayed.addChangeListener(this);
	}
	
	private final JCheckBox cbType = new JCheckBox("Type");
	private final JPanel panelStartingScore = new JPanel();
	private final JPanel panelDate = new JPanel();
	private final JCheckBox chckbxDatePlayed = new JCheckBox("Date");
	private final DateFilterPanel dateFilter = new DateFilterPanel();
	
	public void resetFilters()
	{
		gameParams = "";
		filterByDate = false;
	}
	
	private void saveState()
	{
		if (cbType.isSelected())
		{
			gameParams = filterPanel.getGameParams();
		}
		else
		{
			gameParams = "";
		}
		
		filterByDate = chckbxDatePlayed.isSelected();
	}
	
	public void refresh()
	{
		cbType.setSelected(!gameParams.isEmpty());
		filterPanel.setEnabled(cbType.isSelected());
		
		if (!gameParams.isEmpty())
		{
			filterPanel.setGameParams(gameParams);
		}
		
		chckbxDatePlayed.setSelected(filterByDate);
		dateFilter.enableComponents(chckbxDatePlayed.isSelected());
	}
	
	private boolean valid()
	{
		if (!dateFilter.valid())
		{
			return false;
		}
		
		return true;
	}
	
	public boolean includeGameBasedOnFilters(GameWrapper game)
	{
		String gameParamsToCheck = game.getGameParams();
		if (!gameParams.isEmpty()
		  && !gameParamsToCheck.equals(gameParams))
		{
			return false;
		}
		
		//Date filter
		if (filterByDate)
		{
			Timestamp dtStart = game.getDtStart();
			if (!dateFilter.filterSqlDate(dtStart))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public String getFiltersDesc()
	{
		String desc = "Showing ";
		if (gameParams.isEmpty())
		{
			desc += "all games";
		}
		else
		{
			desc += filterPanel.getFilterDesc();
		}
		
		return desc;
	}
	
	public String getDateDesc()
	{
		if (filterByDate)
		{
			return dateFilter.getDtFromStr() + " - " + dateFilter.getDtToStr();
		}
		
		return "";
	}
	
	@Override
	public void okPressed()
	{
		if (valid())
		{
			saveState();
			dispose();
			
			PlayerStatisticsScreen scrn = ScreenCache.getScreen(PlayerStatisticsScreen.class);
			scrn.buildTabs();
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent arg0)
	{
		Component src = (Component)arg0.getSource();
		if (src == cbType)
		{
			filterPanel.setEnabled(cbType.isSelected());
		}
		else if (src == chckbxDatePlayed)
		{
			dateFilter.enableComponents(chckbxDatePlayed.isSelected());
		}
	}
}
