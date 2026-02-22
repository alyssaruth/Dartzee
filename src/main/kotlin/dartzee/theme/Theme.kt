package dartzee.theme

import dartzee.`object`.ColourWrapper
import java.awt.Color
import javax.swing.UIManager

data class Theme(
    val name: String,
    val primary: Color,
    val primaryDark: Color,
    val background: Color,
    val lightBackground: Color,
    val dartboardColours: ColourWrapper,
    val fontColor: Color = Color.BLACK,
) {
    val font = fontForResource("/theme/$name/font.ttf")

    fun apply() {
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
        defaults.put("nimbusSelectionBackground", background)
        defaults.put("nimbusOrange", lightBackground)
        defaults.put("nimbusBorder", background)
    }
}
