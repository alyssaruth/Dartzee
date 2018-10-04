package code.db.sanity;

import javax.swing.table.DefaultTableModel;

import util.TableUtil.DefaultModel;

public class SanityCheckResultSimpleTableModel extends AbstractSanityCheckResult
{
	private String description = null;
	private DefaultModel model = null;
	
	public SanityCheckResultSimpleTableModel(DefaultModel model, String description)
	{
		this.model = model;
		this.description = description;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public int getCount()
	{
		return model.getRowCount();
	}

	@Override
	protected DefaultTableModel getResultsModel()
	{
		return model;
	}
}
