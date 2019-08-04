package burlton.core.code.util;

import java.util.ArrayList;

public class StringUtil 
{
	public static ArrayList<String> getListFromDelims(String delimitedStr, String delimChar)
	{
		ArrayList<String> list = new ArrayList<>();
		
		//Special case for no delimeter, i.e. "1234" -> {"1", "2", "3", "4"}
		if (delimChar.isEmpty())
		{
			for (int i=0; i<delimitedStr.length(); i++)
			{
				String character = String.valueOf(delimitedStr.charAt(i));
				list.add(character);
			}
			
			return list;
		}
		
		int index = 0;
		while (index > -1)
		{
			int newIndex = delimitedStr.indexOf(delimChar, index);
			int indexToUse = newIndex;
			if (indexToUse == -1)
			{
				indexToUse = delimitedStr.length();
			}
			
			String word = delimitedStr.substring(index, indexToUse);
			list.add(word);
			
			index = newIndex;
			if (index > -1)
			{
				index++;
			}
		}
		
		return list;
	}
	
	public static String convertOrdinalToText(int position)
	{
		if (position == -1)
		{
			return "";
		}
		
	    String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
	    
	    int remainder = position % 100;
	    if (remainder == 11
	      || remainder == 12
	      || remainder == 13)
	    {
	    	//Special cases
	    	return position + "th";
	    }
	    else
	    {
	    	return position + suffixes[position%10];
	    }
	}
}
