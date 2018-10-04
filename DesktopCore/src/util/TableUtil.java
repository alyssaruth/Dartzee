package util;

import java.awt.Component;
import java.awt.Font;
import java.sql.Timestamp;
import java.util.Comparator;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import object.FlagImage;

public class TableUtil 
{
	public static class SimpleRenderer extends DefaultTableCellRenderer
	{
		private Font font = null;
		int alignment = -1;
		
		public SimpleRenderer(Font font)
		{
			this(SwingConstants.CENTER, font);
		}
		public SimpleRenderer(int alignment)
		{
			this(alignment, null);
		}
		public SimpleRenderer(int alignment, Font font)
		{
			this.alignment = alignment;
			this.font = font;
		}
		
        @Override
        public Component getTableCellRendererComponent(JTable table, Object
            value, boolean isSelected, boolean hasFocus, int row, int column) 
        {
    		Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    		if (component instanceof JLabel)
    		{
    			JLabel label = (JLabel)component;
    			label.setHorizontalAlignment(alignment);
    			
    			if (font != null)
    			{
    				setFont(font);
    			}
    		}
    		
    		return component;
        }
    }
    
    public static final DefaultTableCellRenderer FLAG_RENDERER = new DefaultTableCellRenderer() 
    {
    	@Override
		public void setValue(Object value) 
    	{	
    	    if (value == null) 
    	    {
    	    	setIcon(null);
    	    	setToolTipText(null);
    	    }
    	    else
    	    {
    	    	FlagImage fi = (FlagImage)value;
    	    	setIcon(fi.getIcon());
    	    	setToolTipText(fi.getToolTipText());
    	    }
    	}
    	 @Override
         public Component getTableCellRendererComponent(JTable table, Object
             value, boolean isSelected, boolean hasFocus, int row, int column) 
         {
             super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
             setHorizontalAlignment(SwingConstants.CENTER);
             return this;
         }
    };
	
	public static final Comparator<String> DATE_COMPARATOR = new Comparator<String>() 
	{
		@Override
		public int compare(String s1, String s2) 
		{
			String string1 = convertDateForSorting(s1);
			String string2 = convertDateForSorting(s2);

			return string1.compareTo(string2);
		}
	};
	
	public static final DefaultTableCellRenderer TIME_RENDERER = new DefaultTableCellRenderer() 
    {
    	@Override
		public void setValue(Object value) 
    	{	
    	    if (value == null) 
    	    {
    	    	setText("-");
    	    }
    	    else
    	    {
    	    	Long longValue = (Long)value;
    	    	String formattedTime = DateUtil.formatHHMMSS(longValue);
    	    	setText(formattedTime);
    	    }
    	}
    	 @Override
         public Component getTableCellRendererComponent(JTable table, Object
             value, boolean isSelected, boolean hasFocus, int row, int column) 
         {
             super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
             setHorizontalAlignment(SwingConstants.CENTER);
             return this;
         }
    };
	
	private static String convertDateForSorting(String s)
	{
		try
		{
			String year = s.substring(6,10);
			String month = s.substring(3,5);
			String day = s.substring(0,2);
			String hour = s.substring(13, 15);
			String minutes = s.substring(16, 18);

			return year + month + day + hour + minutes;
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
			return "";
		}
	}
	
	public static final DefaultTableCellRenderer TIMESTAMP_RENDERER = new DefaultTableCellRenderer() 
    {
		@Override
        public Component getTableCellRendererComponent(JTable table, Object
            value, boolean isSelected, boolean hasFocus, int row, int column) 
        {
			Object newValue = getObjectForValue(value, row, column);
    		super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column);
    		return this;
        }
		
		private Object getObjectForValue(Object value, int row, int column)
		{
			if (value == null)
			{
				return "";
			}
			
			if (!(value instanceof Timestamp))
			{
				Debug.stackTrace("Non-timestamp object in table. Row " + row + ", Col " + column);
				return null;
			}
			
			Timestamp timestamp = (Timestamp)value;
			return DateUtil.formatTimestamp(timestamp);
		}
    };

	public static final Comparator<FlagImage> FLAG_COMPARATOR = new Comparator<FlagImage>()
	{
		@Override
		public int compare(FlagImage i1, FlagImage i2)
		{
			String order1 = i1.getOrderStr();
			String order2 = i2.getOrderStr();

			return order1.compareTo(order2);
		}
	};
	
	public static final Comparator<String> INT_COMPARATOR = new Comparator<String>()
	{
    	@Override
		public int compare(String s1, String s2)
    	{
    		int i1 = Integer.parseInt(s1);
    		int i2 = Integer.parseInt(s2);
    		
    		if (i1 > i2)
    		{
    			return 1;
    		}
    		else if (i1 < i2)
    		{
    			return -1;
    		}
    		else
    		{
    			return 0;
    		}
    		
    	}
    };
    
    public static final Comparator<Long> LONG_COMPARATOR = new Comparator<Long>()
    {
    	@Override
		public int compare(Long l1, Long l2)
    	{
	    	long i1 = l1.longValue();
	    	long i2 = l2.longValue();

    		if (i1 > i2)
    		{
    			return 1;
    		}
    		else if (i1 < i2)
    		{
    			return -1;
    		}
    		else
    		{
    			return 0;
    		}

    	}
    };

    public static final Comparator<String> PLAYERS_COMPARATOR = new Comparator<String>()
	{
    	@Override
		public int compare(String arg0, String arg1) 
    	{
    		int count1 = Integer.parseInt(arg0.substring(0, 1));
    		int count2 = Integer.parseInt(arg1.substring(0, 1));
    		
    		int capacity1 = Integer.parseInt(arg0.substring(2, 3));
    		int capacity2 = Integer.parseInt(arg1.substring(2, 3));
    		
    		if (count1 > count2)
    		{
    			return 1;
    		}
    		else if (count1 < count2)
    		{
    			return -1;
    		}
    		else
    		{
    			if (capacity1 > capacity2)
    			{
    				return 1;
    			}
    			else if (capacity1 < capacity2)
    			{
    				return -1;
    			}
    			else
    			{
    				return 0;
    			}
    		}
    	}
	};
	
	public static class DefaultModel extends DefaultTableModel
	{
		@Override
		public boolean isCellEditable(int row, int column) {return false;}
		
		@Override
		public Class<?> getColumnClass(int arg0)
		{
			if (getRowCount() > 0
			  && getValueAt(0, arg0) != null)
			{
				return getValueAt(0, arg0).getClass();
			}
			
			return super.getColumnClass(arg0);
		}
	}
}
