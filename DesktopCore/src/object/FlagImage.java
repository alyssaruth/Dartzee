package object;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import util.Debug;
import util.StringUtil;

public class FlagImage
{
	private String toolTipText = "";
	private ImageIcon icon = null;
	private String codeStr = "";
	private ArrayList<String> imageNames = new ArrayList<>();
	
	public FlagImage()
	{
		
	}
	
	public FlagImage(String indexStr)
	{
		ArrayList<String> toks = StringUtil.getListFromDelims(indexStr, "~");
		codeStr = toks.remove(0);
		toolTipText = toks.remove(0);
		
		for (int i=0; i<toks.size(); i++)
		{
			String imageStr = toks.get(i);
			if (!imageStr.isEmpty())
			{
				appendImage(imageStr, "");
			}
		}
	}
	
	public void appendImage(String flagName, String orderCode)
	{
		ImageIcon image = getImageIconForFlagName(flagName);
		imageNames.add(flagName);
		if (icon == null)
		{
			icon = image;
			codeStr += orderCode;
			return;
		}
		
		codeStr += orderCode;
		
		Image currentImage = icon.getImage();
		int oldWidth = icon.getIconWidth();
		int oldHeight = icon.getIconHeight();
		
		Image newImage = image.getImage();
		int width = image.getIconWidth();
		
		BufferedImage img = new BufferedImage(oldWidth + width, oldHeight, BufferedImage.TYPE_INT_ARGB);
		
		boolean successfullyDrawnOld = img.createGraphics().drawImage(currentImage, 0, 0, null, null);
		if (!successfullyDrawnOld)
		{
			Debug.stackTrace("Failed to draw image for " + currentImage.toString());
		}
		
		boolean successfullyDrawnNew = img.createGraphics().drawImage(newImage, oldWidth, 0, null);
		if (!successfullyDrawnNew)
		{
			Debug.stackTrace("Failed to draw image for " + newImage.toString());
		}
		
		icon = new ImageIcon(img);
	}
	
	public void appendToolTip(String text)
	{
		if (toolTipText.isEmpty())
		{
			toolTipText += text;
		}
		else
		{
			toolTipText += "; " + text;
		}
	}
	
	public String getToolTipText()
	{
		return toolTipText;
	}
	public ImageIcon getIcon()
	{
		return icon;
	}
	public String getOrderStr()
	{
		return imageNames.size() + codeStr;
	}
	public String getCodeStr()
	{
		return codeStr;
	}
	public ArrayList<String> getImageNames()
	{
		return imageNames;
	}
	
	public String toIndexString()
	{
		String iconNames = StringUtil.toDelims(imageNames, "~");
		return codeStr + "~" + toolTipText + "~" + iconNames;
	}
	
	public static final ImageIcon getImageIconForFlagName(String flagName)
	{
		try
		{
			return new ImageIcon(FlagImage.class.getResource("/flags/" + flagName + ".png"));
		}
		catch (Throwable t)
		{
			Debug.stackTrace(t, "Unable to get image for " + flagName);
			return null;
		}
	}
}