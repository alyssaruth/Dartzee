package object;

import java.util.ArrayList;
import java.util.Comparator;

public class HashMapCountInteger extends HashMapCount<Integer>
{
    public double calculateMedian()
    {
        if (isEmpty())
        {
            return 0;
        }

        ArrayList<Integer> allKeys = getFlattenedOrderedList(Comparator.comparingInt(k -> k));

        int n = allKeys.size();
        if (n % 2 == 0)
        {
            //Even, so we want either side of the middle value and then to take the average of them.
            int bigIx = n/2;
            int smallIx = (n/2) - 1;

            double sum = allKeys.get(bigIx) + allKeys.get(smallIx);

            return sum/2;
        }
        else
        {
            //Odd, so we just want the middle value. It's (n-1)/2 because of stupid index starting at 0 not 1.
            int ix = (n-1)/2;
            return allKeys.get(ix);
        }
    }
}
