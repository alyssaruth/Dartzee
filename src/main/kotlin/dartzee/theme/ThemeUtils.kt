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
        return Themes.HALLOWEEN
    }

    if (now.month == Month.OCTOBER && now.dayOfMonth >= 24) {
        return Themes.HALLOWEEN
    }

    return null
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
