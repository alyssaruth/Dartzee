package burlton.core.code.util;

public class StringUtil
{
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
