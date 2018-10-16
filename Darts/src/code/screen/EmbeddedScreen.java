package code.screen;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;

import util.Debug;
import java.awt.Font;


public abstract class EmbeddedScreen extends JPanel
									 implements ActionListener
{
	public EmbeddedScreen()
	{
		setPreferredSize(new Dimension(800, 610));
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.EAST);
		btnNext.setFont(new Font("Tahoma", Font.PLAIN, 16));
		
		
		panel_1.add(btnNext);
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2, BorderLayout.WEST);
		btnBack.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel_2.add(btnBack);
		
		btnBack.setVisible(showBackButton());
		btnNext.setVisible(showNextButton());
		
		btnBack.addActionListener(this);
		btnNext.addActionListener(this);
	}
	
	protected final JButton btnBack = new JButton(" < Back");
	protected final JButton btnNext = new JButton(getNextText() + " > ");
	
	public abstract String getScreenName();
	public abstract void init();
	
	/**
	 * Called after the new screen has been switched in etc
	 */
	public void postInit()
	{
		
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		AbstractButton src = (AbstractButton)arg0.getSource();
		if (src == btnBack)
		{
			backPressed();
		}
		else if (src == btnNext)
		{
			nextPressed();
		}
		else
		{
			Debug.stackTrace("Unexpected actionPerformed: " + src.getText());
		}
	}
	
	private void backPressed()
	{
		EmbeddedScreen target = getBackTarget();
		ScreenCache.switchScreen(target, false);
	}
	
	public void nextPressed()
	{
		//default method
	}
	
	/**
	 * Default methods
	 */
	public boolean showBackButton()
	{
		return true;
	}
	public boolean showNextButton()
	{
		return false;
	}
	public String getNextText()
	{
		return "Next";
	}
	public EmbeddedScreen getBackTarget()
	{
		return ScreenCache.getScreen(MenuScreen.class);
	}
}
