package dartzee.screen

import dartzee.logging.CODE_SWING_ERROR
import dartzee.theme.getBaseFont
import dartzee.theme.themedIcon
import dartzee.utils.InjectedThings.logger
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

abstract class EmbeddedScreen : JPanel(), ActionListener {
    val btnBack = JButton("Back")
    protected val btnNext = JButton()

    protected val panelNavigation = JPanel()
    protected val panelNext = JPanel()
    protected val panelBack = JPanel()
    protected val lblNext = JLabel()
    protected val iconNext = JLabel()

    init {
        preferredSize = Dimension(800, 610)
        layout = BorderLayout(0, 0)

        add(panelNavigation, BorderLayout.SOUTH)
        panelNavigation.layout = BorderLayout(0, 0)

        panelNavigation.add(panelNext, BorderLayout.EAST)
        panelNext.add(btnNext)

        panelNavigation.add(panelBack, BorderLayout.WEST)
        panelBack.add(btnBack)

        lblNext.name = "NextText"
        btnNext.name = "Next"
        btnNext.layout = BorderLayout(5, 0)
        btnNext.add(lblNext, BorderLayout.CENTER)
        btnNext.add(iconNext, BorderLayout.EAST)
        btnBack.name = "Back"

        updateAppearance()

        btnBack.addActionListener(this)
        btnNext.addActionListener(this)
    }

    abstract fun initialise()

    abstract fun getScreenName(): String

    /** Called after the new screen has been switched in etc */
    open fun postInit() {
        btnBack.isVisible = showBackButton()
        btnNext.isVisible = showNextButton()

        lblNext.text = getNextText()
    }

    open fun getBackTarget(): EmbeddedScreen = ScreenCache.get<MenuScreen>()

    override fun actionPerformed(arg0: ActionEvent) {
        val src = arg0.source
        when (arg0.source) {
            btnBack -> backPressed()
            btnNext -> nextPressed()
            else -> logger.error(CODE_SWING_ERROR, "Unexpected actionPerformed: $src")
        }
    }

    open fun getNextText() = "Next"

    open fun hideBackButton() {
        btnBack.isVisible = false
    }

    open fun backPressed() {
        ScreenCache.switch(getBackTarget(), false)
    }

    protected fun toggleNextVisibility(visible: Boolean) {
        btnNext.isVisible = visible
    }

    open fun nextPressed() {
        // default method
    }

    private fun updateAppearance() {
        val baseFont = getBaseFont()

        iconNext.icon = themedIcon("/buttons/rightArrow.png")
        btnBack.icon = themedIcon("/buttons/leftArrow.png")

        lblNext.font = baseFont.deriveFont(Font.PLAIN, 20f)
        btnBack.font = baseFont.deriveFont(Font.PLAIN, 20f)
    }

    open fun fireAppearancePreferencesChanged() {
        updateAppearance()
    }

    /** Default methods */
    open fun showBackButton() = true

    open fun showNextButton() = false
}
