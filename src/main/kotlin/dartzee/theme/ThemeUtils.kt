package dartzee.theme

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.parser.SVGLoader
import dartzee.bean.DartLabel
import dartzee.logging.CODE_AUDIO_ERROR
import dartzee.preferences.Preferences
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.now
import dartzee.utils.ResourceCache
import java.awt.Color
import java.awt.Font
import java.awt.Point
import java.awt.Rectangle
import java.io.BufferedInputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sound.sampled.AudioSystem
import javax.swing.ImageIcon

val DEFAULT_BACKGROUND = Color(214, 217, 223)
val DEFAULT_BUTTON_COLOUR = Color(169, 176, 190)
const val CLASSIC_THEME_DESC = "The original grey-on-grey, like it's still the 1990s."

typealias FestivalFinder = (Int) -> Pair<LocalDate, LocalDate>

typealias DartFactory = (Point) -> DartLabel

data class FestivalInfo(val finder: FestivalFinder, val nextDueDesc: String)

data class BannerRenderDetails(
    val text: String,
    val fontHeight: Int,
    val textCenter: Point,
    val maxWidth: Int = Int.MAX_VALUE,
)

typealias BannerTextRenderer = (Rectangle, Point) -> List<BannerRenderDetails>

fun simpleBannerRenderer(themeId: ThemeId): BannerTextRenderer = { svgBounds, dartboardCenter ->
    listOf(BannerRenderDetails(themeId.name, (svgBounds.height * 0.8).toInt(), dartboardCenter))
}

object Themes

fun themeMap() =
    listOf(Themes.EASTER, Themes.OKTOBERFEST, Themes.HALLOWEEN, Themes.BIRTHDAY).associateBy {
        it.id
    }

fun themeDescription(id: ThemeId): String {
    val basicInfo = basicDescription(id)
    val theme = themeMap()[id]

    if (theme?.isLocked() ?: false) {
        return "This theme hasn't unlocked yet. Wait and see!"
    }

    val festivalInfo = theme?.festivalInfo
    return if (id == ThemeId.Birthday) {
        "$basicInfo\n\n${getExtraBirthdayDescription()}"
    } else if (festivalInfo == null) {
        basicInfo
    } else if (getAutomaticThemeForDate() == theme) {
        "$basicInfo\n\nCurrently active - will end on ${festivalInfo.finder(now.year).second.fmt()}."
    } else {
        "$basicInfo\n\n${festivalInfo.nextDueDesc} on ${nextDue(now, festivalInfo.finder).fmt()}."
    }
}

fun LocalDate.fmt(): String = format(DateTimeFormatter.ofPattern("d MMM uuuu"))

private fun basicDescription(id: ThemeId): String =
    when (id) {
        ThemeId.None -> CLASSIC_THEME_DESC
        else -> themeMap().getValue(id).description
    }

fun autoApplyTheme() {
    InjectedThings.birthdayInfo = computeBirthdayInfo()
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
    if (InjectedThings.birthdayInfo != null) {
        return Themes.BIRTHDAY
    }

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
    val fontStream =
        Theme::class.java.getResourceAsStream(resourcePath)
            ?: Theme::class.java.getResourceAsStream(resourcePath.replace(".ttf", ".otf"))
            ?: return null

    return Font.createFont(Font.TRUETYPE_FONT, fontStream)
}

fun clipForResource(resourcePath: String): AudioClip? {
    val stream = Theme::class.java.getResourceAsStream(resourcePath) ?: return null

    val audioStream = AudioSystem.getAudioInputStream(BufferedInputStream(stream)) ?: return null
    return try {
        AudioClip(audioStream)
    } catch (e: Exception) {
        logger.warn(CODE_AUDIO_ERROR, "Failed to prepare AudioClip $resourcePath", e)
        return null
    }
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

fun makeDartLabel(pt: Point): DartLabel {
    val themed = InjectedThings.theme?.dartFactory?.invoke(pt)

    return themed ?: DartLabel().apply { location = pt }
}
