package burlton.desktopcore.code.obj;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class LimitedDocument extends PlainDocument 
{
	private int limit;

	public LimitedDocument(int limit) 
	{
		super();
		this.limit = limit;
	}
	
	@Override
	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException 
	{
		if (str == null)
		{
			return;
		}
		
		//Never allow these as they cause problems in XML messages
		//Reverting this, XML is automatically escaped and the HTML is escaped on the client now too
		/*if (str.contains(">")
		  || str.contains("<"))
		{
			return;
		}*/
		
		if (getLength() + str.length() > limit)
		{
			return;
		}
			
		super.insertString(offset, str, attr);
	}
}