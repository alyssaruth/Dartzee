package burlton.desktopcore.code.bean;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import burlton.core.code.util.Debug;

public class HyperlinkAdaptor extends MouseAdapter
{
	private HyperlinkListener listener = null;
	private Component listenerWindow = null;
	
	public HyperlinkAdaptor(HyperlinkListener listener)
	{
		if (!(listener instanceof Component))
		{
			Debug.stackTrace("Creating HyperlinkAdaptor with non-component: " + listener);
		}
		
		this.listener = listener;
		this.listenerWindow = (Component)listener;
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0)
	{
		listener.linkClicked(arg0);
	}

	@Override
	public void mouseMoved(MouseEvent arg0)
	{
		if (listener.isOverHyperlink(arg0))
		{
			listenerWindow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		else
		{
			listenerWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	@Override
	public void mouseExited(MouseEvent arg0)
	{
		listenerWindow.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
}
