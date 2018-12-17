package burlton.desktopcore.code.screen;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class LoadingDialog extends JDialog
{
	public LoadingDialog()
	{
		setSize(200, 100);
		setLocationRelativeTo(null);
		setResizable(false);
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lblMessage, BorderLayout.CENTER);
	}
	
	private final JLabel lblMessage = new JLabel("Communicating with Server...");
	
	public void showDialog(final String textToShow)
	{
		Runnable showRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				lblMessage.setText(textToShow);
				setVisible(true);
			}
		};
		
		SwingUtilities.invokeLater(showRunnable);
	}
	
	public void dismissDialog()
	{
		Runnable hideRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				setVisible(false);
			}
		};
		
		SwingUtilities.invokeLater(hideRunnable);
	}
}