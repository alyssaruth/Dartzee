package test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import code.screen.ScreenCache;
import code.utils.DesktopDartsClient;
import util.AbstractClient;
import util.Debug;

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
