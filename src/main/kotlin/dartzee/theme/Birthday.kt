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

private val candleColours = listOf("red", "yellow", "green", "blue", "pink", "purple")

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
        fontColor = Color.decode("#fff68f"),
    )

private fun dartFactory(pt: Point): DartLabel {
    val colour = candleColours.random()
    val icon = ImageIcon(Theme::class.java.getResource("/theme/birthday/darts/candle-$colour.png"))
    val label = DartLabel(icon).also { it.location = Point(pt.x, pt.y - icon.iconHeight) }
    return label
}

val Themes.BIRTHDAY: Theme
    get() =
        Theme(
            ThemeId.Birthday,
            "Celebrate with cake, balloons and confetti",
            Color.decode("#057684"),
            Color.decode("#6bbabe"),
            Color.decode("#a1cc9e"),
            menuFontSize = 26f,
            lightBackground = Color.decode("#FDFBD4"),
            dartboardColours = dartboardColours,
            bannerTextRenderer = ::getBannerDetails,
            dartFactory = ::dartFactory,
        )

private fun getBannerDetails(svgHeight: Int, dartboardCenter: Point): List<BannerRenderDetails> {
    val center = Point(dartboardCenter.x, dartboardCenter.y - (0.3 * svgHeight).toInt())

    return listOf(
        BannerRenderDetails("Happy Birthday", (svgHeight * 0.1).toInt(), center),
        BannerRenderDetails(
            "Alyssa!",
            (svgHeight * 0.1).toInt(),
            Point(dartboardCenter.x, dartboardCenter.y - (0.15 * svgHeight).toInt()),
        ),
    )
}
