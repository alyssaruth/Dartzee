package burlton.dartzee.code.core.bean

import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

open class RadioButtonPanel : JPanel(), ChangeListener
{
    private val bg = ButtonGroup()
    var selection: JRadioButton? = null

    override fun add(arg0: Component): Component
    {
        if (arg0 is JRadioButton)
        {
            addRadioButton(arg0)
        }

        return super.add(arg0)
    }

    override fun add(arg0: Component, arg1: Any)
    {
        if (arg0 is JRadioButton)
        {
            addRadioButton(arg0)
        }

        super.add(arg0, arg1)
    }

    private fun addRadioButton(rdbtn: JRadioButton)
    {
        rdbtn.addChangeListener(this)

        bg.add(rdbtn)
        if (bg.buttonCount == 1)
        {
            //Ensure this is selected
            rdbtn.isSelected = true
        }
    }

    fun getSelectionStr() = selection?.text ?: ""

    fun setSelection(selectionStr: String)
    {
        val buttons = bg.elements
        while (buttons.hasMoreElements())
        {
            val button = buttons.nextElement()
            val buttonText = button.text
            if (buttonText == selectionStr)
            {
                button.isSelected = true
                stateChanged(ChangeEvent(button))
                break
            }
        }
    }

    fun addActionListener(listener: ActionListener)
    {
        val buttons = bg.elements
        while (buttons.hasMoreElements())
        {
            val button = buttons.nextElement()
            button.addActionListener(listener)
        }
    }

    fun removeActionListener(listener: ActionListener)
    {
        val buttons = bg.elements
        while (buttons.hasMoreElements())
        {
            val button = buttons.nextElement()
            button.removeActionListener(listener)
        }
    }

    fun isEventSource(evt: ActionEvent?): Boolean
    {
        evt ?: return false

        val source = evt.source

        val buttons = bg.elements
        while (buttons.hasMoreElements())
        {
            val button = buttons.nextElement()
            if (source === button)
            {
                return true
            }
        }

        return false
    }

    override fun stateChanged(arg0: ChangeEvent)
    {
        val src = arg0.source as JRadioButton
        if (src.isSelected)
        {
            selection = src
        }
    }
}
