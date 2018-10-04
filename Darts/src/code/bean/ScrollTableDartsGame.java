package code.bean;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import bean.HyperlinkAdaptor;
import bean.HyperlinkListener;
import bean.ScrollTable;
import code.screen.game.DartsGameScreen;
import util.DialogUtil;

/**
 * A scroll table with a 'Game' column. 
 * Handles rendering this column as a hyperlink to launch it on click.
 */
public class ScrollTableDartsGame extends ScrollTable
							 	  implements HyperlinkListener
{
	private static final String COLUMN_NAME_GAME_ID = "Game";
	
	private String gameColumnName = null;
	private int gameColumn = -1;
	
	private HyperlinkAdaptor adaptor = null;
	
	public ScrollTableDartsGame()
	{
		super();
	}
	public ScrollTableDartsGame(String gameColumnName)
	{
		super();
		
		this.gameColumnName = gameColumnName;
	}

	@Override
	public void setModel(DefaultTableModel model)
	{
		super.setModel(model);
		
		int columnCount = model.getColumnCount();
		for (int i=0; i<columnCount; i++)
		{
			String columnName = model.getColumnName(i);
			if (columnName.equals(getGameColumnName()))
			{
				gameColumn = i;
				setRenderer(gameColumn, new HyperlinkRenderer(getTableForeground()));
			}
		}
		
		//Init the adaptor if we need to, but only once
		if (adaptor == null)
		{
			adaptor = new HyperlinkAdaptor(this);
			
			table.addMouseListener(adaptor);
			table.addMouseMotionListener(adaptor);
		}
		
	}
	private String getGameColumnName()
	{
		if (gameColumnName != null)
		{
			return gameColumnName;
		}
		
		return COLUMN_NAME_GAME_ID;
	}
	
	/**
	 * Allow direct setting of the game column index, so I can show game hyperlinks within the DartsScorers
	 */
	public void setGameColumnIndex(int ix)
	{
		gameColumn = ix;
		setRenderer(gameColumn, new HyperlinkRenderer(getTableForeground()));
	}

	@Override
	public boolean isOverHyperlink(MouseEvent arg0)
	{
		Point pt = arg0.getPoint();
		int col = table.columnAtPoint(pt);
		if (col != gameColumn)
		{
			return false;
		}
		
		int row = table.rowAtPoint(pt);
		if (row == -1)
		{
			return false;
		}
		
		int actualRow = table.convertRowIndexToModel(row);
		Object val = table.getValueAt(actualRow, col);
		return (val instanceof Long);
	}
	
	@Override
	public void linkClicked(MouseEvent arg0)
	{
		if (!isOverHyperlink(arg0))
		{
			return;
		}
		
		Point pt = arg0.getPoint();
		int col = table.columnAtPoint(pt);
		int row = table.rowAtPoint(pt);
		int actualRow = table.convertRowIndexToModel(row);
		long gameId = (long)table.getModel().getValueAt(actualRow, col);
		
		if (gameId > 0)
		{
			DartsGameScreen.loadAndDisplayGame(gameId);
		}
		else
		{
			DialogUtil.showError("It isn't possible to display individual games from a simulation.");
		}
	}
	
	@Override
	public void setCursor(Cursor arg0)
	{
		super.setCursor(arg0);
		table.setCursor(arg0);
	}
	
	private static final class HyperlinkRenderer extends DefaultTableCellRenderer
	{
		private Color fgColor = null;
		
		public HyperlinkRenderer(Color color)
		{
			if (color != null)
			{
				this.fgColor = color;
			}
			else
			{
				this.fgColor = Color.BLUE;
			}
		}
		
		@Override
	    public Component getTableCellRendererComponent(JTable table, Object
	        value, boolean isSelected, boolean hasFocus, int row, int column) 
        {
			if (!(value instanceof Long))
			{
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				return this;
			}
			
			super.getTableCellRendererComponent(table, "#" + value, isSelected, hasFocus, row, column);
			
			Map<TextAttribute, Integer> fontAttributes = new HashMap<>();
			fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			Font hyperlinkFont = new Font("Tahoma", Font.BOLD, 12).deriveFont(fontAttributes);
			
			setFont(hyperlinkFont);
			
			if (isSelected)
			{
				setForeground(Color.WHITE);
			}
			else
			{
				setForeground(fgColor);
			}
			
			return this;
        }
	}
}
