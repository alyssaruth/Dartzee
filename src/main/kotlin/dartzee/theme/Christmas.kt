package dartzee.theme

import dartzee.core.util.toRomanNumerals
import dartzee.game.GameType
import dartzee.screen.animation.Animation
import dartzee.screen.animation.CompositeAnimation
import dartzee.screen.animation.DartScoreTrigger
import dartzee.screen.animation.IAnimation
import dartzee.screen.animation.IAnimationTrigger
import java.awt.Color
import java.awt.Point
import java.awt.Rectangle
import java.time.LocalDate
import java.time.Month

val gold: Color = Color.decode("#f2c49b")

private val humbugAnimation =
    CompositeAnimation(
        listOf(Animation("humbug-muppet", "/theme/christmas/horrific/scrooge-muppet.png")) +
            (1..2).map {
                Animation("humbug-stewart-$it", "/theme/christmas/horrific/scrooge-stewart.png")
            }
    )

private val numberOneCustomer =
    CompositeAnimation(
        (1..3).map { Animation("number-one-customer$it", "/theme/christmas/horrific/arnold.png") }
    )

private val animations: List<Pair<IAnimationTrigger, IAnimation>> =
    GameType.values().flatMap { gameType ->
        listOf(
            DartScoreTrigger(gameType, 1) to numberOneCustomer,
            DartScoreTrigger(gameType, 3) to
                Animation("hans3", "/theme/christmas/horrific/hans-gruber.png"),
            DartScoreTrigger(gameType, 4) to
                Animation("hans4", "/theme/christmas/horrific/hans-gruber.png"),
            DartScoreTrigger(gameType, 0) to humbugAnimation,
        )
    }

private fun getBannerDetails(
    svgBounds: Rectangle,
    dartboardCenter: Point,
): List<BannerRenderDetails> {
    val topBanner =
        BannerRenderDetails(
            "Merry",
            (svgBounds.height * 0.15).toInt(),
            Point(dartboardCenter.x, dartboardCenter.y - (0.05 * svgBounds.height).toInt()),
        )

    val bottomBanner =
        BannerRenderDetails(
            "Christmas!",
            (svgBounds.height * 0.15).toInt(),
            Point(dartboardCenter.x, dartboardCenter.y + (0.1 * svgBounds.height).toInt()),
        )

    return listOf(topBanner, bottomBanner)
}

val Themes.CHRISTMAS: Theme
    get() =
        Theme(
            ThemeId.Christmas,
            "Ho ho ho",
            Color.decode("#9e000e"),
            Color.decode("#73020c"),
            Color.decode("#062601"),
            Color.decode("#385025"),
            linkColour = gold,
            fontColor = Color.WHITE,
            dartboardColours = ChristmasDartboardPainter(),
            menuFontSize = 24f,
            animations = animations.toMap(),
            bannerTextRenderer = ::getBannerDetails,
            festivalInfo = FestivalInfo(::findChristmas, "Will next pop up"),
            // unlockDate = LocalDate.of(2026, Month.DECEMBER, 1),
            menuAnimation = Snowfall(),
            resultConverter = { toRomanNumerals(it) + " " },
            dartboardFlasher = true,
        )

private fun findChristmas(year: Int) =
    LocalDate.of(year, Month.DECEMBER, 1) to LocalDate.of(year, Month.DECEMBER, 26)
