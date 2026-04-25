package dartzee.theme

import dartzee.core.bean.makeTransparentTextPane
import dartzee.core.util.alignCentrally
import dartzee.utils.ResourceCache
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder
import net.miginfocom.swing.MigLayout

class ThemePanel(val themeId: ThemeId) : JPanel() {
    val theme = themeMap()[themeId]

    private val panelNorth = JPanel()
    private val lblName = JLabel(indexedThemeName())
    private val lblThemeDesc = makeTransparentTextPane().apply { alignCentrally() }

    init {
        border = EmptyBorder(20, 20, 20, 20)
        setLayout(BorderLayout(0, 20))

        panelNorth.layout = MigLayout("al center center")
        val font =
            theme?.getIfUnlocked(Theme::font, ResourceCache.BASE_FONT) ?: ResourceCache.BASE_FONT
        val fontColor = theme?.getIfUnlocked(Theme::fontColor, Color.LIGHT_GRAY) ?: Color.BLACK

        val menuFontSize = theme?.getIfUnlocked(Theme::menuFontSize, 14f) ?: 10f

        val lblLeftIcon = JLabel(getLeftIcon())
        lblLeftIcon.name = "LeftIcon"
        panelNorth.add(lblLeftIcon)

        panelNorth.add(lblName)

        val lblRightIcon = JLabel(getRightIcon())
        lblRightIcon.name = "RightIcon"
        panelNorth.add(lblRightIcon)

        lblName.name = "Name"
        lblName.font = font.deriveFont(20f + menuFontSize)
        lblName.foreground = fontColor
        lblName.horizontalAlignment = SwingConstants.CENTER

        val bg = theme?.getIfUnlocked(Theme::background, Color.DARK_GRAY)
        background = bg
        panelNorth.background = bg

        lblThemeDesc.foreground = fontColor
        lblThemeDesc.text = themeDescription(themeId)
        lblThemeDesc.font = ResourceCache.BASE_FONT.deriveFont(18f)
        lblThemeDesc.name = "Description"

        add(panelNorth, BorderLayout.NORTH)
        add(lblThemeDesc, BorderLayout.CENTER)
    }

    private fun getLeftIcon() = getIconIfUnlocked("/buttons/playerManagement.png")

    private fun getRightIcon() = getIconIfUnlocked("/buttons/newGame.png")

    private fun getIconIfUnlocked(path: String) =
        if (theme?.isLocked() ?: false) {
            ImageIcon(javaClass.getResource("/theme/locked.png"))
        } else {
            themedIcon(path, theme)
        }

    private fun indexedThemeName(): String {
        val index = ThemeId.values().indexOf(themeId) + 1
        return "$index. ${getThemeName()}"
    }

    private fun getThemeName(): String {
        if (theme == null) {
            return "Classic (none)"
        }

        return if (theme.isLocked()) "Locked" else theme.name
    }
}
