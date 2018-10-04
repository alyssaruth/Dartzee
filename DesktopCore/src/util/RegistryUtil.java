package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import org.w3c.dom.Document;

public class RegistryUtil
{
	public static void clearNode(Preferences node)
	{
		try
		{
			node.clear();
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
		}
	}
	
	public static Document getAttributeXml(Preferences node, String key)
	{
		String attribute = node.get(key, "");
		return XmlUtil.getDocumentFromXmlString(attribute);
	}
	
	public static void setAttributeXml(Preferences node, String key, Document xmlDoc)
	{
		String xmlStr = XmlUtil.getStringFromDocument(xmlDoc);
		node.put(key, xmlStr);
	}
	
	public static String getKeyValue(String location, String key)
	{
		try
		{
			Process p = Runtime.getRuntime().exec("reg query \"" + location + "\" /v \"" + key + "\"");
			String result = getStringFromProcess(p);
			
			if (result != null)
			{
				ArrayList<String> toks = StringUtil.getListFromDelims(result, "    ");
				String value = toks.get(toks.size()-1);
				return value.trim();
			}
			
			return result;
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
			return null;
		}
	}
	private static String getStringFromProcess(Process p)
	{
		try (InputStream is = p.getInputStream();
		  InputStreamReader isr = new InputStreamReader(is);
		  BufferedReader br = new BufferedReader(isr);)
		{
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = br.readLine()) != null)
			{
				sb.append(line);
			}
			
			return sb.toString();
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
			return null;
		}
	}
	
	public static void setKeyValue(String location, String key, String value)
	{
		try
		{
			Runtime.getRuntime().exec("reg add \"" + location + "\" /v \"" + key + "\"/d \"" + value + "\" /f");
			Runtime.getRuntime().exec("%SystemRoot%\\System32\\RUNDLL32.EXE user32.dll, UpdatePerUserSystemParameters");
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
		}
	}
}
