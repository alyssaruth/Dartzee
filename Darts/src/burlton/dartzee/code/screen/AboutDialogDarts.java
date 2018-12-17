package burlton.dartzee.code.screen;

import java.awt.Window;

import burlton.desktopcore.code.screen.AbstractAboutDialog;
import burlton.core.code.util.OnlineConstants;

public class AboutDialogDarts extends AbstractAboutDialog
{
	@Override
	public String getProductDesc()
	{
		return "Darts " + OnlineConstants.DARTS_VERSION_NUMBER;
	}

	@Override
	public Window getChangeLog()
	{
		return new ChangeLog();
	}

}
