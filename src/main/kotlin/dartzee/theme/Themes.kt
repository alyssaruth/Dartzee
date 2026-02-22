package dartzee.theme

import dartzee.`object`.ColourWrapper
import java.awt.Color

private val lightOrange = Color.decode("#ff8200")
private val orange = Color.decode("#CF5704")
private val pastelGreen = Color.decode("#dcf9a8")
private val easterYellow = Color.decode("#ffebaf")

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

private val easterDartboardColours =
    ColourWrapper(
        easterYellow.darker(),
        easterYellow,
        easterYellow,
        pastelGreen,
        pastelGreen.darker(),
        pastelGreen.darker(),
        easterYellow,
        pastelGreen.darker(),
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

    val EASTER =
        Theme(
            "easter",
            Color.decode("#c1f0fb"),
            Color.decode("#8aadd3"),
            Color.decode("#e0cdff"),
            Color.decode("#f9ceee"),
            fontColor = Color.DARK_GRAY,
            dartboardColours = easterDartboardColours,
        )
}
