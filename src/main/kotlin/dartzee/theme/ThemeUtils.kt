package dartzee.theme

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.parser.SVGLoader
import dartzee.bean.DartLabel
import dartzee.preferences.Preferences
import dartzee.utils.InjectedThings
import dartzee.utils.ResourceCache
import java.awt.Color
import java.awt.Font
import java.awt.Point
import java.io.BufferedInputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sound.sampled.AudioSystem
import javax.swing.ImageIcon

val DEFAULT_BACKGROUND = Color(214, 217, 223)
val DEFAULT_BUTTON_COLOUR = Color(169, 176, 190)

typealias FestivalFinder = (Int) -> Pair<LocalDate, LocalDate>

typealias DartFactory = (Point) -> DartLabel

data class FestivalInfo(val finder: FestivalFinder, val nextDueDesc: String)

object Themes

fun themeMap() =
    listOf(Themes.EASTER, Themes.OKTOBERFEST, Themes.HALLOWEEN, Themes.BIRTHDAY).associateBy {
        it.id
    }

fun themeDescription(id: ThemeId, now: LocalDate): String {
    val basicInfo = basicDescription(id)
    val theme = themeMap()[id]
    val festivalInfo = theme?.festivalInfo
    return if (festivalInfo == null) {
        basicInfo
    } else if (getAutomaticThemeForDate(now) == theme) {
        "$basicInfo\n\nCurrently active - will end on ${festivalInfo.finder(now.year).second.fmt()}."
    } else {
        "$basicInfo\n\n${festivalInfo.nextDueDesc} on ${nextDue(now, festivalInfo.finder).fmt()}."
    }
}

private fun LocalDate.fmt() = format(DateTimeFormatter.ofPattern("d MMM uuuu"))

private fun basicDescription(id: ThemeId): String =
    when (id) {
        ThemeId.None -> "The original grey-on-grey, like it's still the 1990s."
        else -> themeMap().getValue(id).description
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

fun getAutomaticThemeForDate(now: LocalDate): Theme? =
    themeMap().values.find { theme -> now.inRange(theme.festivalInfo?.finder) }

private fun LocalDate.inRange(finder: FestivalFinder?): Boolean {
    finder ?: return false
    val (start, end) = finder(year)
    return isBefore(end.plusDays(1)) && isAfter(start.minusDays(1))
}

fun fontForResource(resourcePath: String): Font? {
    val fontStream =
        Theme::class.java.getResourceAsStream(resourcePath)
            ?: Theme::class.java.getResourceAsStream(resourcePath.replace(".ttf", ".otf"))
            ?: return null

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

fun makeDartLabel(pt: Point): DartLabel {
    val themed = InjectedThings.theme?.dartFactory?.invoke(pt)

    return themed ?: DartLabel().apply { location = pt }
}
