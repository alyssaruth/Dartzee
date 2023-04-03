package dartzee.screen.stats


fun List<Int>.median(): Double {
    val allKeys = sorted()

    val n = allKeys.size
    return if (n % 2 == 0)
    {
        //Even, so we want either side of the middle value and then to take the average of them.
        val bigIx = n / 2
        val smallIx = n / 2 - 1

        val sum = (allKeys[bigIx] + allKeys[smallIx]).toDouble()

        sum / 2
    }
    else
    {
        //Odd, so we just want the middle value. It's (n-1)/2 because of stupid index starting at 0 not 1.
        val ix = (n - 1) / 2
        allKeys[ix].toDouble()
    }
}