package dartzee.theme

import dartzee.`object`.ColourWrapper
import java.awt.Color

private val lightOrange = Color.decode("#ff8200")
private val orange = Color.decode("#CF5704")
private val halloweenDartboardColours =
    ColourWrapper(
        lightOrange,
        orange,
        orange,
        Color.decode("#009900"),
        Color.GREEN,
        Color.GREEN,
        orange,
        Color.GREEN,
    )

object Themes {
    val HALLOWEEN =
        Theme(
            "halloween",
            lightOrange,
            orange,
            Color.decode("#32172a"),
            lightBackground = Color.decode("#DAB1DA"),
            fontColor = Color.decode("#880808"),
            dartboardColours = halloweenDartboardColours,
        )
}
