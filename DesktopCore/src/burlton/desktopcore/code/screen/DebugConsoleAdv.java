package burlton.desktopcore.code.screen;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import burlton.core.code.util.Debug;
import burlton.core.code.util.DebugOutput;

public class DebugConsoleAdv extends JFrame
							 implements DebugOutput
{
	public DebugConsoleAdv()
	{
		setTitle("Console");
		setSize(1000, 600);
		setLocationRelativeTo(null);
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(scrollPane);
		
		textArea.setForeground(Color.GREEN);
		textArea.setBackground(Color.BLACK);
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
	}
	
	private final DefaultStyledDocument doc = new DefaultStyledDocument();
	private final JScrollPane scrollPane = new JScrollPane();
	private final JTextPane textArea = new JTextPane(doc);
	
	@Override
	public void append(String text)
	{
		StyleContext cx = new StyleContext();
		Style style = cx.addStyle(text, null);
		
		if (text.contains(Debug.SQL_PREFIX))
		{
			if (text.contains("INSERT")
			  || text.contains("UPDATE"))
			{
				StyleConstants.setForeground(style, Color.ORANGE);
			}
			else if (text.contains("DELETE"))
			{
				StyleConstants.setForeground(style, Color.RED);
			}
			else
			{
				StyleConstants.setForeground(style, Color.CYAN);
			}
		}
		
		try
		{
			doc.insertString(doc.getLength(), text, style);
			textArea.select(doc.getLength(), doc.getLength());
		}
		catch (BadLocationException ble)
		{
			Debug.stackTrace(ble, "BLE trying to append: " + text);
		}
	}

	@Override
	public String getLogs()
	{
		try
		{
			return doc.getText(0, doc.getLength());
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
			return null;
		}
	}
	
	@Override
	public void clear()
	{
		textArea.setText("");
	}

}
