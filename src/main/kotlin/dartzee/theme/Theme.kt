import dartzee.`object`.ColourWrapper
import dartzee.utils.InjectedThings
import dartzee.utils.ResourceCache
import java.awt.Color
import java.awt.Font
import javax.swing.ImageIcon
import javax.swing.JDialog
import javax.swing.JFrame
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

        // TODO - Trying to set frame/dialog bar colors, not working on linux
        JDialog.setDefaultLookAndFeelDecorated(true)
        JFrame.setDefaultLookAndFeelDecorated(true)

        UIManager.put("Windows.TitlePane.borderColor", Color.YELLOW)
        UIManager.put("Windows.TitlePane.background", Color.CYAN)
        UIManager.put("Windows.TitlePane.foreground", Color.RED)
        UIManager.put("Windows.TitlePane.inactiveBackground", Color.BLUE)
        UIManager.put("Windows.TitlePane.inactiveForeground", Color.ORANGE)
        UIManager.put("JRootPane.titleBarBackground", lightBackground)
        UIManager.put("activeCaption", lightBackground)
        UIManager.put("activeCaptionText", javax.swing.plaf.ColorUIResource(fontColor))

        // This worked but not needed now
        // UIManager.put("Button[MouseOver].backgroundPainter", FillPainter(Color(127, 255, 191)))
    }
}

// internal class FillPainter(private val color: Color) : Painter<JComponent?> {
//    override fun paint(g: Graphics2D, `object`: JComponent?, width: Int, height: Int) {
//        g.color = color
//        g.fillRect(0, 0, width - 1, height - 1)
//    }
// }

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
