package dartzee.theme

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.parser.SVGLoader
import dartzee.preferences.Preferences
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.now
import dartzee.utils.ResourceCache
import java.awt.Color
import java.awt.Font
import java.io.BufferedInputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sound.sampled.AudioSystem
import javax.swing.ImageIcon

val DEFAULT_BACKGROUND = Color(214, 217, 223)
val DEFAULT_BUTTON_COLOUR = Color(169, 176, 190)
val CLASSIC_THEME_DESC = "The original grey-on-grey, like it's still the 1990s."

typealias FestivalFinder = (Int) -> Pair<LocalDate, LocalDate>

data class FestivalInfo(val finder: FestivalFinder, val nextDueDesc: String)

object Themes

fun themeMap() = listOf(Themes.EASTER, Themes.OKTOBERFEST, Themes.HALLOWEEN).associateBy { it.id }

fun themeDescription(id: ThemeId): String {
    val basicInfo = basicDescription(id)
    val theme = themeMap()[id]

    if (theme?.isLocked() ?: false) {
        return "This theme hasn't unlocked yet. Wait and see!"
    }

    val festivalInfo = theme?.festivalInfo
    return if (festivalInfo == null) {
        basicInfo
    } else if (getAutomaticThemeForDate() == theme) {
        "$basicInfo\n\nCurrently active - will end on ${festivalInfo.finder(now.year).second.fmt()}."
    } else {
        "$basicInfo\n\n${festivalInfo.nextDueDesc} on ${nextDue(now, festivalInfo.finder).fmt()}."
    }
}

private fun LocalDate.fmt() = format(DateTimeFormatter.ofPattern("d MMM uuuu"))

private fun basicDescription(id: ThemeId): String =
    when (id) {
        ThemeId.None -> CLASSIC_THEME_DESC
        else -> themeMap().getValue(id).description
    }

fun autoApplyTheme() {
    InjectedThings.theme = pickTheme()
    InjectedThings.theme?.apply()
}

private fun nextDue(now: LocalDate, finder: FestivalFinder): LocalDate {
    val thisYear = finder(now.year).first

    if (now.isBefore(thisYear)) {
        return thisYear
    }

    return finder(now.year + 1).first
}

fun pickTheme(): Theme? {
    val autoTheme = getAutomaticThemeForDate()
    if (autoTheme != null) {
        return autoTheme
    }

    val themeId = InjectedThings.preferenceService.get(Preferences.theme)
    return themeMap()[themeId]
}

fun getAutomaticThemeForDate(): Theme? =
    themeMap().values.find { theme -> now.inRange(theme.festivalInfo?.finder) }

private fun LocalDate.inRange(finder: FestivalFinder?): Boolean {
    finder ?: return false
    val (start, end) = finder(year)
    return isBefore(end.plusDays(1)) && isAfter(start.minusDays(1))
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

fun themedIcon(path: String, theme: Theme? = InjectedThings.theme): ImageIcon {
    val default = ImageIcon(Theme::class.java.getResource(path))

    return if (theme == null || theme.isLocked()) {
        default
    } else {
        theme.icon(path) ?: default
    }
}
