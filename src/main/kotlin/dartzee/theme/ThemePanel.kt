package dartzee.theme

import dartzee.core.bean.makeTransparentTextPane
import dartzee.core.util.alignCentrally
import dartzee.utils.ResourceCache
import java.awt.BorderLayout
import java.awt.Color
import java.time.LocalDate
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder
import net.miginfocom.swing.MigLayout

class ThemePanel(val themeId: ThemeId) : JPanel() {
    val theme = themeMap()[themeId]

    private val panelNorth = JPanel()
    private val lblName = JLabel(theme?.name ?: "Classic (none)")
    private val lblThemeDesc = makeTransparentTextPane().apply { alignCentrally() }

    init {
        border = EmptyBorder(20, 20, 20, 20)
        setLayout(BorderLayout(0, 20))

        panelNorth.layout = MigLayout("al center center")
        val font = theme?.font ?: ResourceCache.BASE_FONT
        val fontColor = theme?.fontColor ?: Color.BLACK

        panelNorth.add(JLabel(themedIcon("/buttons/playerManagement.png", theme)))
        panelNorth.add(lblName)
        panelNorth.add(JLabel(themedIcon("/buttons/newGame.png", theme)))
        lblName.font = font.deriveFont(20f + (theme?.menuFontSize ?: 14f))
        lblName.foreground = fontColor
        lblName.horizontalAlignment = SwingConstants.CENTER

        background = theme?.background
        panelNorth.background = theme?.background

        lblThemeDesc.foreground = fontColor
        lblThemeDesc.text = themeDescription(themeId, LocalDate.now())
        lblThemeDesc.font = ResourceCache.BASE_FONT.deriveFont(18f)

        add(panelNorth, BorderLayout.NORTH)
        add(lblThemeDesc, BorderLayout.CENTER)
    }
}
