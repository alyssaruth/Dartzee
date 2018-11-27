package burlton.desktopcore.code.bean;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import burlton.core.code.util.Debug;

public class SuperTextPane extends JTextPane
{
	public SuperTextPane()
	{
		super();
		
		setDocument(doc);
	}
	
	private AwesomeStyledDocument doc = new AwesomeStyledDocument();
	
	/**
	 * Append text
	 */
	public void append(String str)
	{
		append(str, false);
	}
	public void append(String str, boolean bold)
	{
		append(str, bold, false);
	}
	public void append(String str, boolean bold, boolean italic)
	{
		SimpleAttributeSet style = new SimpleAttributeSet();
		StyleConstants.setBold(style, bold);
		StyleConstants.setItalic(style, italic);
		
		doc.append(str, style);
	}
	
	/**
	 * Overridden document with helper methods
	 */
	private static final class AwesomeStyledDocument extends DefaultStyledDocument
	{
		public void append(String str, AttributeSet style)
		{
			try
			{
				insertString(getLength(), str, style);
			}
			catch (BadLocationException ble)
			{
				Debug.stackTrace(ble);
			}
		}
	}
}
