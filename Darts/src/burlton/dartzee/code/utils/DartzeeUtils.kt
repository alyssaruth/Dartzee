package burlton.dartzee.code.utils

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.dartzee.DartzeeRoundResult
import java.awt.Color
import java.awt.Component

fun Component.setColoursForDartzeeResult(success: Boolean)
{
    if (success)
    {
        background = Color.GREEN
        foreground = DartsColour.getProportionalColourRedToGreen(1.0, 1, 0.5)
    }
    else
    {
        background = Color.RED
        foreground = DartsColour.getProportionalColourRedToGreen(0.0, 1, 0.5)
    }
}

fun factoryHighScoreResult(darts: List<Dart>): DartzeeRoundResult
{
    return DartzeeRoundResult(-1, success = true, score = sumScore(darts))
}
