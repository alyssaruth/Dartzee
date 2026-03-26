package dartzee.theme

import dartzee.bean.DartLabel
import dartzee.`object`.ColourWrapper
import java.awt.Color
import java.awt.Point
import javax.swing.ImageIcon

private val aqua = Color.decode("#00FFF0")
private val pink = Color.decode("#FF13F0")

private val dartboardColours =
    ColourWrapper(Color.white, pink, pink, Color.white, aqua, aqua, aqua, pink)

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
