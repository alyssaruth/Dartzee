package dartzee.theme

import dartzee.logging.CODE_PARSE_ERROR
import dartzee.logging.CODE_THEME_APPLIED
import dartzee.`object`.ColourWrapper
import dartzee.screen.animation.IAnimation
import dartzee.screen.animation.IAnimationTrigger
import dartzee.utils.InjectedThings.logger
import java.awt.Color
import java.awt.GraphicsEnvironment
import java.time.LocalDate
import javax.swing.ImageIcon
import javax.swing.UIManager

enum class ThemeId {
    None,
    Easter,
    Oktoberfest,
    Halloween;

    companion object {
        fun parseFromPreference(preference: String): ThemeId =
            try {
                ThemeId.valueOf(preference)
            } catch (ex: Exception) {
                logger.error(
                    CODE_PARSE_ERROR,
                    "Failed to parse ThemeId from preference: $preference",
                    ex,
                )
                return None
            }
    }
}

data class Theme(
    val id: ThemeId,
    val primary: Color,
    private val primaryDark: Color,
    val background: Color,
    val lightBackground: Color,
    val dartboardColours: ColourWrapper?,
    val linkColour: Color,
    val fontColor: Color = Color.BLACK,
    val menuFontSize: Float? = null,
    val animations: Map<IAnimationTrigger, IAnimation> = emptyMap(),
    val bannerOffset: Double = 0.0,
    val bannerScaleFactor: Double = 0.8,
    val finder: FestivalFinder? = null,
    val unlockDate: LocalDate? = null,
) {
    val name = id.name
    private val resourcePath = name.lowercase()
    val font = fontForResource("/theme/$resourcePath/font.ttf")
    private val dartboardFont = fontForResource("/theme/$resourcePath/dartboard.ttf") ?: font
    val banner = svgForResource("/theme/$resourcePath/banner.svg")

    val menuMusic = clipForResource("/theme/$resourcePath/menu.wav")
    val newGameSfx = clipForResource("/theme/$resourcePath/newGame.wav")

    init {
        dartboardFont?.let { dartboardColours?.font = dartboardFont }
    }

    fun apply() {
        logger.info(CODE_THEME_APPLIED, "Applying theme $name")

        val defaults = UIManager.getDefaults()

        defaults["control"] = background
        defaults["nimbusBase"] = primary
        defaults["nimbusBlueGrey"] = primaryDark
        defaults["nimbusFocus"] = primaryDark
        defaults["background"] = background
        defaults["nimbusLightBackground"] = lightBackground
        defaults["Table.alternateRowColor"] = null
        defaults["DesktopPane.background"] = background
        defaults["Panel.background"] = background
        defaults["text"] = fontColor
        defaults["nimbusSelectionBackground"] = primaryDark
        defaults["nimbusOrange"] = lightBackground
        defaults["nimbusBorder"] = background

        dartboardFont?.let { GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(it) }
        font?.let { GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(it) }
    }

    fun icon(path: String) = javaClass.getResource("/theme/$resourcePath$path")?.let(::ImageIcon)
}
