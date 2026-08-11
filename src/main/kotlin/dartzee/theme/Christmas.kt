package dartzee.theme

import dartzee.game.GameType
import dartzee.screen.animation.Animation
import dartzee.screen.animation.DartScoreTrigger
import dartzee.screen.animation.IAnimation
import dartzee.screen.animation.IAnimationTrigger
import dartzee.screen.animation.PlayerVictory
import java.awt.Color
import java.awt.Point
import java.awt.Rectangle
import java.time.LocalDate
import java.time.Month

private val darkRed = Color.decode("#800020")
private val darkGreen = Color.decode("#39AD48")

private val dartboardColours =
    ColourWrapper(
        darkRed,
        Color.RED,
        Color.RED,
        Color.GREEN,
        darkGreen,
        darkGreen,
        Color.RED,
        darkGreen,
    )

private val animations: List<Pair<IAnimationTrigger, IAnimation>> =
    listOf(PlayerVictory to Animation("thats-all-folks", "/theme/easter/horrific/bugs-bunny.png")) +
        GameType.values().flatMap { gameType ->
            listOf(
                DartScoreTrigger(gameType, 0) to
                    Animation("egg-crack", "/theme/easter/horrific/egg-crack.png")
            )
        }

private val easterBannerRenderer: BannerTextRenderer = ::getBannerDetails

val Themes.CHRISTMAS: Theme
    get() =
        Theme(
            ThemeId.Christmas,
            "Ho ho ho",
            Color.decode("#c1f0fb"),
            Color.decode("#8aadd3"),
            Color.decode("#e0cdff"),
            Color.decode("#f9ceee"),
            linkColour = Color.BLUE,
            fontColor = Color.DARK_GRAY,
            dartboardColours = dartboardColours,
            menuFontSize = 20f,
            animations = animations.toMap(),
            bannerTextRenderer = easterBannerRenderer,
            festivalInfo = FestivalInfo(::findChristmas, "Will next pop up"),
            // unlockDate = LocalDate.of(2026, Month.DECEMBER, 1),
        )

private fun getBannerDetails(
    svgBounds: Rectangle,
    dartboardCenter: Point,
): List<BannerRenderDetails> {
    val center = Point(dartboardCenter.x, dartboardCenter.y - (0.2 * svgBounds.height).toInt())

    return listOf(
        BannerRenderDetails(ThemeId.Easter.name, (svgBounds.height * 0.6).toInt(), center)
    )
}

private fun findChristmas(year: Int) =
    LocalDate.of(year, Month.DECEMBER, 1) to LocalDate.of(year, Month.DECEMBER, 26)
