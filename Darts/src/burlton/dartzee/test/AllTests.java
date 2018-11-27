package burlton.dartzee.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import burlton.dartzee.code.screen.ScreenCache;
import burlton.dartzee.code.utils.DesktopDartsClient;
import burlton.core.code.util.AbstractClient;
import burlton.core.code.util.Debug;

@RunWith(Suite.class)
@SuiteClasses({ TestDartboardUtil.class, TestGeometryUtil.class,
		TestReporting.class, TestX01Util.class })

public class AllTests
{
	@BeforeClass
	public static void setup()
	{
		Debug.initialise(ScreenCache.getDebugConsole());
		AbstractClient.setInstance(new DesktopDartsClient());
	}
	
	@AfterClass
	public static void tearDown()
	{
		ScreenCache.getDebugConsole().setVisible(true);
		
		while (ScreenCache.getDebugConsole().isVisible())
		{
			try {Thread.sleep(1000);} catch (Throwable t){}
		}
	}
}
