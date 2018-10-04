package util;

import java.lang.Thread.State;
import java.util.Iterator;
import java.util.Map;

public class ThreadUtil
{
	public static void dumpStacks()
	{
		Debug.appendWithoutDate("");
		Debug.appendBanner("STACKS DUMP");
		
		Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
		Iterator<Thread> it = threads.keySet().iterator();
		for (; it.hasNext(); )
		{
			Thread thread = it.next();
			StackTraceElement[] stack = thread.getStackTrace();
			State state = thread.getState();
			if (stack.length > 0)
			{
				Debug.append("---- THREAD " + thread.getName() + "  (" + state + ") ----");
				for (StackTraceElement element : stack)
				{
					Debug.appendWithoutDate("" + element);
				}
			}
		}
		
		Debug.appendWithoutDate("");
	}
	
	public static void sleepWithCatch(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException ie)
		{
			Debug.append("Caught " + ie + " sleeping for " + millis + " millis");
		}
	}
}
