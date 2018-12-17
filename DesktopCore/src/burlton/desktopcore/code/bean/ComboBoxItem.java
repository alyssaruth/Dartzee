package burlton.desktopcore.code.bean;

public class ComboBoxItem<E>
{
	private Object visibleData;
	private E hiddenData;
	private boolean enabled = true;
	
	public ComboBoxItem(E hiddenData, Object visibleData)
	{
		this.hiddenData = hiddenData;
		this.visibleData = visibleData;
	}
	
	public Object getVisibleData()
	{
		return visibleData;
	}
	public E getHiddenData()
	{
		return hiddenData;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
	
	@Override
	public String toString() 
	{
		if (enabled)
		{
			return "" + visibleData;
		}
		
		return "<html><font color=\"gray\">" + visibleData + "</font></html>";
	}
}
