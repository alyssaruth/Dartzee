package screen;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import util.DebugOutput;

public class DebugConsole extends JFrame
						  implements DebugOutput
{
	private boolean scrollLock = false;
	
	public DebugConsole()
	{
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(scrollPane);
		
		textArea.setForeground(Color.GREEN);
		textArea.setBackground(Color.BLACK);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		scrollPane.setViewportView(textArea);
		
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
	}
	
	private final JScrollPane scrollPane = new JScrollPane();
	private final JTextArea textArea = new JTextArea();
	
	public void scrollToBottom()
	{
		if (!scrollLock)
		{
			String allText = textArea.getText();
			int length = allText.length();
		
			textArea.setCaretPosition(length);
		}
	}
	
	public void setScrollLock(boolean scrollLock)
	{
		this.scrollLock = scrollLock;
	}

	@Override
	public void append(String text)
	{
		textArea.append(text);
		scrollToBottom();
	}

	@Override
	public String getLogs()
	{
		return textArea.getText();
	}
	
	@Override
	public void clear()
	{
		textArea.setText("");
	}
}
