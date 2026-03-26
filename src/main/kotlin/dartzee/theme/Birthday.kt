package dartzee.theme

import dartzee.bean.DartLabel
import dartzee.`object`.ColourWrapper
import java.awt.Color
import java.awt.Point
import javax.swing.ImageIcon

private val lightBrown = Color.decode("#542e15")
private val darkBrown = Color.decode("#4b270f")
private val lightPink = Color.decode("#b25f67")
private val darkPink = Color.decode("#813c3f")

private val dartboardColours =
    ColourWrapper(
        lightBrown,
        lightPink,
        darkPink,
        darkBrown,
        darkPink,
        lightPink,
        darkPink,
        lightPink,
    )

private fun dartFactory(pt: Point): DartLabel {
    val icon = ImageIcon(Theme::class.java.getResource("/theme/birthday/darts/candle-red.png"))
    val label = DartLabel(icon).also { it.location = Point(pt.x, pt.y - icon.iconHeight) }
    return label
}

val Themes.BIRTHDAY: Theme
    get() =
        Theme(
            ThemeId.Birthday,
            "Celebrate with cake, balloons and confetti",
            Color.decode("#0000CD"),
            Color.decode("#90D5FF"),
            Color.white,
            lightBackground = Color.decode("#FDFBD4"),
            dartboardColours = dartboardColours,
            dartFactory = ::dartFactory,
        )
