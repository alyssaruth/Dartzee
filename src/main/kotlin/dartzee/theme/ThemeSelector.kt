package dartzee.theme

import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.border.TitledBorder
import net.miginfocom.swing.MigLayout

class ThemeSelector : JPanel(), ActionListener {
    private val themeIds = ThemeId.values().toList()

    private val panelRight = JPanel()
    private val panelLeft = JPanel()
    private val btnRight = JButton()
    private val btnLeft = JButton()
    private var themePanel = ThemePanel(ThemeId.None)
    private val actionListeners = mutableListOf<ActionListener>()

    private val titleBorder =
        TitledBorder(null, "Theme", TitledBorder.LEADING, TitledBorder.TOP, null, null)

    init {
        border = titleBorder

        setLayout(BorderLayout(20, 0))

        btnRight.icon = themedIcon("/buttons/rightArrow.png")
        btnLeft.icon = themedIcon("/buttons/leftArrow.png")

        panelLeft.layout = MigLayout("al center center")
        panelLeft.add(btnLeft)

        panelRight.layout = MigLayout("al center center")
        panelRight.add(btnRight)

        add(panelLeft, BorderLayout.WEST)
        add(panelRight, BorderLayout.EAST)
        add(themePanel, BorderLayout.CENTER)

        btnLeft.addActionListener(this)
        btnRight.addActionListener(this)
    }

    fun selectedThemeId() = themePanel.themeId

    private fun selectionChanged(newIndex: Int) {
        remove(themePanel)
        themePanel = ThemePanel(themeIds[newIndex])
        add(themePanel, BorderLayout.CENTER)

        repaint()

        btnLeft.isEnabled = themeIds.indexOf(selectedThemeId()) > 0
        btnRight.isEnabled = themeIds.indexOf(selectedThemeId()) < themeIds.size - 1

        btnRight.icon = themedIcon("/buttons/rightArrow.png", themePanel.theme)
        btnLeft.icon = themedIcon("/buttons/leftArrow.png", themePanel.theme)

        val btnColour = themePanel.theme?.primary ?: Color(169, 176, 190)
        val bg = themePanel.theme?.background ?: Color(214, 217, 223)

        btnLeft.background = btnColour
        btnRight.background = btnColour

        titleBorder.titleColor = themePanel.theme?.fontColor ?: Color.BLACK

        panelRight.background = bg
        panelLeft.background = bg
        background = bg
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
            btnRight -> nextPressed()
            btnLeft -> previousPressed()
        }

        actionListeners.forEach { it.actionPerformed(e) }
    }
}
