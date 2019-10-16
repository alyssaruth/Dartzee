package burlton.desktopcore.code.bean;

import burlton.core.code.util.StringUtil;
import burlton.desktopcore.code.util.TableUtil.DefaultModel;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Comparator;

public class ScrollTable extends JPanel
						 implements TableColumnModelListener, ListSelectionListener, MouseListener
{
	private static final int TABLE_ROW_FOOTER = -2;
	private static final String COL_WIDTH_STRING_DT = "DT";
	private static final int COL_WIDTH_DT = 115;
	
	private String rowNameSingular = "row";
	private String rowNamePlural = null;
	
	private Color fgColor = null;
	
	private boolean sortingEnabled = true;
	
	private TableRowSorter<TableModel> sorter = null;
	private ArrayList<RowSelectionListener> listeners = new ArrayList<>();
	private ArrayList<DoubleClickListener> clickListeners = new ArrayList<>();
	
	public ScrollTable() 
	{
		setLayout(new BorderLayout(0, 0));
		
		getColumnModel().addColumnModelListener(this);
		
		table.addMouseListener(this);
		
		table.getSelectionModel().addListSelectionListener(this);
		tableFooter.getSelectionModel().addListSelectionListener(this);
		
		add(panelCenter, BorderLayout.CENTER);
		panelCenter.setLayout(new BorderLayout(0, 0));
		panelCenter.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setViewportView(table);
		table.getTableHeader().setReorderingAllowed(false);
		table.setFillsViewportHeight(true);
		panelCenter.add(panelRowCount, BorderLayout.SOUTH);
		panelRowCount.setLayout(new BorderLayout(0, 0));
		
		panelRowCount.add(tableFooter, BorderLayout.CENTER);
		panelRowCount.add(lblRowCount, BorderLayout.SOUTH);
		lblRowCount.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRowCount.setBorder(new EmptyBorder(0, 10, 0, 10));
		
		tableFooter.getTableHeader().setReorderingAllowed(false);
		tableFooter.setFillsViewportHeight(true);
		tableFooter.setVisible(false);
		
		
		Font boldFont = tableFooter.getFont().deriveFont(Font.BOLD);
		tableFooter.setFont(boldFont);
		
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, WHEN_FOCUSED), "Down");
		table.getActionMap().put("Down", new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int row = table.getSelectedRow();
				if (row == getRowCount() - 1
				  && tableFooter.isVisible())
				{
					selectRow(TABLE_ROW_FOOTER);
					tableFooter.requestFocus();
				}
				else if (row < getRowCount() - 1)
				{
					selectRow(row + 1);
				}
			}
		});
		
		tableFooter.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, WHEN_FOCUSED), "Up");
		tableFooter.getActionMap().put("Up", new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				selectRow(table.getRowCount() - 1);
				table.requestFocus();
			}
		});
	}
	
	private final JScrollPane scrollPane = new JScrollPane();
	public final JTable table = new JTable()
	{
		@Override
		public boolean isCellEditable(int arg0, int arg1)
		{
			return isEditable(arg0, arg1);
		}
	};
	
	private final JLabel lblRowCount = new JLabel("<Row Count>");
	private final JPanel panelRowCount = new JPanel();
	private final JTable tableFooter = new JTable()
	{
		@Override
		public boolean isCellEditable(int row, int column) { return false; }
	};
	private final JPanel panelCenter = new JPanel();
	
	public DefaultTableModel getModel()
	{
		return (DefaultTableModel)table.getModel();
	}
	public void setModel(DefaultTableModel model)
	{
		table.setModel(model);
		
		if (sortingEnabled)
		{
			sorter = new TableRowSorter<>(model);
			table.setRowSorter(sorter);
		}
		
		//Initialise our footer model in preparation
		DefaultModel footerModel = new DefaultModel();
		for (int i=0; i<model.getColumnCount(); i++)
		{
			footerModel.addColumn("");
		}
		
		tableFooter.setModel(footerModel);
		
		refreshRowCount();
	}
	public void addRow(Object[] row)
	{
		DefaultTableModel model = getModel();
		model.addRow(row);
		
		refreshRowCount();
	}
	public void insertRow(Object[] row, int index)
	{
		DefaultTableModel model = getModel();
		model.insertRow(index, row);
		refreshRowCount();
	}
	public void addColumn(String columnName)
	{
		DefaultTableModel model = getModel();
		model.addColumn(columnName);
		
		DefaultTableModel footerModel = (DefaultTableModel)tableFooter.getModel();
		footerModel.addColumn(columnName);
	}
	
	private void refreshRowCount()
	{
		int rows = table.getRowCount();
		String rowCountDesc = getRowCountDesc(rows);
		lblRowCount.setText(rowCountDesc);
	}
	private String getRowCountDesc(int rows)
	{
		if (rows == 1)
		{
			return rows + " " + rowNameSingular;
		}
		
		String rowName = rowNamePlural;
		if (rowName == null)
		{
			rowName = rowNameSingular + "s";
		}
		
		return rows + " " + rowName;
	}
	
	public void setRowName(String rowNameSingular)
	{
		this.rowNameSingular = rowNameSingular;
	}
	public void setRowName(String rowNameSingular, String rowNamePlural)
	{
		this.rowNameSingular = rowNameSingular;
		this.rowNamePlural = rowNamePlural;
	}
	public void setShowRowCount(boolean show)
	{
		panelRowCount.setVisible(show);
	}
	public void setRowCountAlignment(int alignment)
	{
		lblRowCount.setHorizontalAlignment(alignment);
	}
	
	/**
	 * Pass-throughs to the table
	 */
	public TableRowSorter<TableModel> getRowSorter()
	{
		return sorter;
	}
	public int getRowCount()
	{
		return table.getRowCount();
	}
	public int getColumnCount()
	{
		return table.getColumnCount();
	}
	public String getColumnName(int colIndex)
	{
		return table.getColumnName(colIndex);
	}
	public ListSelectionModel getSelectionModel()
	{
		return table.getSelectionModel();
	}
	public void setSelectionMode(int mode)
	{
		table.setSelectionMode(mode);
	}
	public void setRowHeight(int height)
	{
		table.setRowHeight(height);
	}
	public Object getValueAt(int row, int col)
	{
		if (row == TABLE_ROW_FOOTER)
		{
			return tableFooter.getValueAt(0, col);
		}
		
		return getModel().getValueAt(row, col);
	}
	public void setPreferredScrollableViewportSize(Dimension dim)
	{
		table.setPreferredScrollableViewportSize(dim);
	}
	public void setTableForeground(Color color)
	{
		fgColor = color;
		
		table.setForeground(color);
		tableFooter.setForeground(color);
	}
	public Color getTableForeground()
	{
		return fgColor;
	}
	public void setFillsViewportHeight(boolean fill)
	{
		table.setFillsViewportHeight(fill);
	}
	public TableColumnModel getColumnModel()
	{
		return table.getColumnModel();
	}
	public boolean isEditing()
	{
		return table.isEditing();
	}
	public TableCellEditor getCellEditor()
	{
		return table.getCellEditor();
	}
	public int getEditingRow()
	{
		return table.getEditingRow();
	}
	public int convertRowIndexToModel(int viewRowIndex)
	{
		return table.convertRowIndexToModel(viewRowIndex);
	}
	
	/**
	 * Helpers
	 */
	public void sortBy(int columnIndex, boolean desc)
	{
		sorter.toggleSortOrder(columnIndex);
		
		if (desc)
		{
			sorter.toggleSortOrder(columnIndex);
		}
	}
	public void setRenderer(int columnIndex, TableCellRenderer renderer)
	{
		TableColumn column = getColumn(columnIndex);
		column.setCellRenderer(renderer);
		
		TableColumn footerColumn = tableFooter.getColumnModel().getColumn(columnIndex);
		footerColumn.setCellRenderer(renderer);
	}
	public <T> void setComparator(int columnIndex, Comparator<T> comp)
	{
		sorter.setComparator(columnIndex, comp);
	}
	public void setColumnWidths(String colStr)
	{
		ArrayList<String> columnWidths = StringUtil.getListFromDelims(colStr, ";");
		for (int i=0; i<columnWidths.size(); i++)
		{
			String colWidthStr = columnWidths.get(i);
			int colWidth = getColWidthForString(colWidthStr);
			if (colWidth == -1)
			{
				//Deliberately not setting this one
				continue;
			}
			
			
			TableColumn column = getColumn(i);
			setWidthForColumn(column, colWidth);
			
			//Ensure we set the widths on the footer row as well
			TableColumn footerCol = tableFooter.getColumnModel().getColumn(i);
			setWidthForColumn(footerCol, colWidth);
		}
		
	}
	private void setWidthForColumn(TableColumn column, int colWidth)
	{
		column.setPreferredWidth(colWidth);
		column.setMaxWidth(colWidth);
	}
	private int getColWidthForString(String colWidthStr)
	{
		if (colWidthStr.equals(COL_WIDTH_STRING_DT))
		{
			return COL_WIDTH_DT;
		}
		
		return Integer.parseInt(colWidthStr);
	}
	public void disableSorting()
    {
        table.setRowSorter(null);
        sortingEnabled = false;
    }
	
	public void removeColumn(int colIx)
	{
		TableColumn col = getColumn(colIx);
		table.removeColumn(col);
	}
	public TableColumn getColumn(int col)
	{
		return table.getColumnModel().getColumn(col);
	}
	public int getSelectedModelRow()
	{
		if (footerRowSelected())
		{
			return TABLE_ROW_FOOTER;
		}
		
		int viewRow = table.getSelectedRow();
		if (viewRow == -1)
		{
			return -1;
		}
		
		return table.convertRowIndexToModel(viewRow);
	}
	private boolean footerRowSelected()
	{
		return tableFooter.getSelectedRow() > -1;
	}
	public int[] getSelectedModelRows()
	{
		int[] viewRows = table.getSelectedRows();
		
		int rowCount = viewRows.length;
		int[] modelRows = new int[rowCount];
		for (int i=0; i<rowCount; i++)
		{
			int viewRow = viewRows[i];
			modelRows[i] = table.convertRowIndexToModel(viewRow);
		}
		
		return modelRows;
	}
	public int getSelectedViewRow()
	{
		if (footerRowSelected())
		{
			return TABLE_ROW_FOOTER;
		}
		
		return table.getSelectedRow();
	}
	
	public void selectFirstRow()
	{
		selectRow(0);
	}
	public void selectRow(int row)
	{
		if (table.getRowCount() == 0)
		{
			//Nothing to select
			return;
		}
		
		if (row == -1)
		{
			table.clearSelection();
		}
		else if (row == TABLE_ROW_FOOTER)
		{
			tableFooter.setRowSelectionInterval(0, 0);
		}
		else
		{
			table.setRowSelectionInterval(row, row);
			table.scrollRectToVisible(new Rectangle(table.getCellRect(row, 0, true)));
		}
	}
	
	public void addFooterRow(Object[] row)
	{
		tableFooter.setVisible(true);
		
		DefaultTableModel model = (DefaultTableModel)tableFooter.getModel();
		if (model.getRowCount() > 0)
		{
			model.removeRow(0);
		}
		
		model.addRow(row);
	}
	public void removeAllRows()
	{
		getModel().setRowCount(0);
		refreshRowCount();
	}
	public void scrollToBottom()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				JScrollBar vertical = scrollPane.getVerticalScrollBar();
				vertical.setValue( vertical.getMaximum() );
			}
		});
	}
	
	/**
	 * Default methods
	 */
	@SuppressWarnings("unused")
	public boolean isEditable(int row, int col)
	{
		return false;
	}
	
	/**
	 * TableColumnModelListener
	 */
	@Override
	public void columnAdded(TableColumnModelEvent e){}
	@Override
	public void columnMarginChanged(ChangeEvent e)
	{
		if (!tableFooter.isVisible())
		{
			return;
		}
		
		for (int i=0; i<getColumnCount(); i++)
		{
			TableColumn column = getColumn(i);
			int width = column.getWidth();
			
			TableColumn footerColumn = tableFooter.getColumnModel().getColumn(i);
			footerColumn.setMinWidth(width);
			footerColumn.setMaxWidth(width);
		}
		
		tableFooter.repaint();
	}
	@Override
	public void columnMoved(TableColumnModelEvent e){}
	@Override
	public void columnRemoved(TableColumnModelEvent e){}
	@Override
	public void columnSelectionChanged(ListSelectionEvent e){}
	
	/**
	 * ListSelectionListener
	 */
	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getSource() == table.getSelectionModel())
		{
			updateSelection(table, tableFooter);
		}
		else if (e.getSource() == tableFooter.getSelectionModel())
		{
			updateSelection(tableFooter, table);
		}
		
		//Now fire off an event for external listeners, once we've sorted the footer row etc
		for (RowSelectionListener listener : listeners)
		{
			listener.selectionChanged(this);
		}
	}
	private void updateSelection(JTable src, JTable dest)
	{
		int srcRow = src.getSelectedRow();
		if (srcRow > -1)
		{
			//We've selected something in the source, so need to clear the destination table's selection
			dest.clearSelection();
		}
	}
	
	public void addKeyAction(int key, String actionName, AbstractAction action)
	{
		table.getInputMap().put(KeyStroke.getKeyStroke(key, WHEN_FOCUSED), actionName);
		table.getActionMap().put(actionName, action);
	}
	
	public void addRowSelectionListener(RowSelectionListener listener)
	{
		listeners.add(listener);
	}
	
	public void addDoubleClickListener(DoubleClickListener listener)
	{
		clickListeners.add(listener);
	}
	
	public void setBorder(AbstractBorder border)
	{
		scrollPane.setBorder(border);
		table.setBorder(border);
	}
	
	public void setBackgroundProper(Color bg)
	{
		table.setBackground(bg);
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0)
	{
		if (arg0.getClickCount() == 2)
		{
			for (DoubleClickListener listener : clickListeners)
			{
				listener.doubleClicked(this);
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0){}
	@Override
	public void mouseExited(MouseEvent arg0){}
	@Override
	public void mousePressed(MouseEvent arg0){}
	@Override
	public void mouseReleased(MouseEvent arg0){}
}
