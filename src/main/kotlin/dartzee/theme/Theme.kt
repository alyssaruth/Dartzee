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
        UIManager.put("control", background)
        UIManager.put("nimbusBase", primary)
        UIManager.put("nimbusBlueGrey", primaryDark)
        UIManager.put("nimbusFocus", primaryDark)
        UIManager.put("background", background)
        UIManager.put("nimbusLightBackground", lightBackground)
        UIManager.put("Table.alternateRowColor", null)
        UIManager.put("DesktopPane.background", background)
        UIManager.put("Panel.background", background)
        UIManager.put("text", fontColor)
        UIManager.put("nimbusSelectionBackground", background)
        UIManager.put("nimbusOrange", lightBackground)

        UIManager.put("nimbusBorder", background)
    }
}
