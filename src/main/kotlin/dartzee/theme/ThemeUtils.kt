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
    InjectedThings.theme = pickThemeForDate()
    InjectedThings.theme?.apply()
}

private fun pickThemeForDate(): Theme? {
    if (DartsClient.devMode) {
        return Themes.HALLOWEEN
    }

    val currentDate = LocalDate.now()
    if (currentDate.month == Month.OCTOBER && currentDate.dayOfMonth >= 24) {
        return Themes.HALLOWEEN
    }

    return null
}

fun applyCurrentTheme() {
    InjectedThings.theme?.apply()
    ScreenCache.fireAppearancePreferencesChanged()
}

fun fontForResource(resourcePath: String): Font {
    val fontStream =
        Theme::class.java.getResourceAsStream(resourcePath)
            ?: throw RuntimeException("Font not found for path $resourcePath")

    return Font.createFont(Font.TRUETYPE_FONT, fontStream)
}

fun getBaseFont(): Font = InjectedThings.theme?.font ?: ResourceCache.BASE_FONT

fun themedIcon(path: String): ImageIcon {
    return InjectedThings.theme?.icon(path) ?: ImageIcon(Theme::class.java.getResource(path))
}

fun Theme.icon(path: String): ImageIcon? {
    return Theme::class.java.getResource("/theme/$name$path")?.let(::ImageIcon)
}
