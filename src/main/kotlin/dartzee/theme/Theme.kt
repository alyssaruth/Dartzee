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
    private val background: Color,
    val lightBackground: Color,
    val dartboardColours: ColourWrapper?,
    val linkColour: Color,
    val fontColor: Color = Color.BLACK,
    val menuFontSize: Float? = null,
    val animations: Map<IAnimationTrigger, IAnimation> = emptyMap(),
    val bannerOffset: Double = 0.0,
    val bannerScaleFactor: Double = 0.8,
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

        defaults.put("control", background)
        defaults.put("nimbusBase", primary)
        defaults.put("nimbusBlueGrey", primaryDark)
        defaults.put("nimbusFocus", primaryDark)
        defaults.put("background", background)
        defaults.put("nimbusLightBackground", lightBackground)
        defaults.put("Table.alternateRowColor", null)
        defaults.put("DesktopPane.background", background)
        defaults.put("Panel.background", background)
        defaults.put("text", fontColor)
        defaults.put("nimbusSelectionBackground", primaryDark)
        defaults.put("nimbusOrange", lightBackground)
        defaults.put("nimbusBorder", background)

        dartboardFont?.let { GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(it) }
        font?.let { GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(it) }
    }
}
