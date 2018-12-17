package burlton.desktopcore.code.screen;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import burlton.desktopcore.code.bean.HyperlinkAdaptor;
import burlton.desktopcore.code.bean.HyperlinkListener;

public abstract class AbstractAboutDialog extends JDialog 
						 		  		  implements HyperlinkListener,
						 		  			         ActionListener
{
	public AbstractAboutDialog() 
	{
		setTitle("About");
		setSize(230, 175);
		setResizable(false);
		getContentPane().setLayout(null);
		lblProductDesc.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblProductDesc.setBounds(15, 8, 184, 25);
		lblProductDesc.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lblProductDesc);
		JLabel lblCreatedByAlex = new JLabel("Created by Alex Burlton");
		lblCreatedByAlex.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblCreatedByAlex.setBounds(0, 29, 214, 25);
		lblCreatedByAlex.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lblCreatedByAlex);
		lblViewChangelog.setBounds(65, 65, 84, 25);
		lblViewChangelog.setForeground(Color.BLUE);
		lblViewChangelog.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblViewChangelog.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lblViewChangelog);
		btnOk.setMargin(new Insets(0, 0, 0, 0));
		btnOk.setBounds(85, 107, 45, 25);
		getContentPane().add(btnOk);

		HyperlinkAdaptor adaptor = new HyperlinkAdaptor(this);
		lblViewChangelog.addMouseListener(adaptor);
		lblViewChangelog.addMouseMotionListener(adaptor);
		
		btnOk.addActionListener(this);
	}
	
	private final JLabel lblProductDesc = new JLabel(getProductDesc());
	private final JLabel lblViewChangelog = new JLabel("<html><u>Change Log</u></html>");
	private final JButton btnOk = new JButton("Ok");
	
	/**
	 * Abstract methods
	 */
	public abstract String getProductDesc();
	public abstract Window getChangeLog();

	/**
	 * HyperlinkListener
	 */
	@Override
	public void linkClicked(MouseEvent arg0)
	{
		setVisible(false);
		Window dialog = getChangeLog();
		dialog.setVisible(true);
	}

	@Override
	public boolean isOverHyperlink(MouseEvent arg0)
	{
		return true;
	}
	
	/**
	 * ActionListener
	 */
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		dispose();
	}
}