import java.awt.Color
import javax.swing.UIManager

data class Theme(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val text: Color = Color.BLACK)
{
    fun apply()
    {
        UIManager.put("control", background)
        UIManager.put("nimbusBase", primary)
        UIManager.put("background", background)
        UIManager.put("DesktopPane.background", background)
        UIManager.put("Panel.background", background)
        UIManager.put("text", text)
    }
}