package burlton.core.code.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtil 
{
	public static int countOccurences(String str, String stringToCount)
	{
		int length = str.length();
		String strippedStr = str.replace(stringToCount, "");
		
		return length - strippedStr.length();
	}
	
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
	
	public static String toDelims(List<? extends Object> list, String delimChar)
	{
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<list.size(); i++)
		{
			if (i > 0)
			{
				sb.append(delimChar);
			}
			
			sb.append(list.get(i));
		}
		
		return sb.toString();
	}

	public static String escapeHtml(String plainText)
	{
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<plainText.length(); i++)
		{
			char c = plainText.charAt(i);
			if (c == '&'
			  || c == '"'
			  || c == '<'
			  || c == '>'
			  || c > 128)
			{
				sb.append("&#");
				sb.append((int)c);
				sb.append(";");
			}
			else
			{
				sb.append(c);
			}
		}
		
		return sb.toString();
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
