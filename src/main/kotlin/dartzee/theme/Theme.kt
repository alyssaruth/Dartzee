import dartzee.utils.InjectedThings
import dartzee.utils.ResourceCache
import java.awt.Color
import java.awt.Font
import javax.swing.UIManager

data class Theme(
    val primary: Color,
    val background: Color,
    val lightBackground: Color,
    val font: Font,
    val fontColor: Color = Color.BLACK,
) {
    fun apply() {
        UIManager.put("control", background)
        UIManager.put("nimbusBase", primary)
        UIManager.put("nimbusFocus", primary.darker())
        UIManager.put("background", background)
        UIManager.put("nimbusLightBackground", lightBackground)
        UIManager.put("Table.alternateRowColor", null)
        UIManager.put("DesktopPane.background", background)
        UIManager.put("Panel.background", background)
        UIManager.put("text", fontColor)
        UIManager.put("nimbusSelectionBackground", background)
    }
}

fun fontForResource(resourcePath: String): Font {
    val fontStream =
        Theme::class.java.getResourceAsStream(resourcePath)
            ?: throw RuntimeException("Font not found for path $resourcePath")

    return Font.createFont(Font.TRUETYPE_FONT, fontStream)
}

fun getBaseFont(): Font = InjectedThings.theme?.font ?: ResourceCache.BASE_FONT
