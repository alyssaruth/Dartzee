package burlton.desktopcore.code.bean;

import burlton.core.code.util.Debug;
import burlton.desktopcore.code.util.DialogUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

public class FileUploader extends JPanel
						  implements ActionListener
{
	private File selectedFile = null;
	private ArrayList<IFileUploadListener> listeners = new ArrayList<>();
	
	public FileUploader(FileFilter ff)
	{
		this(ff, "Upload");
	}
	public FileUploader(FileFilter ff, String buttonName)
	{
		setLayout(new BorderLayout(0, 0));
		
		FileFilter[] filters = fc.getChoosableFileFilters();
		fc.removeChoosableFileFilter(filters[0]);
		fc.addChoosableFileFilter(ff);
		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(textField, BorderLayout.CENTER);
		textField.setText("");
		textField.setEditable(false);
		panel.add(btnSelectFile, BorderLayout.EAST);
		btnSelectFile.setPreferredSize(new Dimension(25, 20));
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(0, 5, 0, 5));
		add(panel_1, BorderLayout.EAST);
		panel_1.setLayout(new BorderLayout(0, 0));
		panel_1.add(btnUpload, BorderLayout.CENTER);
		btnUpload.setText(buttonName);
		
		btnSelectFile.addActionListener(this);
		btnUpload.addActionListener(this);
	}
	
	private final JTextField textField = new JTextField("");
	private final JButton btnSelectFile = new JButton("...");
	private final JFileChooser fc = new JFileChooser();
	private final JButton btnUpload = new JButton("Upload");
	
	public File getSelectedFile()
	{
		return selectedFile;
	}
	public String getSelectedFilePath()
	{
		return textField.getText();
	}
	public void addFileUploadListener(IFileUploadListener listener)
	{
		listeners.add(listener);
	}
	
	private void selectFile()
	{
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) 
		{
			selectedFile = fc.getSelectedFile();
			textField.setText(selectedFile.getPath());

			Debug.append("Selected " + selectedFile.getName());
		}
	}
	
	private void uploadPressed()
	{
		if (selectedFile == null)
		{
			String btnText = btnUpload.getText().toLowerCase();
			DialogUtil.showError("You must select a file to " + btnText + ".");
			return;
		}
		
		for (int i=0; i<listeners.size(); i++)
		{
			IFileUploadListener listener = listeners.get(i);
			listener.fileUploaded(selectedFile);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		JButton src = (JButton)arg0.getSource();
		if (src == btnSelectFile)
		{
			selectFile();
		}
		else if (src == btnUpload)
		{
			uploadPressed();
		}
	}
}
