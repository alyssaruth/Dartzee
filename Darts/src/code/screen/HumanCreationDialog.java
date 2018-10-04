package code.screen;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import code.db.PlayerEntity;

import java.awt.FlowLayout;

public class HumanCreationDialog extends AbstractPlayerCreationDialog
{
	public HumanCreationDialog()
	{
		super();
		
		setTitle("New Player");
		setSize(350, 225);
		setResizable(false);
		setModal(true);
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setHgap(20);
		
		getContentPane().add(panel, BorderLayout.CENTER);
		
		panel.add(avatar);
		panel.add(textFieldName);
		textFieldName.setColumns(10);
	}
	
	private final JPanel panel = new JPanel();
	
	public void init()
	{
		createdPlayer = false;
		textFieldName.setText("");
		avatar.init(null, false);
	}
	
	@Override
	protected void savePlayer()
	{
		String name = textFieldName.getText();
		long avatarId = avatar.getAvatarId();
		
		PlayerEntity.factoryAndSaveHuman(name, avatarId);
		
		createdPlayer = true;
		
		//Now dispose the window
		dispose();
	}
}
