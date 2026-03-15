package dartzee.theme

import dartzee.logging.CODE_THEME_APPLIED
import dartzee.`object`.ColourWrapper
import dartzee.screen.animation.IAnimation
import dartzee.screen.animation.IAnimationTrigger
import dartzee.utils.InjectedThings.logger
import java.awt.Color
import java.awt.GraphicsEnvironment
import javax.swing.UIManager

data class Theme(
    val name: String,
    val primary: Color,
    val primaryDark: Color,
    val background: Color,
    val lightBackground: Color,
    val dartboardColours: ColourWrapper,
    val linkColour: Color,
    val fontColor: Color = Color.BLACK,
    val menuFontSize: Float? = null,
    val animations: Map<IAnimationTrigger, IAnimation> = emptyMap(),
    val bannerOffset: Double = 0.0,
    val bannerScaleFactor: Double = 0.8,
) {
    val font = fontForResource("/theme/$name/font.ttf")
    val dartboardFont = fontForResource("/theme/$name/dartboard.ttf") ?: font
    val banner = svgForResource("/theme/$name/banner.svg")

    val menuMusic = clipForResource("/theme/$name/menu.wav")
    val newGameSfx = clipForResource("/theme/$name/newGame.wav")

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
