package code.db.sanity;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

import bean.ScrollTable;
import screen.TableModelDialog;
import util.DialogUtil;

public abstract class AbstractSanityCheckResult
{
	public abstract String getDescription();
	public abstract int getCount();
	
	protected abstract DefaultTableModel getResultsModel();
	
	public TableModelDialog getResultsDialog()
	{
		DefaultTableModel model = getResultsModel();
		
		ScrollTable t = getScrollTable();
		t.setModel(model);

		Action deleteAction = getDeleteAction(t);
		if (deleteAction != null)
		{
			t.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "Delete");
			t.getActionMap().put("Delete", deleteAction);
		}
			
		return new TableModelDialog(getDescription(), t);
	}
	
	public ScrollTable getScrollTable()
	{
		return new ScrollTable();
	}
	
	public Action getDeleteAction(ScrollTable t)
	{
		return null;
	}
	
	public void autoFix()
	{
		DialogUtil.showError("No auto-fix available.");
	}
	
	@Override
	public String toString()
	{
		return getDescription();
	}
}
