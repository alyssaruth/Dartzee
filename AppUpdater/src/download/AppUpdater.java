package download;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import burlton.desktopcore.code.screen.DebugConsole;
import burlton.desktopcore.code.screen.ProgressDialog;
import burlton.core.code.util.Debug;
import burlton.desktopcore.code.util.DialogUtil;
import burlton.core.code.util.FileUtil;
import burlton.core.code.util.MessageUtil;

public class AppUpdater
{
	private static final int ERROR_CODE_SUCCESS = 0;
	private static final int ERROR_CODE_ERROR = 1;
	
	private static int port = -1;
	private static String fileName = "";
	private static String fileNameTmp = "";
	
	private static boolean downloadFinished = false;
	private static boolean downloadFailed = false;
	
	public static void main(String[] args)
	{
		Debug.initialise(new DebugConsole());
		Debug.setProductDesc("App Updater");
		Debug.setLogToSystemOut(false);
		
		int fileSize = Integer.parseInt(args[0]);
		String version = args[1];
		fileName = args[2];
		fileNameTmp = fileName.replace(".", "_Tmp.");
		port = Integer.parseInt(args[3]);
		
		Debug.setProductDesc("App Updater: " + fileName + " " + version);
		
		doDownloadInBackgroundThread(fileSize, version);
	}
	
	private static void doDownloadInBackgroundThread(final int fileSize, final String version)
	{
		Runnable downloadRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				doDownload(fileSize, version);
			}
		};
		
		Thread t = new Thread(downloadRunnable, "DownloadThread");
		t.start();
		
		Debug.append("Waiting for download to finish...");
	    while (!downloadFinished) 
	    {
	    	try {Thread.sleep(1000);} catch (Throwable e) {}
	    }
	    
	    if (downloadFailed)
	    {
	    	System.exit(ERROR_CODE_ERROR);
	    }
	    
	    renameFiles(version);
	}
	
	private static void doDownload(int fileSize, String version)
	{
		int fileSizeKb = fileSize / 1024;
		
		ProgressDialog dialog = ProgressDialog.factory("Downloading update (" + version + ")", "kB remaining", fileSizeKb);
		dialog.setVisibleLater();
		
		InetAddress address = MessageUtil.factoryInetAddress(MessageUtil.SERVER_IP);
		try (Socket socket = new Socket(address, port);
		     BufferedInputStream inStream = new BufferedInputStream(socket.getInputStream());
			 FileOutputStream outStream = new FileOutputStream(fileNameTmp);)
		{
			byte[] buffer = new byte[4096];
			int count = 0;
			while ((count = inStream.read(buffer)) > 0)
			{
				outStream.write(buffer, 0, count);
				dialog.incrementProgressLater(count/1024);
			}
		}
		catch (SocketException | SocketTimeoutException se)
		{
			Debug.stackTraceSilently(se);
			downloadFailed("connection timed out");
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
			downloadFailed("an unexpected error occurred");
		}
		finally
		{
			dialog.disposeLater();
			downloadFinished = true;
		}
	}
	
	private static void downloadFailed(String reason)
	{
		downloadFailed = true;
		DialogUtil.showError("Download failed: " + reason);
		FileUtil.deleteFileIfExists(fileNameTmp);
	}
	
	private static void renameFiles(String version)
	{
		String errorString = FileUtil.swapInFile(fileName, fileNameTmp);
		if (errorString != null)
		{
			Debug.stackTrace("Failed to swap in downloaded file: " + errorString);
			DialogUtil.showError("An error occurred overwriting the old file.");
			System.exit(ERROR_CODE_ERROR);
		}

		String appName = FileUtil.stripFileExtension(fileName);
		DialogUtil.showInfo(appName + " " + version + " downloaded successfully.");
		System.exit(ERROR_CODE_SUCCESS);
	}
}
