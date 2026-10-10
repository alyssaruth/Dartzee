package dartzee.theme

import dartzee.core.util.toRomanNumerals
import dartzee.game.GameType
import dartzee.screen.animation.Animation
import dartzee.screen.animation.DartScoreTrigger
import dartzee.screen.animation.IAnimation
import dartzee.screen.animation.IAnimationTrigger
import java.awt.Color
import java.time.LocalDate
import java.time.Month

val gold = Color.decode("#f2c49b")

private val animations: List<Pair<IAnimationTrigger, IAnimation>> =
    GameType.values().flatMap { gameType ->
        listOf(
            DartScoreTrigger(gameType, 3) to
                Animation("hans3", "/theme/christmas/horrific/hans-gruber.png"),
            DartScoreTrigger(gameType, 4) to
                Animation("hans4", "/theme/christmas/horrific/hans-gruber.png"),
        )
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
            bannerTextRenderer = simpleBannerRenderer(ThemeId.Christmas),
            festivalInfo = FestivalInfo(::findChristmas, "Will next pop up"),
            // unlockDate = LocalDate.of(2026, Month.DECEMBER, 1),
            menuAnimation = Snowfall(),
            resultConverter = { toRomanNumerals(it) + " " },
        )

private fun findChristmas(year: Int) =
    LocalDate.of(year, Month.DECEMBER, 1) to LocalDate.of(year, Month.DECEMBER, 26)
