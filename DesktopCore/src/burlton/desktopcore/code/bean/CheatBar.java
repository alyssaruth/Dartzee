package burlton.desktopcore.code.bean;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class CheatBar extends JTextField
					  implements ActionListener
{
	private AbstractDevScreen listener = null;
	
	public CheatBar()
	{
		setBorder(null);
		setOpaque(false);
		setEnabled(false);
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		
		addActionListener(this);
	}
	
	public void setCheatListener(AbstractDevScreen listener)
	{
		this.listener = listener;
		
		KeyStroke triggerStroke = listener.getKeyStrokeForCommandBar();
		JPanel content = (JPanel)listener.getContentPane();
		
		InputMap inputMap = content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(triggerStroke, "showCheatBar");
		
		ActionMap actionMap = content.getActionMap();
		actionMap.put("showCheatBar", new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if (listener.commandsEnabled())
				{
					listener.enableCheatBar(true);
					EventQueue.invokeLater(new Runnable() 
					{
						@Override
						public void run() 
						{
							grabFocus();
						}
					});
				}
			}
		});
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		String text = getText();
		setText(null);
		
		String result = listener.processCommandWithTry(text);
		if (result.isEmpty())
		{
			setEnabled(false);
		}
		else
		{
			setText(result);
		}
	}
}
