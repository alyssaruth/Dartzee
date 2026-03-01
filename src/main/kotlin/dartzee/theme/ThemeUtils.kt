package dartzee.theme

import dartzee.`object`.DartsClient
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings
import dartzee.utils.ResourceCache
import java.awt.Font
import java.time.LocalDate
import java.time.Month
import javax.swing.ImageIcon

fun autoApplyTheme() {
    InjectedThings.theme = pickThemeForDate(LocalDate.now())
    InjectedThings.theme?.apply()
}

fun pickThemeForDate(now: LocalDate): Theme? {
    if (DartsClient.devMode) {
        return Themes.EASTER
    }

    val easterSunday = findEasterSunday(now.year)
    if (now.isBefore(easterSunday.plusDays(1)) && now.isAfter(easterSunday.minusDays(9))) {
        return Themes.EASTER
    }

    if (now.month == Month.OCTOBER && now.dayOfMonth >= 24) {
        return Themes.HALLOWEEN
    }

    return null
}

/** https://en.wikipedia.org/wiki/Date_of_Easter#Gauss's_Easter_algorithm */
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

fun applyCurrentTheme() {
    InjectedThings.theme?.apply()
    ScreenCache.fireAppearancePreferencesChanged()
}

fun fontForResource(resourcePath: String): Font? {
    val fontStream = Theme::class.java.getResourceAsStream(resourcePath) ?: return null

    return Font.createFont(Font.TRUETYPE_FONT, fontStream)
}

fun getBaseFont(): Font = InjectedThings.theme?.font ?: ResourceCache.BASE_FONT

fun themedIcon(path: String) =
    InjectedThings.theme?.icon(path) ?: ImageIcon(Theme::class.java.getResource(path))

private fun Theme.icon(path: String) =
    Theme::class.java.getResource("/theme/$name$path")?.let(::ImageIcon)
