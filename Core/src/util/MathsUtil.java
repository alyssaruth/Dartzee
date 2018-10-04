package util;

import java.util.ArrayList;

import object.HandyArrayList;

public final class MathsUtil
{
	public static boolean isPrime(long x)
	{
		int maxFactor = (int)Math.floor(Math.sqrt(x));
		for (int i=2; i<=maxFactor; i++)
		{
			if (x % i == 0)
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean primeFactorisationIsAllSingleDigits(long x)
	{
		long p = 2;
		while (x > 1)
		{
			long remainder = x % p;
			if (remainder == 0)
			{
				x = x / p;
			}
			else
			{
				p = getNextPrime(p);
				
				//Stop the factorisation now!
				if (("" + p).length() > 1)
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static HandyArrayList<Long> primeFactorise(long x)
	{
		if (isPrime(x))
		{
			return HandyArrayList.factoryAdd(x);
		}
		
		HandyArrayList<Long> results = new HandyArrayList<>();
		long p = 2;
		while (x > 1)
		{
			long remainder = x % p;
			if (remainder == 0)
			{
				x = x / p;
				results.add(p);
			}
			else
			{
				p = getNextPrime(p);
			}
		}
		
		return results;
	}
	private static long getNextPrime(long x)
	{
		long ret = x+1;
		while (!isPrime(ret))
		{
			ret++;
		}
		
		return ret;
	}
	
	public static void findPerfectNumbers()
	{
		for (int i=1; i<=1000; i++)
		{
			if (isPerfect(i))
			{
				Debug.append("" + i);
			}
		}
	}
	
	public static boolean isPerfect(int x)
	{
		int factorsTotal = 0;
		for (int i=1; i<x; i++)
		{
			if ((x % i) == 0)
			{
				factorsTotal += i;
			}
		}
		
		return factorsTotal == x;
	}
	
	public static int getFactorCount(int x)
	{
		int count = 0;
		for (int i=1; i<=x; i++)
		{
			if ((x % i) == 0)
			{
				count++;
			}
		}
		
		return count;
	}
	
	public static String convertToBase(String x, int fromBase, int toBase) 
	{
		return Integer.toString(Integer.parseInt(x, fromBase),toBase);
	}
	
	public static double getPercentage(int count, double total)
	{
		return (double)Math.round(1000 * count/total)/10;
	}
	
	public static HandyArrayList<Integer> getDigits(long number)
	{
		HandyArrayList<Integer> ret = new HandyArrayList<>();
		
		String numberStr = "" + number;
		for (int i=0; i<numberStr.length(); i++)
		{
			String digitStr = numberStr.substring(i, i+1);
			ret.add(Integer.parseInt(digitStr));
		}
		
		return ret;
	}
	
	public static long getDigitProduct(long number)
	{
		long product = 1;
		
		ArrayList<Integer> digits = getDigits(number);
		for (int digit : digits)
		{
			//digit = (int)Math.pow(digit, digitPower);
			product = product * digit;
		}
		
		return product;
	}
	public static int getDigitSum(long number, int digitPower)
	{
		int sum = 0;
		
		ArrayList<Integer> digits = getDigits(number);
		for (int digit : digits)
		{
			digit = (int)Math.pow(digit, digitPower);
			sum = sum + digit;
		}
		
		return sum;
	}
	
	public static int getNthDigit(long number, int digit)
	{
		String numberStr = "" + number;
		String digitStr = numberStr.substring(digit-1, digit);
		return Integer.parseInt(digitStr);
	}
	
	public static int doCollatz(int i)
	{
		if ((i % 2) == 0)
		{
			return i / 2;
		}
		else
		{
			return 3*i + 1;
		}
	}
	
	/**
	 * Pass a number with some unknowns as X's. This will return all possible values.
	 * 
	 * E.g. Pass in 10X01. Will get [10001, 10101, 10201, ..., 10901] returned.
	 */
	public static ArrayList<Long> getAllPossibleSubstitutions(String numberStr)
	{
		int substitutionsNeeded = 0;
		for (int i=0; i<numberStr.length(); i++)
		{
			String character = numberStr.substring(i, i+1);
			if (character.equals("X"))
			{
				substitutionsNeeded++;
			}
		}
		
		//Need to go from 00000 - 99999
		//But if it starts with an X, we don't want any leading zeros. SO start from 10000 instead.
		long min = 0;
		if (numberStr.startsWith("X"))
		{
			min = (long)Math.pow(10, substitutionsNeeded - 1);
		}
		long max = (long)Math.pow(10, substitutionsNeeded);
		
		Debug.append("About to do " + (max-min) + " loops...");
		ArrayList<Long> ret = new ArrayList<>();
		for (long i=min; i<max; i++)
		{
			Debug.logProgress(i+1, max, 1, "substitutions");
			
			String subStr = "" + i;
			while (subStr.length() < substitutionsNeeded)
			{
				subStr = "0" + subStr;
			}
			
			long result = doSubstitution(numberStr, subStr);
			ret.add(result);
		}
		
		return ret;
	}
	public static long doSubstitution(String numberWithBlanks, String numbersToSubstitute)
	{
		String resultStr = "";
		for (int i=0; i<numberWithBlanks.length(); i++)
		{
			String charStr = numberWithBlanks.substring(i, i+1);
			if (charStr.equals("X"))
			{
				resultStr += numbersToSubstitute.charAt(0);
				numbersToSubstitute = numbersToSubstitute.substring(1, numbersToSubstitute.length());
			}
			else
			{
				resultStr += charStr;
			}
		}
		
		return Long.parseLong(resultStr);
	}
	
	public static boolean isTriangleNumber(long number)
	{
		int i = 1;
		while (number > 0)
		{
			number = number - i;
			i++;
		}
		
		return number == 0;
	}
	
	public static boolean isPowerOf(long number, long power)
	{
		long powerSeq = power;
		while (powerSeq < number)
		{
			powerSeq = powerSeq * power;
		}
		
		return powerSeq == number;
	}
}
