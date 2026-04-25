package dartzee.theme

import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.ActionEvent.ACTION_PERFORMED
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.border.TitledBorder
import net.miginfocom.swing.MigLayout

class ThemeSelector(private val themeIds: List<ThemeId> = ThemeId.values().toList()) :
    JPanel(), ActionListener {

    private val panelRight = JPanel()
    private val panelLeft = JPanel()
    private val btnNext = JButton()
    private val btnPrevious = JButton()
    private var themePanel = ThemePanel(themeIds.first())
    private val actionListeners = mutableListOf<ActionListener>()

    private val themePanels = themeIds.map { ThemePanel(it) }

    private val titleBorder =
        TitledBorder(null, "Theme", TitledBorder.LEADING, TitledBorder.TOP, null, null)

    init {
        border = titleBorder

        setLayout(BorderLayout(20, 0))

        btnNext.name = "NextTheme"
        btnPrevious.name = "PreviousTheme"

        btnNext.icon = themedIcon("/buttons/rightArrow.png")
        btnPrevious.icon = themedIcon("/buttons/leftArrow.png")

        panelLeft.layout = MigLayout("al center center")
        panelLeft.add(btnPrevious)

        panelRight.layout = MigLayout("al center center")
        panelRight.add(btnNext)

        add(panelLeft, BorderLayout.WEST)
        add(panelRight, BorderLayout.EAST)
        add(themePanel, BorderLayout.CENTER)

        selectionChanged(0)
        btnPrevious.addActionListener(this)
        btnNext.addActionListener(this)
    }

    fun selectedThemeId() = themePanel.themeId

    fun selectionIsLocked() = themePanel.theme?.isLocked() ?: false

    private fun selectionChanged(newIndex: Int) {
        remove(themePanel)
        themePanel = themePanels[newIndex]
        add(themePanel, BorderLayout.CENTER)

        repaint()

        btnPrevious.isEnabled = themeIds.indexOf(selectedThemeId()) > 0
        btnNext.isEnabled = themeIds.indexOf(selectedThemeId()) < themeIds.size - 1

        btnNext.icon = themedIcon("/buttons/rightArrow.png", themePanel.theme)
        btnPrevious.icon = themedIcon("/buttons/leftArrow.png", themePanel.theme)

        val btnColour =
            themePanel.theme?.getIfUnlocked(Theme::primary, null) ?: DEFAULT_BUTTON_COLOUR
        val bg =
            themePanel.theme?.getIfUnlocked(Theme::background, Color.DARK_GRAY)
                ?: DEFAULT_BACKGROUND

        btnPrevious.background = btnColour
        btnNext.background = btnColour

        titleBorder.titleColor =
            themePanel.theme?.getIfUnlocked(Theme::fontColor, Color.LIGHT_GRAY) ?: Color.BLACK

        panelRight.background = bg
        panelLeft.background = bg
        background = bg

        actionListeners.forEach { it.actionPerformed(ActionEvent(this, ACTION_PERFORMED, "")) }
    }

    private fun nextPressed() {
        val newIndex = themeIds.indexOf(selectedThemeId()) + 1
        selectionChanged(newIndex)
    }

    private fun previousPressed() {
        val newIndex = themeIds.indexOf(selectedThemeId()) - 1
        selectionChanged(newIndex)
    }

    fun selectTheme(themeId: ThemeId) {
        selectionChanged(themeIds.indexOf(themeId))
    }

    fun addActionListener(listener: ActionListener) {
        actionListeners.add(listener)
    }

    override fun actionPerformed(e: ActionEvent) {
        when (e.source) {
            btnNext -> nextPressed()
            btnPrevious -> previousPressed()
        }
    }
}
