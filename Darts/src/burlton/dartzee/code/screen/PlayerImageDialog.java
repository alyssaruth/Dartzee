package burlton.dartzee.code.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import burlton.desktopcore.code.util.ComponentUtil;
import burlton.desktopcore.code.util.DialogUtil;
import burlton.core.code.util.FileUtil;
import burlton.desktopcore.code.bean.FileUploadListener;
import burlton.desktopcore.code.bean.FileUploader;
import burlton.desktopcore.code.bean.WrapLayout;
import burlton.dartzee.code.bean.PlayerImageRadio;
import burlton.dartzee.code.db.PlayerImageEntity;

public class PlayerImageDialog extends JDialog
							   implements ActionListener,
							   			  FileUploadListener
{
	private long playerImageIdSelected = -1;
	
	public PlayerImageDialog() 
	{
		setSize(650, 400);
		setLocationRelativeTo(null);
		setModal(true);
		setTitle("Select Avatar");
		
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addTab("Presets", null, panelPreset, null);
		panelPreset.setLayout(new BorderLayout(0, 0));
		JScrollPane scrollPane = new JScrollPane();
		panelPreset.add(scrollPane);
		scrollPane.setViewportView(panelPresets);
		panelPresets.setLayout(new WrapLayout());
		tabbedPane.addTab("Upload", null, panelUpload, null);
		panelUpload.setLayout(new BorderLayout(0, 0));
		JPanel panelUploadOptions = new JPanel();
		panelUpload.add(panelUploadOptions, BorderLayout.NORTH);
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Previously Uploaded", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelUpload.add(scrollPane_1, BorderLayout.CENTER);
		scrollPane_1.setViewportView(panelPreviouslyUploaded);
		panelPreviouslyUploaded.setLayout(new WrapLayout());
		panelUploadOptions.setLayout(new BorderLayout(0, 0));
		panelUploadOptions.add(fs);
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.add(btnOk);
		panel.add(btnCancel);
		
		fs.addFileUploadListener(this);
		
		btnOk.addActionListener(this);
		btnCancel.addActionListener(this);
		
		init();
	}
	
	private final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
	private final JPanel panelPreset = new JPanel();
	private final JPanel panelUpload = new JPanel();
	private final JPanel panelPresets = new JPanel();
	private final JPanel panelPreviouslyUploaded = new JPanel();
	private final JButton btnOk = new JButton("Ok");
	private final FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
	private final FileUploader fs = new FileUploader(filter);
	private final JButton btnCancel = new JButton("Cancel");
	private final ButtonGroup bgUploaded = new ButtonGroup();
	
	private void init()
	{
		ArrayList<PlayerImageEntity> entities = new PlayerImageEntity().retrieveEntities();
		populatePanel(panelPresets, entities, true, new ButtonGroup());
		populatePanel(panelPreviouslyUploaded, entities, false, bgUploaded);
	}
	private void populatePanel(JPanel panel, ArrayList<PlayerImageEntity> entities, boolean preset, ButtonGroup bg)
	{
		for (int i=0; i<entities.size(); i++)
		{
			PlayerImageEntity pi = entities.get(i);
			if (pi.getPreset() != preset)
			{
				continue;
			}
			
			PlayerImageRadio lbl = new PlayerImageRadio(pi);
			
			panel.add(lbl);
			lbl.addToButtonGroup(bg);
		}
	}
	
	private void validateAndUploadImage(File imgFile)
	{
		Dimension imgDim = FileUtil.getImageDim(imgFile.getAbsolutePath());
		if (imgDim.getWidth() > 150
		  || imgDim.getHeight() > 150)
		{
			DialogUtil.showError("The image must be no larger than 150x150px.");
			return;
		}
		
		PlayerImageEntity pi = PlayerImageEntity.factoryAndSave(imgFile, false);
		PlayerImageRadio rdbtn = new PlayerImageRadio(pi);
		
		panelPreviouslyUploaded.add(rdbtn);
		rdbtn.addToButtonGroup(bgUploaded);
		
		repaint();
	}
	
	private void okPressed()
	{
		playerImageIdSelected = getPlayerImageIdFromSelection();
		if (playerImageIdSelected == -1)
		{
			DialogUtil.showError("You must select an image.");
			return;
		}
		
		dispose();
	}
	private long getPlayerImageIdFromSelection()
	{
		JPanel panel = (JPanel)tabbedPane.getSelectedComponent();
		
		ArrayList<PlayerImageRadio> radios = ComponentUtil.getAllChildComponentsForType(panel, PlayerImageRadio.class);
		for (int i=0; i<radios.size(); i++)
		{
			PlayerImageRadio rad = radios.get(i);
			if (rad.isSelected())
			{
				return rad.getPlayerImageId();
			}
		}
		
		return -1;
	}
	
	public void reset()
	{
		playerImageIdSelected = -1;
	}
	
	public long getPlayerImageIdSelected()
	{
		return playerImageIdSelected;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		Object src = arg0.getSource();
		if (src == btnOk)
		{
			okPressed();
		}
		else if (src == btnCancel)
		{
			dispose();
		}
	}
	@Override
	public void fileUploaded(File file)
	{
		validateAndUploadImage(file);
	}
}
