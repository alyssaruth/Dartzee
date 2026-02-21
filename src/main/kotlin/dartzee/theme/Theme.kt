import dartzee.`object`.ColourWrapper
import dartzee.utils.InjectedThings
import dartzee.utils.ResourceCache
import java.awt.Color
import java.awt.Font
import javax.swing.ImageIcon
import javax.swing.UIManager

data class Theme(
    val name: String,
    val primary: Color,
    val background: Color,
    val lightBackground: Color,
    val dartboardColours: ColourWrapper,
    val fontColor: Color = Color.BLACK,
) {
    val font = fontForResource("/theme/$name/font.ttf")

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

fun themedIcon(path: String): ImageIcon {
    return InjectedThings.theme?.icon(path) ?: ImageIcon(Theme::class.java.getResource(path))
}

fun Theme.icon(path: String): ImageIcon? {
    return Theme::class.java.getResource("/theme/$name$path")?.let(::ImageIcon)
}
