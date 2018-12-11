package burlton.core.code.util;

import burlton.core.code.obj.HandyArrayList;

import java.math.BigInteger;
import java.util.ArrayList;

public final class MathsUtil
{
	public static boolean isPrime(BigInteger x)
	{
		//Can't square root a BigInteger easily. But can estimate it.
		double numberOfDigits = x.toString().length();
		if (numberOfDigits == 1)
		{
			return isPrime(x.longValue());
		}
		
		if (!x.isProbablePrime(10))
		{
			return false;
		}
		
		int digitsRounded = (int)Math.ceil(numberOfDigits/2);
		BigInteger max = BigInteger.valueOf(10);
		max = max.pow(digitsRounded);
		
		
		for (BigInteger factor = BigInteger.valueOf(2); (max.compareTo(factor) == 1); factor = factor.add(BigInteger.valueOf(1)))
		{
			BigInteger[] result = x.divideAndRemainder(factor);
			if (result[1].equals(BigInteger.valueOf(0)))
			{
				return false;
			}
		}
		
		return true;
	}
	
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
	
	public static ArrayList<BigInteger> getAllPrimeFactorsLessThan(BigInteger x, int threshold)
	{
		if (isPrime(x))
		{
			return HandyArrayList.factoryAdd(x);
		}
		
		HandyArrayList<BigInteger> results = new HandyArrayList<>();
		BigInteger p = BigInteger.valueOf(2);
		while (x.compareTo(BigInteger.valueOf(1)) == 1 //X > 1
		  && p.compareTo(BigInteger.valueOf(threshold)) == -1) //P < threshold
		{
			BigInteger[] division = x.divideAndRemainder(p);
			if (division[1].equals(BigInteger.valueOf(0)))
			{
				x = division[0];
				results.add(p);
			}
			else
			{
				p = getNextPrime(p);
			}
		}
		
		return results;
	}
	
	public static long getLargestPrimeFactor(long x)
	{
		ArrayList<Long> factors = MathsUtil.primeFactorise(x);
		
		return factors.stream().mapToLong(l -> l.longValue()).max().getAsLong();
	}
	
	public static BigInteger getFirstPrimeFactor(BigInteger x)
	{
		if (isPrime(x))
		{
			return x;
		}
		
		BigInteger p = BigInteger.valueOf(2);
		while (x.compareTo(p) == 1) //x > p
		{
			BigInteger[] division = x.divideAndRemainder(p);
			if (division[1].equals(BigInteger.valueOf(0)))
			{
				return p;
			}
			else
			{
				p = getNextPrime(p);
			}
		}
		
		Debug.stackTrace("Prime check broken? Number was [" + x + "], found 0 prime factors");
		return x;
	}
	
	public static HandyArrayList<BigInteger> primeFactorise(BigInteger x)
	{
		if (isPrime(x))
		{
			return HandyArrayList.factoryAdd(x);
		}
		
		HandyArrayList<BigInteger> results = new HandyArrayList<>();
		BigInteger p = BigInteger.valueOf(2);
		while (x.compareTo(BigInteger.valueOf(1)) == 1)
		{
			BigInteger[] division = x.divideAndRemainder(p);
			if (division[1].equals(BigInteger.valueOf(0)))
			{
				x = division[0];
				results.add(p);
				
				if (isPrime(x))
				{
					results.add(x);
					return results;
				}
			}
			else
			{
				p = getNextPrime(p);
			}
		}
		
		return results;
	}
	private static BigInteger getNextPrime(BigInteger x)
	{
		BigInteger ret = x.add(BigInteger.valueOf(1));
		while (!isPrime(ret))
		{
			ret = ret.add(BigInteger.valueOf(1));
		}
		
		return ret;
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
	
	public static HandyArrayList<Long> getFactors(long x)
	{
		HandyArrayList<Long> ret = new HandyArrayList<>();
		for (long i=1; i<=x; i++)
		{
			if ((x % i) == 0)
			{
				ret.add(i);
			}
		}
		
		return ret;
	}
	
	public static int getFactorCount(int x)
	{
		return getFactors(x).size();
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
			Debug.logProgress(i-min+1, max-min, 1, "substitutions");
			
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
	
	public static double round(double number, int decimalPlaces)
	{
		double product = Math.pow(10, decimalPlaces);
		
		int rounded = (int)(product * number);
		
		return rounded / product;
	}
	
	public static ArrayList<Long> factoryAll(int length)
	{
		ArrayList<Long> ret = new ArrayList<>();
		
		long min = (long)Math.pow(10, length-1);
		long max = (long)Math.pow(10, length);
		for (long i=min; i<max; i++)
		{
			ret.add(i);
		}
		
		return ret;
	}
	
	public static boolean isDivisible(long number, long factor)
	{
		return (number % factor) == 0;
	}
	
	public static long gcd(long l1, long l2)
	{
		ArrayList<Long> factorsOne = primeFactorise(l1);
		ArrayList<Long> factorsTwo = primeFactorise(l2);
		
		ArrayList<Long> factorsIntersect = intersect(factorsOne, factorsTwo);
		return factorsIntersect.stream().reduce(1L, (a, b) -> a * b);
	}
	
	public static ArrayList<Long> intersect(ArrayList<Long> listOne, ArrayList<Long> listTwo)
	{
		ArrayList<Long> intersection = new ArrayList<>();
		for (long l : listOne)
		{
			if (listTwo.contains(l))
			{
				intersection.add(l);
				listTwo.remove(Long.valueOf(l));
			}
		}
		
		return intersection;
	}
	
	public static boolean areCoprime(long l1, long l2)
	{
		return gcd(l1, l2) == 1;
	}

	public static long factorial(long l)
	{
		int result = 1;
		for (long i=l; i>0; i--)
		{
			result *= i;
		}

		return result;
	}

}
