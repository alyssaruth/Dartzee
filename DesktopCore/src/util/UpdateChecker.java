package util;

import java.io.IOException;

import javax.swing.JOptionPane;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class to check for updates and launch the EntropyUpdater via a batch file if they are available
 */
public class UpdateChecker implements XmlConstants
{
	public static void checkForUpdates(String filename, int portForDownload)
	{
		try
		{
			checkForUpdatesAndDoDownloadIfRequired(filename, portForDownload);
		}
		finally
		{
			DialogUtil.dismissLoadingDialog();
		}
	}
	
	private static void checkForUpdatesAndDoDownloadIfRequired(String filename, int portForDownload)
	{
		//Show this here, checking the CRC can take time
		DialogUtil.showLoadingDialog("Checking for updates...");
		String crc = FileUtil.getMd5Crc(filename);
		if (crc == null)
		{
			DialogUtil.showError("Failed to check for updates (couldn't find " + filename + ").");
			return;
		}
		
		Debug.append("Checking for updates - fileCrc is " + crc);
		
		//We have a CRC, so go to the Server and check against it's copy.
		Document crcCheck = factoryCrcCheck(crc, filename);
		String responseStr = AbstractClient.getInstance().sendSync(crcCheck, false);
		if (responseStr == null)
		{
			DialogUtil.showError("Failed to check for updates (unable to connect).");
			return;
		}
		
		DialogUtil.dismissLoadingDialog();
		
		Document xmlResponse = XmlUtil.getDocumentFromXmlString(responseStr);
		Element rootElement = xmlResponse.getDocumentElement();
		String responseName = rootElement.getNodeName();
		if (responseName.equals(RESPONSE_TAG_NO_UPDATES))
		{
			//No need to show a message, should be pretty obvious
			Debug.append("I am up to date");
			return;
		}
		
		//An update is available
		int answer = DialogUtil.showQuestion("An update is available. Would you like to download it now?", false);
		if (answer == JOptionPane.NO_OPTION)
		{
			return;
		}
		
		startUpdate(rootElement, filename, portForDownload);
	}
	
	private static Document factoryCrcCheck(String fileCrc, String fileName)
	{
		Document document = XmlUtil.factoryNewDocument();
		Element rootElement = document.createElement(ROOT_TAG_CRC_CHECK);
		rootElement.setAttribute("FileCrc", fileCrc);
		rootElement.setAttribute("FileName", fileName);
		
		document.appendChild(rootElement);
		return document;
	}
	
	public static void startUpdate(Element rootElement, String filename, int portForUpdate)
	{
		int fileSize = XmlUtil.getAttributeInt(rootElement, "FileSize");
		String version = rootElement.getAttribute("VersionNumber");
		String args = fileSize + " " + version + " " + filename + " " + portForUpdate;
		
		try
		{
			if (AbstractClient.isAppleOs())
			{
				Runtime.getRuntime().exec("update.command " + args);
			}
			else
			{
				Runtime.getRuntime().exec("cmd /c start update.bat " + args);
			}
		}
		catch (IOException ioe)
		{
			Debug.stackTrace(ioe);
			String manualCommand = "update.bat " + args;
			
			String msg = "Failed to launch update.bat - call the following manually to perform the update: \n\n" + manualCommand;
			DialogUtil.showError(msg);
		}
		
		System.exit(0);
	}
}
