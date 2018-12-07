package burlton.dartzee.code.screen

import burlton.core.code.util.Debug
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.AbstractButton
import javax.swing.JButton
import javax.swing.JPanel


abstract class EmbeddedScreen : JPanel(), ActionListener
{
    private val btnBack = JButton(" < Back")
    private val btnNext = JButton(getNextText() + " > ")

    init
    {
        preferredSize = Dimension(800, 610)
        layout = BorderLayout(0, 0)

        val panel = JPanel()
        add(panel, BorderLayout.SOUTH)
        panel.layout = BorderLayout(0, 0)

        val panelNext = JPanel()
        panel.add(panelNext, BorderLayout.EAST)
        btnNext.font = Font("Tahoma", Font.PLAIN, 16)

        panelNext.add(btnNext)

        val panelBack = JPanel()
        panel.add(panelBack, BorderLayout.WEST)
        btnBack.font = Font("Tahoma", Font.PLAIN, 16)
        panelBack.add(btnBack)

        btnBack.isVisible = showBackButton()
        btnNext.isVisible = showNextButton()

        btnBack.addActionListener(this)
        btnNext.addActionListener(this)
    }

    abstract fun initialise()
    abstract fun getScreenName() : String

    /**
     * Called after the new screen has been switched in etc
     */
    fun postInit()
    {

    }

    open fun getBackTarget() : EmbeddedScreen
    {
        return ScreenCache.getScreen(MenuScreen::class.java)
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        val src = arg0.source as AbstractButton

        when (src)
        {
            btnBack -> backPressed()
            btnNext -> nextPressed()
            else -> Debug.stackTrace("Unexpected actionPerformed: " + src.text)
        }
    }

    open fun getNextText() : String
    {
        return "Next"
    }

    open fun hideBackButton()
    {
        btnBack.isVisible = false
    }


    private fun backPressed()
    {
        ScreenCache.switchScreen(getBackTarget(), false)
    }

    open fun nextPressed()
    {
        //default method
    }

    /**
     * Default methods
     */
    open fun showBackButton(): Boolean
    {
        return true
    }

    open fun showNextButton(): Boolean
    {
        return false
    }
}
