package dartzee.theme

import dartzee.game.GameType
import dartzee.`object`.ColourWrapper
import dartzee.screen.animation.Animation
import dartzee.screen.animation.DartScoreTrigger
import dartzee.screen.animation.IAnimation
import dartzee.screen.animation.IAnimationTrigger
import dartzee.screen.animation.PlayerVictory
import java.awt.Color
import java.time.LocalDate

private val pastelGreen = Color.decode("#dcf9a8")
private val easterYellow = Color.decode("#ffebaf")

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

private val easterAnimations: List<Pair<IAnimationTrigger, IAnimation>> =
    listOf(PlayerVictory to Animation("thats-all-folks", "/theme/easter/horrific/bugs-bunny.png")) +
        GameType.values().flatMap { gameType ->
            listOf(
                DartScoreTrigger(gameType, 0) to
                    Animation("egg-crack", "/theme/easter/horrific/egg-crack.png")
            )
        }

val Themes.EASTER: Theme
    get() =
        Theme(
            ThemeId.Easter,
            "Bunnies & pastel colours!",
            Color.decode("#c1f0fb"),
            Color.decode("#8aadd3"),
            Color.decode("#e0cdff"),
            Color.decode("#f9ceee"),
            linkColour = Color.BLUE,
            fontColor = Color.DARK_GRAY,
            dartboardColours = easterDartboardColours,
            menuFontSize = 20f,
            animations = easterAnimations.toMap(),
            bannerScaleFactor = 0.5,
            bannerOffset = 0.3,
            festivalInfo = FestivalInfo(::findEaster, "Will next pop up"),
        )

private fun findEaster(year: Int): Pair<LocalDate, LocalDate> {
    val easterSunday = findEasterSunday(year)
    return easterSunday.minusDays(8) to easterSunday
}

/** https://en.wikipedia.org/wiki/Date_of_Easter#Gauss's_Easter_algorithm */
@Suppress("VariableNaming")
private fun findEasterSunday(year: Int): LocalDate {
    val a = year % 19
    val b = year % 4
    val c = year % 7
    val k = year / 100
    val p = (13 + 8 * k) / 25
    val q = k / 4
    val M = (15 - p + k - q) % 30
    val N = (4 + k - q) % 7
    val d = (19 * a + M) % 30
    val e = (2 * b + 4 * c + 6 * d + N) % 7

    if (d == 29 && e == 6) {
        return LocalDate.of(year, 4, 19)
    } else if (d == 28 && e == 6 && ((11 * M + 11) % 30 < 10)) {
        return LocalDate.of(year, 4, 18)
    }

    val H = 22 + d + e
    if (H <= 31) {
        return LocalDate.of(year, 3, H)
    }
    return LocalDate.of(year, 4, H - 31)
}
