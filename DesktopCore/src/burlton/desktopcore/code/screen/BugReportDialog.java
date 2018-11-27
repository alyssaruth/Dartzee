package burlton.desktopcore.code.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import burlton.desktopcore.code.obj.LimitedDocument;
import burlton.core.code.util.Debug;
import burlton.desktopcore.code.util.DialogUtil;

public class BugReportDialog extends JFrame
							 implements ActionListener
{
	public BugReportDialog() 
	{
		try
		{
			setTitle("Bug Report");
			setSize(400, 400);
			setMinimumSize(new Dimension(400, 400));
			JPanel panel = new JPanel();
			getContentPane().add(panel, BorderLayout.SOUTH);
			panel.add(btnSendReport);
			panel.add(btnCancel);
			JPanel panel_1 = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			getContentPane().add(panel_1, BorderLayout.NORTH);
			
			JLabel lblDescriptionconcise = new JLabel("Description*");
			panel_1.add(lblDescriptionconcise);
			
			Component horizontalStrut = Box.createHorizontalStrut(5);
			panel_1.add(horizontalStrut);
			descriptionField.setPreferredSize(new Dimension(280, 22));
			panel_1.add(descriptionField);
			descriptionField.setDocument(new LimitedDocument(40));
			JPanel panel_2 = new JPanel();
			getContentPane().add(panel_2, BorderLayout.CENTER);
			panel_2.setLayout(new BorderLayout(0, 0));
			
			JPanel panel_3 = new JPanel();
			FlowLayout flowLayout_1 = (FlowLayout) panel_3.getLayout();
			flowLayout_1.setAlignment(FlowLayout.LEFT);
			panel_2.add(panel_3, BorderLayout.NORTH);
			
			JLabel lblReplicationSteps = new JLabel("Additional Information / Replication Steps:");
			lblReplicationSteps.setHorizontalAlignment(SwingConstants.LEFT);
			panel_3.add(lblReplicationSteps);
			
			JPanel panel_4 = new JPanel();
			panel_4.setBorder(new EmptyBorder(5, 5, 5, 5));
			panel_2.add(panel_4, BorderLayout.CENTER);
			panel_4.setLayout(new BorderLayout(0, 0));
			
			textPaneReplicationSteps.setBorder(new LineBorder(Color.GRAY));
			
			JScrollPane scrollpane = new JScrollPane();
			scrollpane.setViewportView(textPaneReplicationSteps);
			panel_4.add(scrollpane, BorderLayout.CENTER);
			btnSendReport.addActionListener(this);
			btnCancel.addActionListener(this);
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
		}
	}
	
	private final JButton btnSendReport = new JButton("Send Report");
	private final JButton btnCancel = new JButton("Cancel");
	private final JTextField descriptionField = new JTextField();
	private final JTextPane textPaneReplicationSteps = new JTextPane();
	
	public void init()
	{
		descriptionField.setText("");
		textPaneReplicationSteps.setText("");
	}
	
	private boolean valid()
	{
		String description = descriptionField.getText();
		if (description == null || description.isEmpty())
		{
			DialogUtil.showError("You must enter a description.");
			return false;
		}
		
		return true;
	}
	
	private void sendReport()
	{
		String description = "BUG REPORT: " +  descriptionField.getText();
		String replication = textPaneReplicationSteps.getText();
		
		if (Debug.sendBugReport(description, replication))
		{
			DialogUtil.showInfo("Bug report submitted.");
			dispose();
		}
		else
		{
			DialogUtil.showInfo("Unable to send bug report. Please check your internet connection and try again.");
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		JButton source = (JButton)arg0.getSource();
		if (source == btnSendReport)
		{
			if (valid())
			{
				sendReport();
			}
		}
		else if (source == btnCancel)
		{
			dispose();
		}
		else
		{
			Debug.stackTrace("Unexpected actionPerformed: " + source);
		}
	}
}
