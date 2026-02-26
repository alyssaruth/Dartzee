package dartzee.theme

import dartzee.logging.CODE_THEME_APPLIED
import dartzee.`object`.ColourWrapper
import dartzee.utils.InjectedThings.logger
import java.awt.Color
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
) {
    val font = fontForResource("/theme/$name/font.ttf")

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
    }
}
