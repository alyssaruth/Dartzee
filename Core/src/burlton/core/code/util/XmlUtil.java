package burlton.core.code.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import burlton.core.code.obj.SuperHashMap;

public class XmlUtil 
{
	//Cache one instance of the factories - I've seen from thread stacks that constructing it each time is really slow
	private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	private static final TransformerFactory tf = TransformerFactory.newInstance();
	
	public static Document factoryNewDocument()
	{
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
		
			return builder.newDocument();
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
			return null;
		}
	}
	
	public static Document factorySimpleMessage(String name)
	{
		Document response = XmlUtil.factoryNewDocument();
		Element rootElement = response.createElement(name);
		
		response.appendChild(rootElement);
		return response;
	}
	
	public static String getStringFromDocument(Document xmlDoc)
	{
		try
		{
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(xmlDoc), new StreamResult(writer));

			StringBuffer sb = writer.getBuffer();
			String s = sb.toString().replaceAll("\n|\r", "");
			return s;
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
			return "";
		}
	}
	
	public static Document getDocumentFromXmlString(String xmlStr)
	{
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(null);
			return builder.parse(new InputSource(new StringReader(xmlStr)));
		}
		catch (Throwable t)
		{
			return null;
		}
	}
	
	public static int getAttributeIntCompulsory(Element rootElement, String attributeName) throws IOException
	{
		String attribute = getCompulsoryAttribute(rootElement, attributeName);
		int ret = -1;
		try
		{
			ret = Integer.parseInt(attribute);
		}
		catch (NumberFormatException nfe)
		{
			throw new IOException("Failed to parse " + attribute + " as an integer");
		}
		
		return ret;
	}
	
	public static String getCompulsoryAttribute(Element rootElement, String attributeName) throws IOException
	{
		String attribute = rootElement.getAttribute(attributeName);
		if (attribute.equals(""))
		{
			throw new IOException("Missing attribute: " + attributeName);
		}
		
		return attribute;
	}
	
	public static int getAttributeInt(Element rootElement, String attributeName)
	{	
		return getAttributeInt(rootElement, attributeName, 0);
	}
	
	public static int getAttributeInt(Element rootElement, String attributeName, int defaultValue)
	{
		String attribute = rootElement.getAttribute(attributeName);
		if (attribute.equals(""))
		{
			return defaultValue;
		}
		
		int ret = -1;
		try
		{
			ret = Integer.parseInt(attribute);
		}
		catch (NumberFormatException nfe)
		{
			Debug.append("Caught nfe trying to parse attribute " + attributeName + " with value " + attribute);
		}
		
		return ret;
	}
	
	public static long getAttributeLong(Element rootElement, String attributeName)
	{
		String attribute = rootElement.getAttribute(attributeName);
		if (attribute.equals(""))
		{
			return 0;
		}
		
		return Long.parseLong(attribute);
	}
	
	public static double getAttributeDouble(Element rootElement, String attributeName)
	{
		String attribute = rootElement.getAttribute(attributeName);
		if (attribute.equals(""))
		{
			return 0;
		}
		
		return Double.parseDouble(attribute);
	}
	
	public static boolean getAttributeBoolean(Element rootElement, String attributeName)
	{
		String value = rootElement.getAttribute(attributeName);
		return !value.equals("");
	}
	
	public static void setAttributeBoolean(Element rootElement, String attributeName, boolean bool)
	{
		if (bool)
		{
			rootElement.setAttribute(attributeName, "" + bool);
		}
	}
	
	public static ArrayList<String> getListFromElement(Element root, String elementName, String attributePrefix)
	{
		ArrayList<String> ret = new ArrayList<>();
		
		NodeList elements = root.getElementsByTagName(elementName);
		if (elements.getLength() == 0)
		{
			Debug.append("No such element: " + elementName);
			return ret;
		}
		
		Element element = (Element)elements.item(0);
		int i = 0;
		String card = element.getAttribute(attributePrefix + "-" + i);
		while (!card.isEmpty())
		{
			ret.add(card);
			i++;
			card = element.getAttribute(attributePrefix + "-" + i);
		}
		
		return ret;
	}
	
	public static Element getElementIfExists(Element rootElement, String elementName)
	{
		NodeList children = rootElement.getElementsByTagName(elementName);
		int size = children.getLength();
		if (size == 0)
		{
			return null;
		}
		
		if (size > 1)
		{
			Debug.stackTrace("Found more than 1 " + elementName + " element. Message: " );
		}
		
		return (Element)children.item(0);
	}

	public static Document factorySimpleMessage(String username, String rootName)
	{
		Document message = factoryNewDocument();
		
		Element rootElement = message.createElement(rootName);
		rootElement.setAttribute("Username", username);
		
		message.appendChild(rootElement);
		return message;
	}
	
	public static SuperHashMap<Integer, Integer> readIntegerHashMap(Element rootElement, String tagName, String keyTag, String valueTag)
	{
		SuperHashMap<Integer, Integer> hm = new SuperHashMap<>();
		
		NodeList children = rootElement.getElementsByTagName(tagName);
		int size = children.getLength();
		for (int i=0; i<size; i++)
		{
			Element child = (Element)children.item(i);
			int key = XmlUtil.getAttributeInt(child, keyTag);
			int value = XmlUtil.getAttributeInt(child, valueTag);
			
			hm.put(key, value);
		}
		
		return hm;
	}
	
	@SuppressWarnings("rawtypes")
	public static void writeHashMap(SuperHashMap hm, Document xmlDoc, Element rootElement, 
	  String tagName, String keyTag, String valueTag)
	{
		Iterator<Map.Entry> it = hm.entrySet().iterator();
		for (; it.hasNext(); )
		{
			Map.Entry entry = it.next();
			
			Element child = xmlDoc.createElement(tagName);
			child.setAttribute(keyTag, "" + entry.getKey());
			child.setAttribute(valueTag, "" + entry.getValue());
			rootElement.appendChild(child);
		}
	}
}
