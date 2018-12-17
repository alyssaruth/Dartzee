package burlton.desktopcore.code.screen;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import burlton.core.code.util.Debug;

public class ProgressDialog extends JDialog
							implements ActionListener
{	
	private int workToDo = 0;
	private boolean cancelPressed = false;
	private String message = "";
	
	public ProgressDialog(int workToDo, String message) 
	{
		try
		{
			this.workToDo = workToDo;
			this.message = message;
			
			JPanel panel = new JPanel();
			panel.setBorder(new EmptyBorder(10, 0, 0, 0));
			getContentPane().add(panel, BorderLayout.CENTER);
			progressBar.setPreferredSize(new Dimension(200, 20));
			progressBar.setStringPainted(true);
			panel.add(progressBar);
			
			JPanel panel_1 = new JPanel();
			panel_1.setBorder(new EmptyBorder(5, 0, 5, 0));
			getContentPane().add(panel_1, BorderLayout.SOUTH);
			panel_1.add(btnCancel);
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			
			btnCancel.addActionListener(this);
			btnCancel.setVisible(false);
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
		}
	}
	
	private JProgressBar progressBar = new JProgressBar();
	private JButton btnCancel = new JButton("Cancel");
	
	public static ProgressDialog factory(String title, String message, int workToDo)
	{
		ProgressDialog dialog = new ProgressDialog(workToDo, message);
		dialog.resetProgress();
		dialog.setTitle(title);
		dialog.setSize(300, 90);
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(null);
		dialog.setModal(true);
		
		return dialog;
	}
	
	public void setVisibleLater()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				setVisible(true);
			}
		});
	}
	
	public void resetProgress()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				progressBar.setMinimum(0);
				progressBar.setMaximum(workToDo);
				progressBar.setValue(0);
				progressBar.setString(workToDo + " " + message);
			}
		});
	}
	
	public void incrementProgressLater()
	{
		incrementProgressLater(1);
	}
	
	public void incrementProgressLater(final int increment)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				int newValue = progressBar.getValue() + increment;
				progressBar.setValue(newValue);
				progressBar.setString((workToDo - newValue) + " " + message);
				progressBar.repaint();
			}
		});
	}
	
	
	
	public boolean cancelPressed()
	{
		return cancelPressed;
	}
	
	public void disposeLater()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				dispose();
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		cancelPressed = true;
	}
	
	public void showCancel(boolean showCancel)
	{
		btnCancel.setVisible(showCancel);
		
		int height = showCancel?120:90;
		setSize(300, height);
	}
}
