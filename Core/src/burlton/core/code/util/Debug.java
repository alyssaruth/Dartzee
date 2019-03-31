
package burlton.core.code.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Debug implements CoreRegistry
{
	public static final String SQL_PREFIX = "[SQL] ";
	public static final String BUG_REPORT_ADDITONAL_INFO_LINE = "Additional Information:";

	private static final String SUCCESS_MESSAGE = "Email sent successfully";
	private static final long ERROR_MESSAGE_DELAY_MILLIS = 10000; //10s
	private static final long MINIMUM_EMAIL_GAP_MILLIS = 10000;

	private static Object emailSyncObject = new Object();
	public static long lastErrorMillis = -1;
	private static long lastEmailMillis = -1;

	private static DebugOutput output = null;
	private static DebugExtension debugExtension = null;

	private static int positionLastEmailed = 0;
	private static int emailsSentInSuccession = 1;
	private static boolean sendingEmails = true;
	private static boolean logToSystemOut = false;

	//For the email header - should contain a description of the application
	private static String productDesc = "";

	private static ThreadFactory loggerFactory = new ThreadFactory()
	{
		@Override
		public Thread newThread(Runnable r)
		{
			return new Thread(r, "Logger");
		}
	};
	private static ExecutorService logService = Executors.newFixedThreadPool(1, loggerFactory);


	public static void appendSql(String text, boolean logging)
	{
		append(SQL_PREFIX + text, logging);
	}
	public static void append(String text)
	{
		append(text, true);
	}
	public static void append(String text, boolean logging)
	{
		append(text, logging, true);
	}
	private static void append(final String text, boolean logging, final boolean includeDate)
	{
		append(text, logging, includeDate, null);
	}
	private static void append(final String text, boolean logging, final boolean includeDate, final BooleanWrapper haveStackTraced)
	{
		if (!logging)
		{
			return;
		}

		Runnable logRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				appendInCurrentThread(text, includeDate, haveStackTraced);
			}
		};

		logService.execute(logRunnable);
	}

	public static void appendInCurrentThread(String text, boolean includeDate, BooleanWrapper haveStackTraced)
	{
		String time = "";
		if (includeDate)
		{
			time = getCurrentTimeForLogging();
		}

		if (output == null)
		{
			System.out.println("NULL OUTPUT: " + text);
		}

		output.append("\n" + time + text);

		if (logToSystemOut)
		{
			System.out.println(time + text);
		}

		if (haveStackTraced != null)
		{
			haveStackTraced.setValue(true);
		}
	}

	public static void waitUntilLoggingFinished()
	{
		try
		{
			logService.shutdown();
			logService.awaitTermination(30, TimeUnit.SECONDS);
		}
		catch (InterruptedException ie)
		{
			//Do nothing
		}
		finally
		{
			//Re-initialise the log service
			logService = Executors.newFixedThreadPool(1, loggerFactory);
		}
	}

	public static void appendWithoutDate(String text)
	{
		appendWithoutDate(text, true);
	}
	public static void appendWithoutDate(String text, boolean logging)
	{
		append("                                      " + text, logging, false);
	}

	public static void appendTabbed(String text)
	{
		appendWithoutDate("	" + text);
	}

	public static void appendBanner(String text)
	{
		appendBanner(text, true);
	}

	/*public static void appendBannerWithoutDate(String text)
	{
		int length = text.length();

		String starStr = "";
		for (int i=0; i<length + 4; i++)
		{
			starStr += "*";
		}

		appendWithoutDate(starStr);
		appendWithoutDate(text);
		appendWithoutDate(starStr);
	}*/

	public static void appendBanner(String text, boolean logging)
	{
		if (logging)
		{
			int length = text.length();

			String starStr = "";
			for (int i=0; i<length + 4; i++)
			{
				starStr += "*";
			}

			append(starStr, true);
			append("* " + text + " *", true);
			append(starStr, true);
		}
	}

	/**
	 * Stack Trace methods
	 */
	public static void stackTrace(String reason)
	{
		Throwable t = new Throwable();
		stackTrace(t, reason);
	}
	public static void stackTrace(Throwable t)
	{
		stackTrace(t, "");
	}
	public static void stackTrace(Throwable t, String message)
	{
		stackTrace(t, message, false);
	}
	public static void stackTraceNoError(String message)
	{
		Throwable t = new Throwable();
		stackTrace(t, message, true);
	}
	public static void stackTraceNoError(Throwable t)
	{
		stackTrace(t, "", true);
	}
	public static void stackTraceNoError(Throwable t, String message)
	{
		stackTrace(t, message, true);
	}
	public static void stackTrace(Throwable t, String message, boolean suppressError)
	{
		if (debugExtension != null
		  && !suppressError)
		{
			boolean showError = System.currentTimeMillis() - lastErrorMillis > ERROR_MESSAGE_DELAY_MILLIS;
			debugExtension.exceptionCaught(showError);
			if (showError)
			{
				lastErrorMillis = System.currentTimeMillis();
			}
		}

		String datetime = getCurrentTimeForLogging();

		String trace = "";
		if (!message.equals(""))
		{
			trace += datetime + message + "\n";
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		trace += datetime + sw.toString();

		BooleanWrapper haveAppendedStackTrace = new BooleanWrapper(false);
		append(trace, true, false, haveAppendedStackTrace);

		String extraDetails = " (" + productDesc + ")";

		if (message.length() > 50)
		{
			message = message.substring(0, 50) + "...";
		}

		sendContentsAsEmailInSeparateThread(t + " - " + message + extraDetails, false, haveAppendedStackTrace);
	}

	public static void stackTraceSilently(String message)
	{
		stackTraceSilently(new Throwable(message));
	}
	public static void stackTraceSilently(Throwable t)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		String trace = sw.toString();
		t.printStackTrace();

		append(trace, true);
	}

	public static void newLine()
	{
		appendWithoutDate("");
	}

	public static void logProgress(long workDone, long workToDo, int percentageToLogAt)
	{
		logProgress(workDone, workToDo, percentageToLogAt, "");
	}
	public static void logProgress(long workDone, long workToDo, int percentageToLogAt, String taskDesc)
	{
		//Convert 1 to 0.01, 50 to 0.5, etc.
		double percentageAsDecimal = ((double)percentageToLogAt)/100;
		double percentageOfTotal = Math.floor(workToDo * percentageAsDecimal);

		double remainder = workDone % percentageOfTotal;
		if (remainder == 0)
		{
			String percentStr = "" + (double)((10000*workDone)/workToDo)/100;
			String logStr = "Done " + workDone + "/" + workToDo;
			if (!taskDesc.isEmpty())
			{
				logStr += " " + taskDesc;
			}

			logStr += " (" + percentStr + "%)";
			append(logStr);
		}
	}

	/**
	 * SQLException
	 */
	public static void logSqlException(StringBuilder query, SQLException sqle)
	{
		logSqlException(query.toString(), sqle);
	}
	public static void logSqlException(String query, SQLException sqle)
	{
		Debug.append("Caught SQLException for query: " + query);

		while (sqle != null)
        {
			append("\n----- SQLException -----");
            append("  SQL State:  " + sqle.getSQLState());
            append("  Error Code: " + sqle.getErrorCode());
           	append("  Message:    " + sqle.getMessage());
            stackTrace(sqle);

            sqle = sqle.getNextException();
        }
	}

	public static void dumpList(String name, List<?> list)
	{
		String s = name;
		if (list == null)
		{
			s += ": null";
			appendWithoutDate(s);
			return;
		}

		s += "(size: " + list.size() + "): ";

		for (int i=0; i<list.size(); i++)
		{
			if (i > 0)
			{
				s += "\n";
			}

			s += list.get(i);
		}

		appendWithoutDate(s);
	}

	public static String getCurrentTimeForLogging()
	{
		long time = System.currentTimeMillis();

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm:ss.SSS");
		return sdf.format(time) + "   ";
	}

	public static void sendContentsAsEmailInSeparateThread(final String title, final boolean manual, final BooleanWrapper readyToEmail)
	{
		boolean shouldSendEmail = debugExtension != null;
		if (shouldSendEmail
		  && !manual)
		{
			boolean emailsEnabled = instance.getBoolean(INSTANCE_BOOLEAN_ENABLE_EMAILS, true);
			shouldSendEmail = emailsEnabled && sendingEmails;
		}

		if (!shouldSendEmail)
		{
			return;
		}

		String fullTitle = title;
		String username = instance.get(INSTANCE_STRING_USER_NAME, "");
		if (!username.equals(""))
		{
			fullTitle += " - " + username;
		}

		final String titleToUse = fullTitle;

		Runnable emailRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				while (readyToEmail != null
				  && readyToEmail.getValue() == false)
				{
					//wait
				}

				sendContentsAsEmail(titleToUse, manual);
			}
		};

		(new Thread(emailRunnable)).start();
	}

	private static void sendContentsAsEmail(String fullTitle, boolean manual)
	{
		try
		{
			synchronized (Debug.emailSyncObject)
			{
				if (!needToSendMoreLogs())
				{
					return;
				}

				long timeSinceLastEmail = System.currentTimeMillis() - lastEmailMillis;
				if (timeSinceLastEmail < MINIMUM_EMAIL_GAP_MILLIS)
				{
					if (!manual)
					{
						long timeToSleep = MINIMUM_EMAIL_GAP_MILLIS - timeSinceLastEmail;
						Debug.append("Waiting " + timeToSleep + " millis before sending logs...");
						Thread.sleep(timeToSleep);
					}

					fullTitle += " (Part " + (emailsSentInSuccession+1) + ")";
					emailsSentInSuccession++;
				}
				else
				{
					//reset this
					emailsSentInSuccession = 1;
				}

				String totalLogs = output.getLogs();
				String message = totalLogs.substring(positionLastEmailed);

				debugExtension.sendEmail(fullTitle, message);

				Debug.append(SUCCESS_MESSAGE, true);
				positionLastEmailed = positionLastEmailed + message.length();
				lastEmailMillis = System.currentTimeMillis();
			}
		}
		catch (Throwable t)
		{
			Debug.stackTraceSilently(t);
			sendingEmails = false;

			if (debugExtension != null)
			{
				debugExtension.unableToEmailLogs();
			}
		}
	}

	public static boolean sendBugReport(String description, String replication)
	{
		try
		{
			String username = instance.get(INSTANCE_STRING_USER_NAME, "");
			if (!username.equals(""))
			{
				description += " - " + username;
			}

			String totalLogs = output.getLogs();

			String message = "";
			if (replication != null && !replication.equals(""))
			{
				message += BUG_REPORT_ADDITONAL_INFO_LINE;
				message += "\n\n";
				message += replication;
				message += "\n--------------------------\n";
			}

			String logsToSend = totalLogs.substring(positionLastEmailed);
			message += logsToSend;

			debugExtension.sendEmail(description, message);

			Debug.append(SUCCESS_MESSAGE, true);
			positionLastEmailed = positionLastEmailed + logsToSend.length();
			emailsSentInSuccession++;
		}
		catch (Throwable t)
		{
			Debug.append("Unable to send Bug Report. Exceptions follow.");
			Debug.stackTraceSilently(t);
			return false;
		}

		return true;
	}

	private static boolean needToSendMoreLogs()
	{
		String ta = getLogs();
		String m = ta.substring(positionLastEmailed);
		if (m.contains(SUCCESS_MESSAGE) && m.length() < 100)
		{
			//last email was successful and only got 100 new characters to send, so don't bother
			return false;
		}

		return true;
	}

	/**
	 * Expose this statically - it makes sense
	 */
	public static String getLogs()
	{
		return output.getLogs();
	}

	public static void setSendingEmails(boolean sendingEmails)
	{
		Debug.sendingEmails = sendingEmails;
	}


	public static void initialise(DebugOutput output)
	{
		Debug.output = output;
	}

	public static void clearLogs()
	{
		output.clear();
	}

	public static void setDebugExtension(DebugExtension debugExtension)
	{
		Debug.debugExtension = debugExtension;
	}
	public static void setLogToSystemOut(boolean logToSystemOut)
	{
		Debug.logToSystemOut = logToSystemOut;
	}
	public static void setProductDesc(String productDesc)
	{
		Debug.productDesc = productDesc;
	}
}