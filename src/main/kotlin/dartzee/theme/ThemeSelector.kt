package dartzee.theme

import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JPanel

class ThemeSelector : JPanel(), ActionListener {
    private val themeIds = ThemeId.values().toList()

    private val btnRight = JButton()
    private val btnLeft = JButton()
    private var themePanel = ThemePanel(ThemeId.None)
    private val actionListeners = mutableListOf<ActionListener>()

    init {
        setLayout(BorderLayout(20, 0))

        btnRight.icon = themedIcon("/buttons/rightArrow.png")
        btnLeft.icon = themedIcon("/buttons/leftArrow.png")

        add(btnLeft, BorderLayout.WEST)
        add(btnRight, BorderLayout.EAST)
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

        btnLeft.background = themePanel.theme?.primary
        btnRight.background = themePanel.theme?.primary
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
