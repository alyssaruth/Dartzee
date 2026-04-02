package dartzee.theme

import dartzee.game.GameType
import dartzee.`object`.ColourWrapper
import dartzee.screen.animation.Animation
import dartzee.screen.animation.CompositeAnimation
import dartzee.screen.animation.DartScoreTrigger
import dartzee.screen.animation.IAnimation
import dartzee.screen.animation.IAnimationTrigger
import java.awt.Color
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month

private val beerSmashAnimation =
    CompositeAnimation(
        (1..3).map { Animation("smash$it", "/theme/oktoberfest/horrific/dropped-beer.png") } +
            (1..3).map { Animation("smash$it", "/theme/oktoberfest/horrific/dropped-beer-2.png") }
    )

private val oktoberfestAnimations: List<Pair<IAnimationTrigger, IAnimation>> =
    GameType.values().flatMap { gameType ->
        listOf(
            DartScoreTrigger(gameType, 9) to
                Animation("nine-pints", "/theme/oktoberfest/horrific/pints-of-lager.png"),
            DartScoreTrigger(gameType, 10) to
                Animation("blimey", "/theme/oktoberfest/horrific/blimey.png"),
            DartScoreTrigger(gameType, 18) to
                Animation("eighteen-pints", "/theme/oktoberfest/horrific/pints-of-lager.png"),
            DartScoreTrigger(gameType, 0) to beerSmashAnimation,
        )
    }

private val oktoberfestDartboardColours =
    ColourWrapper(
        Color.YELLOW,
        Color.RED,
        Color.RED,
        Color.RED,
        Color.YELLOW,
        Color.YELLOW,
        Color.RED,
        Color.YELLOW,
        fontColor = Color.decode("#e3570a"),
    )

val Themes.OKTOBERFEST: Theme
    get() =
        Theme(
            ThemeId.Oktoberfest,
            "Time to get the Lederhosen out and crack open some German beer!",
            Color.decode("#e3570a"),
            Color.decode("#764d2a"),
            Color.decode("#fbb40e"),
            Color.decode("#ffcf60"),
            linkColour = Color.decode("#880808"),
            dartboardColours = oktoberfestDartboardColours,
            bannerTextRenderer = simpleBannerRenderer(ThemeId.Oktoberfest),
            menuFontSize = 24f,
            animations = oktoberfestAnimations.toMap(),
            festivalInfo = FestivalInfo(::findOktoberfest, "Happy hour next begins"),
            unlockDate = LocalDate.of(2026, Month.SEPTEMBER, 19),
        )

private fun findOktoberfest(year: Int): Pair<LocalDate, LocalDate> {
    val germanUnityDay = LocalDate.of(year, Month.OCTOBER, 3)
    val firstSunday =
        (1..7)
            .map { LocalDate.of(year, Month.OCTOBER, it) }
            .first { it.getDayOfWeek() == DayOfWeek.SUNDAY }

    val endDate = if (firstSunday.isBefore(germanUnityDay)) germanUnityDay else firstSunday
    val daysAdded = maxOf(0, germanUnityDay.dayOfMonth - firstSunday.dayOfMonth)

    val startDate = endDate.minusDays(15L + daysAdded)
    return startDate to endDate
}
