package burlton.dartzee.test

import burlton.dartzee.code.screen.Dartboard

private var dartboard: Dartboard? = null

fun borrowTestDartboard(): Dartboard
{
    if (dartboard == null)
    {
        dartboard = Dartboard(100, 100)
        dartboard!!.paintDartboard()
    }

    return dartboard!!
}