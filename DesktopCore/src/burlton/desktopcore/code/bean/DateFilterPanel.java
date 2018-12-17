package burlton.desktopcore.code.bean;

import java.awt.Dimension;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import burlton.desktopcore.code.util.DateUtil;
import burlton.desktopcore.code.util.DialogUtil;
import de.wannawork.jcalendar.JCalendarComboBox;

public class DateFilterPanel extends JPanel
{
	public DateFilterPanel() 
	{
		add(lblFrom);
		lblFrom.setHorizontalAlignment(SwingConstants.LEFT);
		cbDateFrom.setPreferredSize(new Dimension(130, 22));
		add(cbDateFrom);
		add(lblTo);
		lblTo.setHorizontalAlignment(SwingConstants.LEFT);
		cbDateTo.setPreferredSize(new Dimension(130, 22));
		add(cbDateTo);
	}
	
	private final JCalendarComboBox cbDateFrom = new JCalendarComboBox();
	private final JCalendarComboBox cbDateTo = new JCalendarComboBox();
	private final JLabel lblFrom = new JLabel("from");
	private final JLabel lblTo = new JLabel("to");
	
	public void enableComponents(boolean enabled)
	{
		cbDateFrom.setEnabled(enabled);
		cbDateTo.setEnabled(enabled);
		lblFrom.setEnabled(enabled);
		lblTo.setEnabled(enabled);
	}
	
	public boolean valid()
	{
		if (!cbDateFrom.isEnabled())
		{
			return true;
		}
		
		Date dtFrom = getDtFrom();
		Date dtTo = getDtTo();
		
		if (dtFrom.compareTo(dtTo) > 0)
		{
			DialogUtil.showError("The 'date from' cannot be after the 'date to'");
			return false;
		}
		
		return true;
	}
	
	public boolean filter(java.util.Date date)
	{
		Date dtFrom = getDtFrom();
		Date dtTo = getDtTo();
		
		int i1 = date.compareTo(dtFrom);
		int i2 = date.compareTo(dtTo);

		if (i1 < 0)
		{
			return false;
		}

		if (i2 > 0)
		{
			return false;
		}
		
		return true;
	}
	
	public boolean filterSqlDate(Timestamp sqlDt)
	{
		java.sql.Date dtFrom = new java.sql.Date(getDtFrom().getTime());		
		java.sql.Date dtTo = new java.sql.Date(getDtTo().getTime());
		
		if (sqlDt.before(dtFrom))
		{
			return false;
		}
		
		if (sqlDt.after(dtTo))
		{
			return false;
		}
		
		return true;
	}
	
	public Date getDtFrom()
	{
		Date dt = cbDateFrom.getDate();
		return DateUtil.stripTimeComponent(dt);
	}
	public String getDtFromStr()
	{
		Date dtFrom = getDtFrom();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(dtFrom);
	}
	public Date getDtTo()
	{
		Date dt = cbDateTo.getDate();
		return DateUtil.stripTimeComponent(dt);
	}
	public String getDtToStr()
	{
		Date dtTo = getDtTo();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(dtTo);
	}
	
	public java.sql.Timestamp getSqlDtFrom()
	{
		Date dt = getDtFrom();
		return new java.sql.Timestamp(dt.getTime());
	}
	public java.sql.Timestamp getSqlDtTo()
	{
		Date dt = getDtTo();
		return new java.sql.Timestamp(dt.getTime());
	}
}
