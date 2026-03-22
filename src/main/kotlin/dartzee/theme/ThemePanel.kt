package dartzee.theme

import dartzee.core.bean.makeTransparentTextPane
import dartzee.core.util.alignCentrally
import dartzee.utils.ResourceCache
import java.awt.BorderLayout
import java.time.LocalDate
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder

class ThemePanel(val themeId: ThemeId) : JPanel() {
    val theme = themeMap()[themeId]

    private val lblName = JLabel(theme?.name ?: "Classic (no theme)")
    private val lblThemeDesc = makeTransparentTextPane().apply { alignCentrally() }

    init {
        border = EmptyBorder(20, 20, 20, 20)
        setLayout(BorderLayout(0, 20))

        val font = theme?.font ?: ResourceCache.BASE_FONT
        lblName.font = font.deriveFont(20f + (theme?.menuFontSize ?: 18f))
        lblName.foreground = theme?.fontColor
        lblName.horizontalAlignment = SwingConstants.CENTER

        background = theme?.background

        lblThemeDesc.foreground = theme?.fontColor
        lblThemeDesc.text = themeDescription(themeId, LocalDate.now())
        lblThemeDesc.font = ResourceCache.BASE_FONT.deriveFont(18f)

        add(lblName, BorderLayout.NORTH)
        add(lblThemeDesc, BorderLayout.CENTER)
    }
}
