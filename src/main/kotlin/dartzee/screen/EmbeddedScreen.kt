package dartzee.screen

import dartzee.logging.CODE_SWING_ERROR
import dartzee.utils.InjectedThings.logger
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JPanel

abstract class EmbeddedScreen : JPanel(), ActionListener {
    val btnBack = JButton(" < Back")
    val btnNext = JButton("Next > ")

    protected val panelNavigation = JPanel()
    protected val panelNext = JPanel()
    protected val panelBack = JPanel()

    init {
        preferredSize = Dimension(800, 610)
        layout = BorderLayout(0, 0)

        add(panelNavigation, BorderLayout.SOUTH)
        panelNavigation.layout = BorderLayout(0, 0)

        panelNavigation.add(panelNext, BorderLayout.EAST)
        btnNext.font = Font("Tahoma", Font.PLAIN, 20)

        panelNext.add(btnNext)

        panelNavigation.add(panelBack, BorderLayout.WEST)
        btnBack.font = Font("Tahoma", Font.PLAIN, 20)
        panelBack.add(btnBack)

        btnBack.addActionListener(this)
        btnNext.addActionListener(this)
    }

    abstract fun initialise()

    abstract fun getScreenName(): String

    /** Called after the new screen has been switched in etc */
    open fun postInit() {
        btnBack.isVisible = showBackButton()
        btnNext.isVisible = showNextButton()

        btnNext.text = getNextText() + " >"
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

    fun toggleNextVisibility(visible: Boolean) {
        btnNext.isVisible = visible
    }

    open fun nextPressed() {
        // default method
    }

    /** Default methods */
    open fun showBackButton() = true

    open fun showNextButton() = false
}
