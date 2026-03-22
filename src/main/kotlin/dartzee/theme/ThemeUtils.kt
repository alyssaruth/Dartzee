package dartzee.theme

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.parser.SVGLoader
import dartzee.preferences.Preferences
import dartzee.utils.InjectedThings
import dartzee.utils.ResourceCache
import java.awt.Font
import java.io.BufferedInputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import javax.sound.sampled.AudioSystem
import javax.swing.ImageIcon

typealias FestivalFinder = (Int) -> Pair<LocalDate, LocalDate>

fun themeMap() = listOf(Themes.EASTER, Themes.OKTOBERFEST, Themes.HALLOWEEN).associateBy { it.id }

fun themeDescription(id: ThemeId, now: LocalDate) =
    when (id) {
        ThemeId.None -> "The original grey-on-grey, like it's still the 1990s"
        ThemeId.Easter ->
            "Bunnies & pastel colours! \n\nWill next pop up on ${nextDue(now, ::findEaster)}"
        ThemeId.Oktoberfest ->
            "Time to get the Lederhosen out and crack open some German beer!\n\nHappy hour next begins on ${nextDue(now, ::findOktoberfest)}"
        ThemeId.Halloween ->
            "Zombies, eyeballs and witches have come to terrorise the app!\n\nNext haunting will start on ${nextDue(now, ::findHalloween)}"
    }

fun autoApplyTheme() {
    InjectedThings.theme = pickTheme(LocalDate.now())
    InjectedThings.theme?.apply()
}

private fun nextDue(now: LocalDate, finder: FestivalFinder): LocalDate {
    val thisYear = finder(now.year).first

    if (now.isBefore(thisYear)) {
        return thisYear
    }

    return finder(now.year + 1).first
}

fun pickTheme(now: LocalDate): Theme? {
    val autoTheme = getAutomaticThemeForDate(now)
    if (autoTheme != null) {
        return autoTheme
    }

    val themeId = InjectedThings.preferenceService.get(Preferences.theme)
    return themeMap()[themeId]
}

fun getAutomaticThemeForDate(now: LocalDate): Theme? {
    return themeMap().values.find { theme -> now.inRange(theme.finder) }
}

private fun LocalDate.inRange(finder: FestivalFinder?): Boolean {
    finder ?: return false
    val (start, end) = finder(year)
    return isBefore(end.plusDays(1)) && isAfter(start.minusDays(1))
}

fun findHalloween(year: Int): Pair<LocalDate, LocalDate> {
    return LocalDate.of(year, Month.OCTOBER, 24) to LocalDate.of(year, Month.OCTOBER, 31)
}

fun findOktoberfest(year: Int): Pair<LocalDate, LocalDate> {
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

fun findEaster(year: Int): Pair<LocalDate, LocalDate> {
    val easterSunday = findEasterSunday(year)
    return easterSunday.minusDays(8) to easterSunday
}

/** https://en.wikipedia.org/wiki/Date_of_Easter#Gauss's_Easter_algorithm */
@Suppress("VariableNaming")
fun findEasterSunday(year: Int): LocalDate {
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

fun fontForResource(resourcePath: String): Font? {
    val fontStream = Theme::class.java.getResourceAsStream(resourcePath) ?: return null

    return Font.createFont(Font.TRUETYPE_FONT, fontStream)
}

fun clipForResource(resourcePath: String): AudioClip? {
    val stream = Theme::class.java.getResourceAsStream(resourcePath) ?: return null

    val audioStream = AudioSystem.getAudioInputStream(BufferedInputStream(stream)) ?: return null
    return AudioClip(audioStream)
}

fun svgForResource(resourcePath: String): SVGDocument? {
    val uri = Theme::class.java.getResource(resourcePath) ?: return null

    return SVGLoader().load(uri)
}

fun getBaseFont(): Font = InjectedThings.theme?.font ?: ResourceCache.BASE_FONT

fun themedIcon(path: String, theme: Theme? = InjectedThings.theme) =
    theme?.icon(path) ?: ImageIcon(Theme::class.java.getResource(path))
