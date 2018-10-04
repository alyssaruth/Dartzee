package code.db;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.sql.rowset.serial.SerialBlob;
import javax.swing.ImageIcon;

import util.Debug;
import util.FileUtil;

public class PlayerImageEntity extends AbstractDartsEntity<PlayerImageEntity>
{
	private static final String[] avatarPresets = {"BaboOne", "BaboTwo", "Dennis", "robot", "wage", "wallace", "yoshi", 
												   "Bean", "Goomba", "Minion", "Sid", "dibble"};
	
	//Image cache, to prevent us hitting the DB too often
	private static final HashMap<Long, ImageIcon> hmRowIdToImageIcon = new HashMap<>();
	
	private Blob blobData = null;
	private String filepath = "";
	private boolean preset = false;
	
	//Will be set when we retrieve from the DB
	private byte[] bytes = null;

	@Override
	public String getTableName()
	{
		return "PlayerImage";
	}

	@Override
	public String getCreateTableSqlSpecific()
	{
		return "BlobData Blob NOT NULL, Filepath VARCHAR(1000) NOT NULL, Preset BOOLEAN NOT NULL";
	}

	@Override
	public void populateFromResultSet(PlayerImageEntity img, ResultSet rs) throws SQLException
	{
		img.setBlobData(rs.getBlob("BlobData"));
		img.setFilepath(rs.getString("Filepath"));
		img.setPreset(rs.getBoolean("Preset"));
		
		//While we have the open connection, go and get the actual bytes
		Blob blobData = img.getBlobData();
		int length = (int)blobData.length();
		byte[] bytes = blobData.getBytes(1L, length);
		img.setBytes(bytes);
	}

	@Override
	public String writeValuesToStatement(PreparedStatement statement, int i, String statementStr) throws SQLException
	{
		statementStr = writeBlob(statement, i++, blobData, statementStr);
		statementStr = writeString(statement, i++, filepath, statementStr);
		statementStr = writeBoolean(statement, i++, preset, statementStr);
		
		return statementStr;
	}
	
	@Override
	public long getGameId()
	{
		return -1;
	}
	
	public static PlayerImageEntity factoryAndSave(File file, boolean preset)
	{
		try
		{
			Path path = file.toPath();
			byte[] bytes = Files.readAllBytes(path);
			return factoryAndSave(file.getAbsolutePath(), bytes, preset);
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t);
			return null;
		}
	}
	private static PlayerImageEntity factoryAndSave(String filepath, byte[] fileBytes, boolean preset)
	{
		try
		{
			PlayerImageEntity pi = new PlayerImageEntity();
			pi.assignRowId();
			
			Blob blobData = new SerialBlob(fileBytes);
			pi.setBlobData(blobData);
			pi.setFilepath(filepath);
			pi.setPreset(preset);
			pi.setBytes(fileBytes);
			pi.saveToDatabase();
			
			return pi;
		}
		catch (SQLException se)
		{
			Debug.logSqlException("Instantiating SerialBlob for bytes of length " + fileBytes.length, se);
			return null;
		}
	}
	
	public static ImageIcon retrieveImageIconForId(long rowId)
	{
		ImageIcon cachedIcon = hmRowIdToImageIcon.get(rowId);
		if (cachedIcon != null)
		{
			return cachedIcon;
		}
		
		//Retrieve the entity, turn it into an ImageIcon and cache it.
		PlayerImageEntity ent = new PlayerImageEntity().retrieveForId(rowId);
		ImageIcon icon = ent.getAsImageIcon();
		
		hmRowIdToImageIcon.put(rowId, icon);
		return icon;
	}
	
	public ImageIcon getAsImageIcon()
	{
		return new ImageIcon(bytes);
	}
	
	private static void createPresets()
	{
		int size = avatarPresets.length;
		Debug.append("Creating " + size + " avatar presets");
		
		for (int i=0; i<avatarPresets.length; i++)
		{
			String resourceLocation = "/avatars/" + avatarPresets[i] + ".png";
			byte[] bytes = FileUtil.getByteArrayForResource(resourceLocation);
			factoryAndSave("rsrc:" + resourceLocation, bytes, true);
		}
	}
	
	@Override
	public boolean createTable()
	{
		boolean createdTable = super.createTable();
		if (createdTable)
		{
			createPresets();
		}
		
		return createdTable;
	}

	/**
	 * Gets / Sets
	 */
	public Blob getBlobData()
	{
		return blobData;
	}
	public void setBlobData(Blob blobData)
	{
		this.blobData = blobData;
	}
	public String getFilepath()
	{
		return filepath;
	}
	public void setFilepath(String filepath)
	{
		this.filepath = filepath;
	}
	public boolean getPreset()
	{
		return preset;
	}
	public void setPreset(boolean preset)
	{
		this.preset = preset;
	}
	
	/**
	 * Memory Gets / Sets
	 */
	public byte[] getBytes()
	{
		return bytes;
	}
	public void setBytes(byte[] bytes)
	{
		this.bytes = bytes;
	}
}
